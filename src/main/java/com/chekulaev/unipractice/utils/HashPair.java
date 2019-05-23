package com.chekulaev.unipractice.utils;

public class HashPair {
    private Long hash;
    private String value;

    public HashPair(Long hash, String value) {
        this.hash = hash;
        this.value = value;
    }

    public Long getHash() {
        return hash;
    }

    public void setHash(Long hash) {
        this.hash = hash;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Long.toBinaryString(hash) + " - " + value;
    }
}
