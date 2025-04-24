package com.bikedc.service.impl;

import com.bikedc.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class LogServiceImpl implements LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);

    private final ConcurrentHashMap<String, String> taskStatus = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> taskFiles = new ConcurrentHashMap<>();

    @Override
    public String generateFilteredLog(String date, String level) {
        String taskId = UUID.randomUUID().toString();
        taskStatus.put(taskId, "PENDING");
        asyncGenerateLog(taskId, date, level);
        return taskId;
    }

    @Async
    public void asyncGenerateLog(String taskId, String date, String level) {
        try {
            taskStatus.put(taskId, "PROCESSING");

            Path sourcePath = Paths.get("logs/app-" + date + ".log");
            if (!Files.exists(sourcePath)) {
                throw new IOException("Log file not found for date: " + date);
            }

            List<String> filteredLines = Files.lines(sourcePath)
                    .filter(line -> line.contains(" " + level + " "))
                    .collect(Collectors.toList());

            Path outputPath = Paths.get("logs/filtered-" + taskId + ".log");

            String header = String.format(
                    "=== Filtered Logs ===\n" +
                            "Original Date: %s\n" +
                            "Log Level: %s\n" +
                            "Entries Count: %d\n\n",
                    date, level, filteredLines.size()
            );

            Files.write(outputPath, header.getBytes());
            Files.write(outputPath, filteredLines, StandardOpenOption.APPEND);

            taskStatus.put(taskId, "COMPLETED");
            taskFiles.put(taskId, outputPath.toString());

        } catch (Exception e) {
            String errorMessage = "Log generation error: " + e.getMessage();
            taskStatus.put(taskId, "FAILED: " + errorMessage);
            logger.error(errorMessage, e);
        }
    }

    @Override
    public String getLogStatus(String taskId) {
        return taskStatus.getOrDefault(taskId, "NOT_FOUND");
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