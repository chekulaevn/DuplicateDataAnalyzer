package com.chekulaev.unipractice;

import com.chekulaev.unipractice.utils.FinderReport;
import com.chekulaev.unipractice.utils.FinderInput;
import com.chekulaev.unipractice.utils.SimilarRowsComparator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeSet;

@PropertySource(value = "actualColumnNames.properties", encoding="UTF-8")
public class DuplicateFinder {
    private JdbcTemplate jdbcTemplate;
    private String tableName;

    @Value("#{'${column.names}'.split(',')}")
    private String[] actualColumnNames;
    private final int actualColumnNumber = 8;

    public DuplicateFinder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public class RowSetExtractor implements ResultSetExtractor<FinderReport> {
        boolean[] fixedColumnsFlagArray;
        String[] additionalColumns;

        public RowSetExtractor(FinderInput input) {
            this.fixedColumnsFlagArray = input.getFixedColumnsFlagArray();
            this.additionalColumns = input.getAdditionalColumns();
        }

        /**
         * Adds table rows to TreeSet, which uses SimilarRowsComparator.
         * Checks, if TreeSet contains current row. If so, duplicate count increases by 1.
         */
        @Override
        public FinderReport extractData(ResultSet rs) throws SQLException, DataAccessException {
            TreeSet<String[]> set = new TreeSet<>(new SimilarRowsComparator());
            int rowCount = 0;
            int duplicateCount = 0;
            while (rs.next()) {
                String[] row = new String[actualColumnNumber + additionalColumns.length];
                int i = 0;
                for (i = 0; i < actualColumnNumber; i++) {
                    if (fixedColumnsFlagArray[i]) {
                        row[i] = rs.getString(actualColumnNames[i]);
                    }
                }
                for (String column : additionalColumns) {
                    row[i] = rs.getString(additionalColumns[i-actualColumnNumber]);
                    i++;
                }
                if (set.contains(row)) {

                    System.out.print("+");//////
                    for (String str : row) {
                        System.out.print(" " + str);
                    }
                    System.out.println();

                    duplicateCount++;
                } else {
                    set.add(row);
                }
                rowCount++;
            }

            for (String[] strs : set) {//////
                for (String str : strs) {
                    System.out.print(" " + str);
                }
                System.out.println();
            }
            return new FinderReport(rowCount,duplicateCount);
        }
    }

    public FinderReport find(String tableName, FinderInput input) {
        this.tableName = tableName;
        String selectQuery = input.getSelectQuery();
        return jdbcTemplate.query("SELECT " + selectQuery + " FROM " + tableName,
                new RowSetExtractor(input));
    }
}
