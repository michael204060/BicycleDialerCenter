package com.bikedc.service.impl;

import com.bikedc.service.LogService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class LogServiceImpl implements LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);
    private static final int PROCESSING_DELAY_SECONDS = 20; 

    private final ConcurrentHashMap<String, String> taskStatus = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> taskFiles = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public String generateFilteredLog(String date, String level) {
        String taskId = UUID.randomUUID().toString();
        taskStatus.put(taskId, "PENDING");

        
        executor.schedule(() -> {
            taskStatus.put(taskId, "PROCESSING");

            
            executor.schedule(() -> {
                try {
                    processLogFile(taskId, date, level);
                    taskStatus.put(taskId, "COMPLETED");
                } catch (Exception e) {
                    taskStatus.put(taskId, "FAILED: " + e.getMessage());
                    logger.error("Log processing failed", e);
                }
            }, PROCESSING_DELAY_SECONDS, TimeUnit.SECONDS);

        }, 0, TimeUnit.SECONDS);

        return taskId;
    }

    private void processLogFile(String taskId, String date, String level) throws IOException {
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
        taskFiles.put(taskId, outputPath.toString());
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

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}