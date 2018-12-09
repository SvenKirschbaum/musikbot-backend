package de.elite12.musikbot.server.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.elite12.musikbot.server.core.Controller;
import de.elite12.musikbot.server.model.User;
import de.elite12.musikbot.server.model.UserMessage;
import de.elite12.musikbot.server.util.PlaylistImporter;
import de.elite12.musikbot.server.util.SessionHelper;
import de.elite12.musikbot.server.util.PlaylistImporter.Playlist;
import de.elite12.musikbot.shared.SongIDParser;
import de.elite12.musikbot.shared.SongIDParser.SpotifyPlaylistHelper;

public class PlaylistServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 64953031560116883L;
    private Controller control;

    public PlaylistServlet(Controller ctr) {
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
            this.getControl().addmessage(req, "Hinweis: Es werden nur die ersten 50 Videos einer Playlist angezeigt!",
                    UserMessage.TYPE_NOTIFY);
            req.setAttribute("control", this.getControl());
            req.getRequestDispatcher("/importer.jsp").forward(req, resp);
            return;
        }
        resp.sendRedirect("/");
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	User u = SessionHelper.getUserFromSession(req.getSession());
        if (u != null && u.isAdmin()) {
            req.setAttribute("worked", Boolean.valueOf(true));
            req.setAttribute("control", this.getControl());

            if (req.getParameter("playlist") == null) {
                Logger.getLogger(PlaylistServlet.class).warn("PlaylistServlet without pid");
                resp.sendError(400);
                return;
            }

            String pid = SongIDParser.getPID(req.getParameter("playlist"));
            SpotifyPlaylistHelper spid = SongIDParser.getSPID(req.getParameter("playlist"));
            String said = SongIDParser.getSAID(req.getParameter("playlist"));

            if (pid == null && spid == null && said == null) {
                Logger.getLogger(PlaylistServlet.class).warn("Invalid Playlist Link");
                resp.sendError(400);
                return;
            }

            Playlist p = spid == null
                    ? said == null ? PlaylistImporter.getyoutubePlaylist(pid) : PlaylistImporter.getspotifyAlbum(said)
                    : PlaylistImporter.getspotifyPlaylist(spid.user, spid.pid);

            if (req.getParameter("pimport") == null) {
                req.setAttribute("playlist", p);
                req.getRequestDispatcher("/importer-list.jsp").forward(req, resp);
                return;
            } else {
                String[] val = req.getParameterValues("pimport");

                for (String v : val) {
                    try {
                        this.getControl().addSong(p.entrys[Integer.parseInt(v)].link,
                                u, null);
                    } catch (Exception e) {
                        Logger.getLogger(PlaylistServlet.class).error("pimport hat falsches Format: ", e);
                        return;
                    }
                }
                Logger.getLogger(PlaylistServlet.class).info("Playlist Importiert: " + req.getParameter("playlist")
                        + " by User: " + u);
                resp.sendRedirect("/");
                return;
            }
        }
        resp.sendRedirect("/import/");
    }

    private void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
        throw new java.io.NotSerializableException(getClass().getName());
    }
}
