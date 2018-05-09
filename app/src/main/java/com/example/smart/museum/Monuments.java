package com.example.smart.museum;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Harish on 06-05-2018.
 */

public class Monuments {
    private String id, name, information;

    public Monuments(String id, String name, String information) {
        this.id = id;
        this.name = name;
        this.information = information;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }
    public Monuments() {

    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("Id", id);
        result.put("Name", name);
        result.put("Information", information);
        return result;
    }
}

