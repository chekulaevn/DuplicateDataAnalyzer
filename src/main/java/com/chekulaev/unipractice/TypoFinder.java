package com.chekulaev.unipractice;

import com.chekulaev.unipractice.utils.FinderReport;
import com.chekulaev.unipractice.utils.FinderInput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

@PropertySource(value = "actualColumnNames.properties", encoding="UTF-8")
public class TypoFinder {
    private JdbcTemplate jdbcTemplate;
    private String tableName;

    @Value("#{'${column.names}'.split(',')}")
    private String[] actualColumnNames;
    private int actualColumnNumber = 8;

    public TypoFinder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public FinderReport find(String tableName, FinderInput input) {
        this.tableName = tableName;
        return new FinderReport(5,0);
    }
}
