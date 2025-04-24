// com.bikedc.service/LogService.java
package com.bikedc.service;

import org.springframework.core.io.Resource;

import java.io.IOException;

public interface LogService {
    void generateLogFile(String taskId);
    String getLogStatus(String taskId);
    Resource getLogFile(String taskId) throws IOException;
}