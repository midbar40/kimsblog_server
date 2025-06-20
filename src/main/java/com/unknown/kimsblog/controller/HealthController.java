package com.unknown.kimsblog.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

@RestController
public class HealthController {
    
    @GetMapping("/")
    public String root() {
        return "Kim's Blog Server is running!";
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", new Date().toString());
        return status;
    }
}