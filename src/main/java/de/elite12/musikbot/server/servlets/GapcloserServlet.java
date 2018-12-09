package de.elite12.musikbot.server.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.elite12.musikbot.server.core.Controller;
import de.elite12.musikbot.server.core.Gapcloser.Mode;
import de.elite12.musikbot.server.model.User;
import de.elite12.musikbot.server.util.SessionHelper;
import de.elite12.musikbot.shared.SongIDParser;

public class GapcloserServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -303042558614330805L;
	private Controller control;
    

    
    public GapcloserServlet(Controller ctr) {
        this.control = ctr;
    }

    private Controller getControl() {
        return control;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	User u = SessionHelper.getUserFromSession(req.getSession());
        if (u != null && u.isAdmin()) {
            req.setAttribute("worked", Boolean.valueOf(true));
            req.setAttribute("mode", this.getControl().getGapcloser().getMode());
            req.setAttribute("playlist", this.getControl().getGapcloser().getPlaylist());
            req.setAttribute("control", this.getControl());
            req.getRequestDispatcher("/gapcloser.jsp").forward(req, resp);
            return;
        }
        resp.sendRedirect("/");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	User u = SessionHelper.getUserFromSession(req.getSession());
        if (u != null && u.isAdmin()
                && req.getParameter("mode") != null) {
            req.setAttribute("worked", Boolean.valueOf(true));
            switch (req.getParameter("mode")) {
                case "off": {
                	this.getControl().getGapcloser().setMode(Mode.OFF);
                    break;
                }
                case "zufall100": {
                	this.getControl().getGapcloser().setMode(Mode.RANDOM100);
                    break;
                }
                case "zufall": {
                	this.getControl().getGapcloser().setMode(Mode.RANDOM);
                    break;
                }
                case "playlist": {
                	this.getControl().getGapcloser().setMode(Mode.PLAYLIST);
                    break;
                }
            }
            if (SongIDParser.getPID(req.getParameter("playlist")) != null
                    || SongIDParser.getSPID(req.getParameter("playlist")) != null) {
                String link = SongIDParser.getSPID(req.getParameter("playlist")) == null
                        ? "https://www.youtube.com/playlist?list=" + SongIDParser.getPID(req.getParameter("playlist"))
                        : SongIDParser.getSPID(req.getParameter("playlist")).toString();
                this.getControl().getGapcloser().setPlaylist(link);
            }

            Logger.getLogger(this.getClass())
                    .info("Gapcloser zu " + this.getControl().getGapcloser().getMode() + " geändert (Playlist: " + this.getControl().getGapcloser().getPlaylist() + ")");
        }
        resp.sendRedirect("/gapcloser/");
    }
}
