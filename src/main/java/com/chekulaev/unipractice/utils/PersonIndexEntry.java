package com.chekulaev.unipractice.utils;

import java.util.Arrays;
import java.util.Objects;

public class PersonIndexEntry {
    private int id;
    private HashPair[] entryStrings;
    private Object[] entryObjects;

    public PersonIndexEntry(int id, HashPair[] entryStrings, Object[] entryObjects) {
        this.id = id;
        this.entryStrings = entryStrings;
        this.entryObjects = entryObjects;
    }

    public int getId() {
        return id;
    }

    public HashPair[] getEntryStrings() {
        return entryStrings;
    }

    public Object[] getEntryObjects() {
        return entryObjects;
    }

    /**
     * For duplicate set: records are equal if their id's are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {return true; }
        if (obj instanceof PersonIndexEntry) {
            PersonIndexEntry other = (PersonIndexEntry) obj;
            if (this.id == ((PersonIndexEntry)obj).getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PersonIndexEntry{" +
                "id=" + id +
                ", entryStrings=" + Arrays.toString(entryStrings) +
                ", entryObjects=" + Arrays.toString(entryObjects) +
                '}';
    }
}
