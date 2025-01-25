package com.example.brainalyzer;

import java.io.Serializable;

public class Task implements Serializable {
    private String name;
    private String dueDate;
    private int difficulty;
    private String type;

    public Task(String name, String dueDate, int difficulty, String type) {
        this.name = name;
        this.dueDate = dueDate;
        this.difficulty = difficulty;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDueDate() {
        return dueDate;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getType() {
        return type;
    }
}
