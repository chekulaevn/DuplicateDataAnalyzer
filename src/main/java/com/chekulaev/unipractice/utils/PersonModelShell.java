package com.chekulaev.unipractice.utils;

import java.util.LinkedList;
import java.util.List;

public class PersonModelShell {
    private int id;
    private List<String> stringFields = new LinkedList<>();
    private List<Object> otherFields = new LinkedList<>();

    public PersonModelShell(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public List<String> getStringFields() {
        return stringFields;
    }

    public List<Object> getOtherFields() {
        return otherFields;
    }

    @Override
    public String toString() {
        return ("{" + "PersonModelShell: id = " + id + " [StringFields]: '" + stringFields +
                "' [OtherFields]: '" + otherFields.toString() + " '}");
    }
}
