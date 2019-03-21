package com.chekulaev.unipractice.utils;

public class AnalysisReport {
    private double timeSpent;
    private int processedRecordCount;
    private int duplicateCount;
    private int erroneousRecordsCount;

    public AnalysisReport(double timeSpent, int processedRecordCount, int duplicateCount, int erroneousRecordCount) {
        this.timeSpent = timeSpent;
        this.processedRecordCount = processedRecordCount;
        this.duplicateCount = duplicateCount;
        this.erroneousRecordsCount = erroneousRecordCount;
    }

    public double getTimeSpent() {
        return timeSpent;
    }

    public int getProcessedRecordCount() {
        return processedRecordCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public int getErroneousRecordsCount() {
        return erroneousRecordsCount;
    }

    @Override
    public String toString() {
        return "AnalysisReport{" +
                "timeSpent=" + timeSpent +
                ", processedRecordCount=" + processedRecordCount +
                ", duplicateCount=" + duplicateCount +
                ", erroneousRecordsCount=" + erroneousRecordsCount +
                '}';
    }
}
