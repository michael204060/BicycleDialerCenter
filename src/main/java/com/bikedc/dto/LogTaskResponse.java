package com.bikedc.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogTaskResponse {
    private String taskId;
    private String status;
    private String filePath;

    public LogTaskResponse() {
    }

    public LogTaskResponse(String taskId, String status) {
        this.taskId = taskId;
        this.status = status;
    }

    public LogTaskResponse(String taskId, String status, String filePath) {
        this.taskId = taskId;
        this.status = status;
        this.filePath = filePath;
    }

    // Геттеры
    public String getTaskId() {
        return taskId;
    }

    public String getStatus() {
        return status;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "LogTaskResponse{" +
                "taskId='" + taskId + '\'' +
                ", status='" + status + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}