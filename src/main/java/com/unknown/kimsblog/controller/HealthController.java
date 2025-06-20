package com.unknown.kimsblog.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HealthController {
    @GetMapping("/")
    public String home() {
        return "OK";
    }
}
