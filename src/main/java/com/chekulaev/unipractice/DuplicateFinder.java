package com.chekulaev.unipractice;

import com.chekulaev.unipractice.exceptions.IllegalDatabaseModelException;
import com.chekulaev.unipractice.simhash.BinaryWordSeg;
import com.chekulaev.unipractice.simhash.Simhash;
import com.chekulaev.unipractice.utils.*;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DuplicateFinder {
    private JdbcTemplate jdbcTemplate;
    private String tableName;
    private final String outputFileName = "analyzerOutput.txt";
    private final Simhash simhash = new Simhash(new BinaryWordSeg());

    public DuplicateFinder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private class RowSetExtractor implements ResultSetExtractor<List<PersonIndexEntry>> {
        private final String[] columns;

        public RowSetExtractor(String[] columns) {
            this.columns = columns;
        }

        private String removeEmptyStringTail(String string) {
            char[] charArray = string.toCharArray();
            int i = charArray.length - 1;
            while (i >= 0 && charArray[i] == ' ') {
                i--;
            }
            char[] resultCharArray = new char[i+1];
            System.arraycopy(charArray,0,resultCharArray,0,i+1);
            return new String(resultCharArray);
        }

        private PersonModelShell readPersonShell(ResultSet rs) throws SQLException {
            try {
                PersonModelShell personShell = new PersonModelShell(rs.getInt("id"));
                for (String columnName : columns) {
                    Field field = Person.class.getField(columnName);
                    if (field.getType().equals(String.class)) {
                        personShell.getStringFields().add(removeEmptyStringTail(rs.getString(columnName)));
                    } else {
                        personShell.getOtherFields().add(rs.getObject(columnName));
                    }
                }
                return personShell;
            } catch (NoSuchFieldException e) {
                throw new IllegalDatabaseModelException("Illegal database model in data generator, chech the " +
                        "'Person' class' fields");
            }
        }

        @Override
        public List<PersonIndexEntry> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<PersonIndexEntry> hashIndex = new LinkedList<>();
            while (rs.next()) {
                PersonModelShell personShell = readPersonShell(rs);

                List<String> stringFields = personShell.getStringFields();
                List<Object> otherFields = personShell.getOtherFields();
                HashPair[] entryStrings = new HashPair[stringFields.size()];
                Object[] entryObjects = new Object[otherFields.size()];

                ListIterator<String> it = stringFields.listIterator();
                int i = 0;
                while (it.hasNext()) {
                    String stringField = it.next();
                    entryStrings[i] = new HashPair(simhash.simhash32(stringField), stringField);
                    i++;
                }
                ListIterator<Object> ito = otherFields.listIterator();
                i = 0;
                while (ito.hasNext()) {
                    entryObjects[i] = ito.next();
                    i++;
                }

                hashIndex.add(new PersonIndexEntry(personShell.getId(), entryStrings, entryObjects));
            }

            return hashIndex;
        }
    }

    /**
     * Forms SELECT-SQL-query for requested columns selection
     */
    private String formSqlQuery(String[] requestedColumns) {
        StringBuilder query = new StringBuilder();
        query.append(requestedColumns[0]);
        for (int i = 1; i < requestedColumns.length; i++) {
            query.append(", ");
            query.append(requestedColumns[i]);
        }
        return "SELECT id, " + query.toString() + " FROM " + tableName;
    }

    private void printHashIndex(List<PersonIndexEntry> hashIndex) {
        if (hashIndex.isEmpty()) {
            System.out.print("<empty>");
            return;
        }
        int index = 0;
        for (PersonIndexEntry entry : hashIndex) {
            if (index > 99) break;
            System.out.println("id = " + entry.getId() + " " + Arrays.toString(entry.getEntryStrings()) +
                    "; Objects: " + Arrays.toString(entry.getEntryObjects()));
            index++;
        }
    }

    private void printDuplicateSet(LinkedHashSet<PersonIndexEntry> duplicateSet) {
        Iterator<PersonIndexEntry> it = duplicateSet.iterator();
        PersonIndexEntry entry1, entry2;
        if (it.hasNext()) {
            entry1 = it.next();
            System.out.print("id = " + entry1.getId() + " [");
            for (HashPair hashPair : entry1.getEntryStrings()) {
                System.out.print(Long.toBinaryString(hashPair.getHash()) + " - " + hashPair.getValue() + ", ");
            }
            System.out.print("]; Objects: [");
            for (Object object : entry1.getEntryObjects()) {
                System.out.print(object.toString() + ", ");
            }
            System.out.print("]");
            System.out.println();
        } else {
            System.out.print("<empty>");
            return;
        }
        int index = 1;
        while (it.hasNext() && index < 100) {
            entry2 = it.next();
            HashPair[] entry1Strings = entry1.getEntryStrings();
            HashPair[] entry2Strings = entry2.getEntryStrings();
            System.out.print("id = " + entry2.getId() + " [");
            for (int i = 0; i < entry2Strings.length; i++) {
                System.out.print(Long.toBinaryString(entry2Strings[i].getHash()) +
                        "(distance=" + simhash.hammingDistance(entry1Strings[i].getHash(),
                        entry2Strings[i].getHash()) + ") - " + entry2Strings[i].getValue() + ", ");
            }
            System.out.print("]; Objects: [");
            for (Object object : entry2.getEntryObjects()) {
                System.out.print(object.toString() + ", ");
            }
            System.out.print("]");
            System.out.println();

            entry1 = entry2;
            index++;
        }
    }

    private void printResultToFile(LinkedHashSet<PersonIndexEntry> duplicateSet, String[] requestedColumns) {
        try {
            new File(outputFileName).delete();
            PrintStream out = new PrintStream(new FileOutputStream(outputFileName));
            out.println("------------------------------");
            out.println(" Table: " + tableName);
            out.println(" Columns: " + Arrays.toString(requestedColumns));
            out.println(" Time: " + new Date().toString());
            out.println(" -> Duplicate count: " + duplicateSet.size());
            out.println("------------------------------");

            if (duplicateSet.isEmpty()) {
                out.print("<empty>");
            } else {
                for (PersonIndexEntry entry : duplicateSet) {
                    HashPair[] pairs = entry.getEntryStrings();
                    String[] stringValues = new String[pairs.length];
                    for (int i = 0; i < pairs.length; i++) {
                        stringValues[i] = pairs[i].getValue();
                    }
                    out.println("id = " + entry.getId() + " " + Arrays.toString(stringValues) +
                            "; Non strings: " + Arrays.toString(entry.getEntryObjects()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't write to output file '" + outputFileName + "'!");
        }
    }

    private LinkedHashSet<PersonIndexEntry> formTemporaryDuplicateSet(List<PersonIndexEntry> hashIndex,
                                                                 int hammingDistance) {
        LinkedHashSet<PersonIndexEntry> tempDuplicateSet = new LinkedHashSet<>();
        ListIterator<PersonIndexEntry> it;
        for (int i = 0; i < 64; i++) {
            hashIndex.sort((entry1,entry2)->entry1.getEntryStrings()[0].getHash()
                    .compareTo(entry2.getEntryStrings()[0].getHash()));
            PersonIndexEntry entry1 = new PersonIndexEntry(0,null,null);
            PersonIndexEntry entry2;
            it = hashIndex.listIterator();
            if (it.hasNext()) {
                entry1 = it.next();
            }
            while (it.hasNext()) {
                entry2 = it.next();
                if (simhash.hammingDistance(entry1.getEntryStrings()[0].getHash(),
                        entry2.getEntryStrings()[0].getHash()) <= hammingDistance) {
                    tempDuplicateSet.add(entry1);
                    tempDuplicateSet.add(entry2);
                }

                entry1 = entry2;
            }

            for (PersonIndexEntry indexEntry : hashIndex) {
                Long hash = indexEntry.getEntryStrings()[0].getHash();
                int lowerBits = (int)(hash.longValue());
                int rotated = Integer.rotateLeft(lowerBits, 1);
                indexEntry.getEntryStrings()[0].setHash(rotated & 0x00000000FFFFFFFFL);
            }
        }
        return tempDuplicateSet;
    }

    private LinkedHashSet<PersonIndexEntry> formFinalDuplicateSet(List<PersonIndexEntry> hashIndex,
                                                                      int hammingDistance) {
        LinkedHashSet<PersonIndexEntry> finalDuplicateSet = new LinkedHashSet<>();
        ListIterator<PersonIndexEntry> it1 = hashIndex.listIterator();
        ListIterator<PersonIndexEntry> it2;
        PersonIndexEntry entry1, entry2;
        while (it1.hasNext()) {
            entry1 = it1.next();
            it2 = hashIndex.listIterator(it1.nextIndex());
            while (it2.hasNext()) {
                entry2 = it2.next();
                HashPair[] entry1Strings = entry1.getEntryStrings();
                HashPair[] entry2Strings = entry2.getEntryStrings();
                Object[] entry1Objects = entry1.getEntryObjects();
                Object[] entry2Objects = entry2.getEntryObjects();
                boolean flag = true;
                for (int i = 0; i < entry1Strings.length; i++) {
                    if (simhash.hammingDistance(entry1Strings[i].getHash(),
                            entry2Strings[i].getHash()) > hammingDistance) {
                        flag = false;
                    }
                }
                for (int i = 0; i < entry1Objects.length; i++) {
                    if (!entry1Objects[i].equals(entry2Objects[i])) {
                        flag = false;
                    }
                }
                if (flag) {
                    finalDuplicateSet.add(entry1);
                    finalDuplicateSet.add(entry2);
                }
            }
        }
        return finalDuplicateSet;
    }

    public FinderReport find(String tableName, String[] requestedColumns, int hammingDistance) {
        long startTime = System.currentTimeMillis();

        this.tableName = tableName;
        new FinderInputChecker(jdbcTemplate, tableName).check(requestedColumns);

        List<PersonIndexEntry> hashIndex = jdbcTemplate.query(formSqlQuery(requestedColumns),
                new RowSetExtractor(requestedColumns));
        System.out.println("HashIndex (first 100 records):");
        printHashIndex(hashIndex);
        System.out.println();

        LinkedHashSet<PersonIndexEntry> finalDuplicateSet;
        if (hashIndex.get(0).getEntryStrings().length > 0) {
            LinkedHashSet<PersonIndexEntry> tempDuplicateSet = formTemporaryDuplicateSet(hashIndex, hammingDistance);
            System.out.println("Temporary duplicate set (first 100 records):");
            printDuplicateSet(tempDuplicateSet);
            System.out.println("Temporary duplicate set element count: " + tempDuplicateSet.size());
            System.out.println();

            finalDuplicateSet = formFinalDuplicateSet(new LinkedList<>(tempDuplicateSet), hammingDistance);
        } else {
            finalDuplicateSet = formFinalDuplicateSet(hashIndex, hammingDistance);
        }
        System.out.println("Final duplicate set (first 100 records):");
        printDuplicateSet(finalDuplicateSet);
        System.out.println();
        printResultToFile(finalDuplicateSet, requestedColumns);

        return new FinderReport(hashIndex.size(),finalDuplicateSet.size(),
                (System.currentTimeMillis() - startTime)/1000.0);
    }
}
