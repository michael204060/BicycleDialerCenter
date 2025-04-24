package com.bikedc.controller;

import com.bikedc.dto.LogGenerateRequest;
import com.bikedc.dto.LogTaskResponse;
import com.bikedc.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate filtered log file")
    public ResponseEntity<LogTaskResponse> generateLogFile(
            @RequestBody LogGenerateRequest request) {

        String taskId = logService.generateFilteredLog(request.getDate(), request.getLevel());
        return ResponseEntity.ok(new LogTaskResponse(taskId, "PENDING"));
    }

    @GetMapping("/status/{taskId}")
    @Operation(summary = "Get log generation status")
    public ResponseEntity<LogTaskResponse> getLogStatus(@PathVariable String taskId) {
        String status = logService.getLogStatus(taskId);
        return ResponseEntity.ok(new LogTaskResponse(taskId, status));
    }

    @GetMapping("/download/{taskId}")
    @Operation(summary = "Download generated log file")
    public ResponseEntity<Resource> downloadLogFile(@PathVariable String taskId) throws IOException {
        Resource resource = logService.getLogFile(taskId);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"filtered-logs-" + taskId + ".log\"")
                .body(resource);
    }
}