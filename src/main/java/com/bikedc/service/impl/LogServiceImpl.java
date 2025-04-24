// com.bikedc.service.impl/LogServiceImpl.java
package com.bikedc.service.impl;

import com.bikedc.service.LogService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LogServiceImpl implements LogService {
    private final ConcurrentHashMap<String, String> taskStatus = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> taskFiles = new ConcurrentHashMap<>();

    @Override
    @Async
    public void generateLogFile(String taskId) {
        try {
            taskStatus.put(taskId, "PROCESSING");
            // Имитация долгой операции
            Thread.sleep(3000);

            Path path = Paths.get("logs/generated-" + taskId + ".log");
            Files.write(path, ("Generated log content for task " + taskId).getBytes());

            taskFiles.put(taskId, path.toString());
            taskStatus.put(taskId, "COMPLETED");
        } catch (Exception e) {
            taskStatus.put(taskId, "FAILED");
        }
    }

    @Override
    public String getLogStatus(String taskId) {
        return taskStatus.getOrDefault(taskId, "UNKNOWN");
    }

    @Override
    public Resource getLogFile(String taskId) throws IOException {
        String filePath = taskFiles.get(taskId);
        if (filePath == null) {
            throw new IOException("File not found for task " + taskId);
        }
        Path path = Paths.get(filePath);
        return new UrlResource(path.toUri());
    }
}