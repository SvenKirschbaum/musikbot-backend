package de.elite12.musikbot.server.api;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/log")
@PreAuthorize("hasRole('admin')")
public class Log {
    
    final Logger logger = LoggerFactory.getLogger(Log.class);
    
    @GetMapping
    @ApiOperation(value = "Gets the content of the logfile", notes = "Requires Admin Permissions.", produces = MediaType.TEXT_PLAIN_VALUE)
    public void doGet(HttpServletResponse resp) {
        resp.setContentType("text/plain;charset=UTF-8");

        try (
                PrintWriter p = resp.getWriter();
                FileInputStream fis = new FileInputStream("log.txt");
                InputStreamReader fr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader f = new BufferedReader(fr)
        ) {
            String s;
            while ((s = f.readLine()) != null) {
                p.println(s);
            }
        } catch (IOException e) {
            logger.error("ERROR reading Logfile", e);
        }
    }
}
