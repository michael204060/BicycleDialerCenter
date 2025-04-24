package com.bikedc.service.impl;

import com.bikedc.service.LogService;
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
import java.util.stream.Collectors;

@Service
public class LogServiceImpl implements LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);


    @Override
    public String generateFilteredLog(String date, String level) {
        String taskId = UUID.randomUUID().toString();
        try {
            generateLog(taskId, date, level);
            return taskId;
        } catch (IOException e) {
            logger.error("Error generating log: ", e);
            return null; 
        }
    }

    private void generateLog(String taskId, String date, String level) throws IOException {
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
    }


    @Override
    public String getLogStatus(String taskId) {
        try {
            if (Paths.get("logs/filtered-" + taskId + ".log").toFile().exists()) {
                return "COMPLETED";
            } else {
                return "FAILED"; 
            }
        } catch (Exception e) {
            return "FAILED";
        }
    }

    @Override
    public Resource getLogFile(String taskId) throws IOException {
        Path path = Paths.get("logs/filtered-" + taskId + ".log");
        if (!Files.exists(path)) {
            throw new IOException("File not found for task " + taskId);
        }
        return new UrlResource(path.toUri());
    }
}