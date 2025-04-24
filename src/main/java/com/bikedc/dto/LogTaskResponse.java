package com.bikedc.dto;

public class LogTaskResponse {
    private String taskId;
    private String status;

    public LogTaskResponse(String taskId, String status) {
        this.taskId = taskId;
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getStatus() {
        return status;
    }
}