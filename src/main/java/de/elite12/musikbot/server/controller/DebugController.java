package de.elite12.musikbot.server.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/debug/")
@PreAuthorize("hasRole('admin')")
public class DebugController{
    
    @GetMapping
    public String doGet() {
    	return "debug";
    }
}
