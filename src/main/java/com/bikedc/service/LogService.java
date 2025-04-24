package com.bikedc.service;

import org.springframework.core.io.Resource;
import java.io.IOException;

public interface LogService {
    String generateFilteredLog(String date, String level);
    String getLogStatus(String taskId);
    Resource getLogFile(String taskId) throws IOException;
}