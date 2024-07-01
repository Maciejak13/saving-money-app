package com.example.zpiexpensemanger.Models;

import java.io.Serializable;

public class Income implements Serializable {
    public String incomeplus, date, id;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
