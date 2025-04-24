package com.bikedc.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LogGenerateRequest {
    private String date; 
    private String level; 

    public LocalDate getParsedDate() {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}