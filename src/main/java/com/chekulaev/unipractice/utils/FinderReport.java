package com.chekulaev.unipractice.utils;

public class FinderReport {
    private int processedRecordsNumber;
    private int foundRecordsNumber;

    public FinderReport(int processedRecordNumber, int foundRequestedRecordsNumber) {
        this.processedRecordsNumber = processedRecordNumber;
        this.foundRecordsNumber = foundRequestedRecordsNumber;
    }

    public int getProcessedRecordsNumber() {
        return processedRecordsNumber;
    }

    public int getFoundRecordsNumber() {
        return foundRecordsNumber;
    }
}