package com.chekulaev.unipractice.utils;

import java.util.Comparator;

public class SimilarRowsComparator implements Comparator<String[]> {
    private boolean stringsSimilar(String s1, String s2) {
        return s1.equals(s2);
    }

    /**
     * Guarantied row structure:
     * first 8 elements correspond to actualColumnNames
     * others - to additional columns
     */
    @Override
    public int compare(String[] row1, String[] row2) {
        for (int i = 0; i < row1.length; i++) {
            if (row1[i] == null) {
                continue;
            }
            if (row1[i].equals(row2[i])) {
                continue;
            }
            if (i==1 || i==2 || i==3 || i==5) {
                if (stringsSimilar(row1[i],row2[i])) {
                    continue;
                }
            }
            return row1[i].compareTo(row2[i]);
        }
        return 0;
    }
}