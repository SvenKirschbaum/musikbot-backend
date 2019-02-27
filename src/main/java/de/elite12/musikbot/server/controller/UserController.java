package de.elite12.musikbot.server.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.services.UserService;
import de.elite12.musikbot.server.util.NotFoundException;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userservice;
	
	@Autowired
	private SongRepository songs;
	
	@GetMapping("{user}")
    public void getAction(@PathVariable String user, Model model) {
		Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User u = null;
		boolean admin = false;
		boolean self = false;
		boolean guest = false;
		if(p instanceof UserPrincipal) {
			UserPrincipal t = (UserPrincipal) p;
			u=t.getUser();
			admin = u.isAdmin();
		}
        User target = userservice.findUserbyName(user);
        if(target != null) {
        	if(target.getId() == u.getId()) self = true;
        }
        
        try {
        	String name = UUID.fromString(user).toString();
        	target = new User();
        	target.setName(name);
            guest = true;
        } catch (IllegalArgumentException e) {

        }
        
        if(target==null) {
        	throw new NotFoundException();
        }
		
	            ArrayList<DataEntry> userinfo = new ArrayList<>();
                if (!guest) {
                    userinfo.add(
                            new DataEntry("ID:", target.getId().toString(), false, "id"));
                }
                userinfo.add(new DataEntry("Username:", target.getName(), admin&&(!guest), "username"));
                if (!guest && (self || admin)) {
                    userinfo.add(new DataEntry("Email:", target.getEmail(), true, "email"));
                    userinfo.add(new DataEntry("Passwort:", "****", true, "password"));
                    userinfo.add(new DataEntry("Admin: ", target.isAdmin() ? "Ja" : "Nein", admin && !guest, "admin"));
                }
                
                Long songcount;
                if(guest) {
                	songcount = songs.countByGuestAuthor(target.getName());
                }
                else {
                	songcount = songs.countByUserAuthor(target);
                }
                
                Long skippedcount;
                if(guest) {
                	skippedcount = songs.countByGuestAuthorAndSkipped(target.getName(),true);
                }
                else {
                	skippedcount = songs.countByUserAuthorAndSkipped(target, true);
                }
                
                        PreparedStatement stmnt3 = c.prepareStatement(
                                "SELECT SONG_NAME,SONG_LINK,COUNT(*) AS anzahl FROM PLAYLIST WHERE AUTOR = ? GROUP BY SONG_LINK ORDER BY COUNT(*) DESC LIMIT 10");
                        PreparedStatement stmnt4 = c.prepareStatement(
                                "SELECT SONG_NAME,SONG_LINK,COUNT(*) AS anzahl FROM PLAYLIST WHERE AUTOR = ? AND SONG_SKIPPED = TRUE GROUP BY SONG_LINK ORDER BY COUNT(*) DESC LIMIT 10");
                        PreparedStatement stmnt5 = c.prepareStatement(
                                "SELECT SONG_NAME,SONG_LINK,SONG_ID FROM PLAYLIST WHERE AUTOR = ? ORDER BY SONG_ID DESC LIMIT 10");
                        PreparedStatement stmnt6 = c.prepareStatement("SELECT LASTSEEN FROM USER WHERE ID = ?");
                ) {
                	if (!guest) {
	                	stmnt6.setInt(1, user.getId());
	                    ResultSet r6 = stmnt6.executeQuery();
	                    r6.next();
	                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	                    userinfo.add(new DataEntry("Zuletzt aktiv:", f.format(new Date(r6.getLong(1) * 1000L)), false, ""));
                	}

                    stmnt.setString(1, user.getName());
                    ResultSet r = stmnt.executeQuery();
                    r.next();
                    userinfo.add(new DataEntry("Wünsche:", ((Integer)r.getInt(1)).toString(), false, ""));
                    stmnt2.setString(1, user.getName());
                    ResultSet r2 = stmnt2.executeQuery();
                    r2.next();
                    userinfo.add(new DataEntry("Davon übersprungen:", ((Integer)r2.getInt(1)).toString(), false, ""));
                    stmnt3.setString(1, user.getName());
                    ResultSet r3 = stmnt3.executeQuery();
                    ArrayList<TopEntry> list = new ArrayList<>(10);
                    while (r3.next()) {
                        list.add(new TopEntry(r3.getString(1), r3.getString(2), r3.getInt(3)));
                    }
                    req.setAttribute("toplist", list);
                    stmnt4.setString(1, user.getName());
                    ResultSet r4 = stmnt4.executeQuery();
                    list = new ArrayList<>(10);
                    while (r4.next()) {
                        list.add(new TopEntry(r4.getString(1), r4.getString(2), r4.getInt(3)));
                    }
                    req.setAttribute("topskipped", list);
                    stmnt5.setString(1, user.getName());
                    ResultSet r5 = stmnt5.executeQuery();
                    list = new ArrayList<>(10);
                    while (r5.next()) {
                        list.add(new TopEntry(r5.getString(1), r5.getString(2), r5.getInt(3)));
                    }
                    req.setAttribute("recent", list);
                } catch (SQLException e) {
                    Logger.getLogger(UserServlet.class).error("Error reading Userdata", e);
                    resp.sendError(500);
                    return;
                }

                req.setAttribute("userinfo", userinfo.toArray(new DataEntry[0]));
                req.getRequestDispatcher("/user.jsp").forward(req, resp);
                return;
            } else {
                resp.sendError(404);
            }
        } else {
            resp.sendError(404);
        }
    }

    private static class DataEntry {
        public DataEntry(String name, String value, boolean change, String urlname) {
            this.name = name;
            this.value = value;
            this.changeable = change;
            this.urlname = urlname;
        }

        public String name;
        public String urlname;
        public String value;
        public boolean changeable;
    }
}
