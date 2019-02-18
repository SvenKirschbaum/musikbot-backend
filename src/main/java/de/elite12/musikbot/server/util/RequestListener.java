package de.elite12.musikbot.server.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import de.elite12.musikbot.server.core.Controller;
import de.elite12.musikbot.server.model.User;

public class RequestListener implements ServletRequestListener {

	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		ServletRequest sr = sre.getServletRequest();
		if(sr instanceof HttpServletRequest) {
			
			HttpServletRequest hsr = (HttpServletRequest) sr;
			User u = SessionHelper.getUserFromSession(hsr.getSession());
			
			if(u != null) {
				Logger.getLogger(RequestListener.class).debug("Update Lastsen Info for User: " + u);
		        try (
		                Connection c = Controller.getInstance().getDB();
		                PreparedStatement stmnt = c
		                        .prepareStatement("UPDATE USER SET LASTSEEN = UNIX_TIMESTAMP() WHERE ID = ?");
		        ) {
		            stmnt.setInt(1, u.getId());
		            stmnt.execute();
		        } catch (SQLException e) {
		            Logger.getLogger(RequestListener.class).error("SQLException", e);
		        }
			}
			else {
				Logger.getLogger(RequestListener.class).debug("Handling Guest...");
		        UUID gid = (UUID) hsr.getSession().getAttribute("guest_id");
		        if (gid == null) {
		            gid = UUID.randomUUID();
		            Logger.getLogger(RequestListener.class).info("New Guest: IP: " + hsr.getHeader("X-Real-IP") + " Assigned ID: " + gid);
		            hsr.getSession().setAttribute("guest_id", gid);
		        }
			}
		}
	}

}
