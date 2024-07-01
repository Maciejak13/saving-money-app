package com.example.zpiexpensemanger.Models;

import java.io.Serializable;
public class Goal implements Serializable{
    public String goalsplus, date, goaltitle, id;

    public int goalImageResourceId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
