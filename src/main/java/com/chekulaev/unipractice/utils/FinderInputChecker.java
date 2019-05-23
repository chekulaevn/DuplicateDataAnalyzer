package com.chekulaev.unipractice.utils;

import com.chekulaev.unipractice.exceptions.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;

public class FinderInputChecker {
    private JdbcTemplate jdbcTemplate;
    private String tableName;

    public FinderInputChecker(JdbcTemplate jdbcTemplate, String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
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

    private void checkColumns(String[] requestedColumns) {
        for (String requestedColumn : requestedColumns) {
            try {
                jdbcTemplate.queryForObject("SELECT " + requestedColumn + " FROM " + tableName +
                        " WHERE id = 1", (rs, rowNum) -> null);
            } catch (BadSqlGrammarException e) {
                throw new ColumnNotExistException("Column '" + requestedColumn + "', requested" +
                        " for analysis, does not exist in the database.");
            } catch (EmptyResultDataAccessException e) {
                throw new TableIsEmptyException("Table is empty.");
            }
        }
    }

    public void check(String[] requestedColumns) {
        if (requestedColumns == null || requestedColumns.length == 0) {
            throw new InvalidFinderInputException("Input is empty.");
        }
        for (String column : requestedColumns) {
            if (column == null || column.equals("") || column.equals(tableName) ||
                    Character.isDigit(column.charAt(0))) {
                throw new InvalidFinderInputException("Input contains illegal string.");
            } else if (column.equals("id")) {
                throw new InvalidFinderInputException("Don't use 'id' column in analysis input.");
            }
        }

        checkConnection();
        checkColumns(requestedColumns);
    }
}