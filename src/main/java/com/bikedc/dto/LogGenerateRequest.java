package com.bikedc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LogGenerateRequest {
    @NotNull(message = "Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in yyyy-MM-dd format")
    private String date;

    @NotNull(message = "Level is required")
    @Pattern(regexp = "INFO|DEBUG|ERROR", message = "Level must be one of: INFO, DEBUG, ERROR")
    private String level;

    @JsonIgnore
    public LocalDate getParsedDate() throws DateTimeParseException {
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