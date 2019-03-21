package com.chekulaev.unipractice.utils;

import java.util.Arrays;

public class FinderInput {
    private final boolean[] fixedColumnsFlagArray;
    private final String[] additionalColumns;
    private final String selectQuery;

    public FinderInput(boolean[] fixedColumnsFlagArray, String[] additionalColumns, String selectQuery) {
        this.fixedColumnsFlagArray = fixedColumnsFlagArray;
        this.additionalColumns = additionalColumns;
        this.selectQuery = selectQuery;
    }

    public boolean[] getFixedColumnsFlagArray() {
        return Arrays.copyOf(fixedColumnsFlagArray, fixedColumnsFlagArray.length);
    }

    public String[] getAdditionalColumns() {
        return Arrays.copyOf(additionalColumns, additionalColumns.length);
    }

    public String getSelectQuery() {
        return selectQuery;
    }
}
