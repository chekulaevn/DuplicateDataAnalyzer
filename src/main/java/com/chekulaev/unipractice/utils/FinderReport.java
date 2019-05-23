package com.chekulaev.unipractice.utils;

public class FinderReport {
    private int processedRecordsNumber;
    private int duplicateCount;
    private double timeSpent;

    public FinderReport(int processedRecordsNumber, int duplicateCount, double timeSpent) {
        this.processedRecordsNumber = processedRecordsNumber;
        this.duplicateCount = duplicateCount;
        this.timeSpent = timeSpent;
    }

    public int getProcessedRecordsNumber() {
        return processedRecordsNumber;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public double getTimeSpent() {
        return timeSpent;
    }
}