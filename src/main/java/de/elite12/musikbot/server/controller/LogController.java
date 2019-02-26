package de.elite12.musikbot.server.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/log")
@PreAuthorize("hasRole('admin')")
public class LogController {
    
    @GetMapping
    public String getAction() {
        return "log";
    }
}
