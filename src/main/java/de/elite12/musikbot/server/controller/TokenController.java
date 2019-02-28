package de.elite12.musikbot.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.services.UserService;

@Controller
@RequestMapping("/tokens")
@PreAuthorize("hasRole('admin')")
public class TokenController {

    @Autowired

    private UserService userservice;
    
    @GetMapping
    public String getAction(Model model) {
        model.addAttribute("token", userservice.getExternalToken(
                ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser()));

        return "token";
    }
    
    @PostMapping
    public String postAction(Model model) {
        userservice.resetExternalToken(
                ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser());
        model.addAttribute("token", userservice.getExternalToken(
                ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser()));
        return "token";
    }
}
