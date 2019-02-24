package de.elite12.musikbot.server.controller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.elite12.musikbot.server.core.Controller;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.util.SessionHelper;

public class LogServlet extends HttpServlet {
    
    /**
     *
     */
    private static final long serialVersionUID = 7367249600378627774L;
    @SuppressWarnings("unused")
    private Controller ctr;
    
    public LogServlet(Controller c) {
        this.ctr = c;
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	User u = SessionHelper.getUserFromSession(req.getSession());
        if (u != null && u.isAdmin()) {
            if (req.getParameter("log") != null && req.getParameter("log").equalsIgnoreCase("log")) {
                doPost(req, resp);
                return;
            }
            req.setAttribute("worked", Boolean.valueOf(true));
            req.setAttribute("control", this.ctr);
            req.getRequestDispatcher("/log.jsp").forward(req, resp);
        }
    }
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	User u = SessionHelper.getUserFromSession(req.getSession());
        if (u != null && u.isAdmin()) {
            req.setAttribute("worked", Boolean.valueOf(true));
            resp.setContentType("text/plain;charset=UTF-8");

            try (
                    PrintWriter p = resp.getWriter();
                    FileInputStream fis = new FileInputStream("log.txt");
                    InputStreamReader fr = new InputStreamReader(fis, "UTF-8");
                    BufferedReader f = new BufferedReader(fr);
            ) {
                String s;
                while ((s = f.readLine()) != null) {
                    p.println(s);
                }
            } catch (IOException e) {
                Logger.getLogger(LogServlet.class).error("ERROR reading Logfile", e);
            }
            resp.setStatus(200);
        }
    }
    
    private void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }
    
    private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
        throw new java.io.NotSerializableException(getClass().getName());
    }
}
