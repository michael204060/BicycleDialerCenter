package com.bikedc.controller;

import com.bikedc.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Value("${logging.file.path}")
    private String logPath;


    @GetMapping("/{date}")
    @Operation(summary = "Get log file by date")
    public ResponseEntity<Resource> getLogFile(@PathVariable String date) throws IOException {
        LocalDate logDate;
        try {
            logDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            if (logDate.isAfter(LocalDate.now())) {
                throw new ValidationException("Date cannot be in the future");
            }
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Please use yyyy-MM-dd");
        }

        String logFileName = String.format("%s/app-%s.log", logPath, date);
        Path path = Paths.get(logFileName);

        if (!Files.exists(path)) {
            throw new ValidationException("No logs available for " + date);
        }

        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"app-logs-" + date + ".log\"")
                .body(resource);
    }
}