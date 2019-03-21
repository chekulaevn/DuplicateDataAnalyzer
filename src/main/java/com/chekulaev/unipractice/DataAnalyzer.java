package com.chekulaev.unipractice;

import com.chekulaev.unipractice.exceptions.*;
import com.chekulaev.unipractice.utils.AnalysisReport;
import com.chekulaev.unipractice.utils.FinderReport;
import com.chekulaev.unipractice.utils.FinderInput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;

@PropertySource(value = "actualColumnNames.properties", encoding="UTF-8")
public class DataAnalyzer {
    private JdbcTemplate jdbcTemplate;
    private DuplicateFinder duplicateFinder;
    private TypoFinder typoFinder;
    private String tableName;

    @Value("#{'${column.names}'.split(',')}")
    private String[] actualColumnNames;
    private final int actualColumnNumber = 8;

    public DataAnalyzer(JdbcTemplate jdbcTemplate, DuplicateFinder df, TypoFinder tf) {
        this.jdbcTemplate = jdbcTemplate;
        this.duplicateFinder = df;
        this.typoFinder = tf;
    }

    private void checkConnection() {
        try {
            jdbcTemplate.queryForObject("SELECT count(*) FROM " + tableName, int.class);
        } catch (BadSqlGrammarException e) {
            throw new TableNotExistException("Table '" + tableName + "' doesn't exist in the database.");
        } catch (CannotGetJdbcConnectionException e) {
            throw new ConnectionEstablishmentException("Cannot establish connection: \n" +
                    e.getCause().getMessage());
        }
    }

    private void checkActualColumnNamesAndCount() {
        if (actualColumnNames.length != actualColumnNumber) {
            throw new ColumnNotExistException("Wrong actual column name count (expected "
                    + actualColumnNumber + ", got " + actualColumnNames.length + ")." +
                    " Check 'actualColumnNames.properties'.");
        }

        for (String name : actualColumnNames) {
            if (Character.isDigit(name.charAt(0))) {
                throw new ColumnNotExistException("Invalid actual column name: " + name + ". Check" +
                        " 'actualColumnNames.properties'.");
            }
        }
    }

    /**
     * Checks, if actualColumnNames exist in the database.
     * Checks, if table is not empty.
     */
    private void checkActualColumns() {
        checkActualColumnNamesAndCount();

        for (String name : actualColumnNames) {
            if (!name.equals("-")) {
                try {
                    jdbcTemplate.queryForObject("SELECT " + name + " FROM " + tableName + " WHERE id = 1",
                            (rs,rowNum) -> null);
                } catch (BadSqlGrammarException e) {
                    throw new ColumnNotExistException("Column '" + name + "' from " +
                            "'actualColumnNames.properties' does not exist in the database. Check " +
                            "'actualColumnNames.properties'.");
                } catch (EmptyResultDataAccessException e) {
                    throw new TableIsEmptyException("Table is empty.");
                }
            }
        }
    }

    /**
     * Guarantied, that input array != null
     */
    private String[] removeNullTailOfStringArray(String[] array) {
        int n = array.length - 1;
        while (n >= 0 && array[n] == null) {
            n--;
        }
        array = Arrays.copyOf(array, n+1);
        return array;
    }

    /**
     * Generates SQL SELECT query columns part
     */
    private String formSelectQuery(String[] requestedColumns) {
        StringBuilder query = new StringBuilder();
        query.append(requestedColumns[0]);
        for (int i = 1; i < requestedColumns.length; i++) {
            query.append(", ");
            query.append(requestedColumns[i]);
        }
        return query.toString();
    }

    /**
     * Checks, if requestedColumns correspond to actualColumnNames and are present in the database
     * @return boolean array for fixed (actualColumnNames) columns: true, if the column will be analyzed, false
     * otherwise
     */
    private FinderInput getFindersInput(String[] requestedColumns) {
        boolean[] fixedColumnsFlagArray = new boolean[actualColumnNumber];
        String[] additionalColumns = new String[requestedColumns.length];
        int k = 0;
        boolean requestedColumnFound;
        for (String requestedColumn : requestedColumns) {
            if (Character.isDigit(requestedColumn.charAt(0))) {
                throw new ColumnNotExistException("Column '" + requestedColumn + "', requested" +
                        " for analysis, is invalid.");
            }

            requestedColumnFound = false;
            for (int i = 0; i < actualColumnNumber; i++) {
                if (requestedColumn.equals(actualColumnNames[i])) {
                    requestedColumnFound = true;
                    fixedColumnsFlagArray[i] = true;
                }
            }
            if (!requestedColumnFound) {
                try {
                    jdbcTemplate.queryForObject("SELECT " + requestedColumn + " FROM " + tableName +
                            " WHERE id = 1", (rs,rowNum) -> null);
                } catch (BadSqlGrammarException e) {
                    throw new ColumnNotExistException("Column '" + requestedColumn + "', requested" +
                            " for analysis, does not exist in the database.");
                } catch (EmptyResultDataAccessException e) {
                    throw new TableIsEmptyException("Table is empty.");
                }
                additionalColumns[k] = requestedColumn;
                k++;
            }
        }
        additionalColumns = removeNullTailOfStringArray(additionalColumns);
        return new FinderInput(fixedColumnsFlagArray,additionalColumns,formSelectQuery(requestedColumns));
    }

    /**
     * Checks connection, actualColumnNames, analysis input. Calls duplicate and typo finders.
     * Generates analysis report.
     */
    public AnalysisReport analyze(String tableName, String[] requestedColumns) {
        this.tableName = tableName;

        if (requestedColumns == null || requestedColumns.length == 0) {
            throw new InvalidAnalyzerInputException("Input is empty.");
        }
        for (String column : requestedColumns) {
            if (column == null || column.equals("")) {
                throw new InvalidAnalyzerInputException("Input contains null or empty string.");
            }
        }

        long startTime = System.currentTimeMillis();

        checkConnection();
        checkActualColumns();
        FinderInput input = getFindersInput(requestedColumns);

        FinderReport report = duplicateFinder.find(tableName,input);
        int recordCount = report.getProcessedRecordsNumber();
        int duplicateCount = report.getFoundRecordsNumber();
        report = typoFinder.find(tableName,input);
        int errorCount = report.getFoundRecordsNumber();

        double timeSpent = (System.currentTimeMillis() - startTime)/1000.0;
        return new AnalysisReport(timeSpent, recordCount, duplicateCount, errorCount);
    }
}