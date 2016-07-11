package de.elite12.musikbot.server;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;

import de.elite12.musikbot.shared.Util;

public class PlaylistImporter extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 64953031560116883L;
	private Controller control;

	public PlaylistImporter(Controller ctr) {
		this.control = ctr;
	}

	private Controller getControl() {
		return control;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getSession().getAttribute("user") != null
				&& ((User) req.getSession().getAttribute("user")).isAdmin()) {
			req.setAttribute("worked", Boolean.valueOf(true));
			this.getControl()
					.addmessage(
							req,
							"Hinweis: Es werden nur die ersten 50 Videos einer Playlist angezeigt!",
							UserMessage.TYPE_NOTIFY);
			req.setAttribute("control", this.getControl());
			req.getRequestDispatcher("/importer.jsp").forward(req, resp);
			return;
		}
		resp.sendRedirect("/");
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getSession().getAttribute("user") != null
				&& ((User) req.getSession().getAttribute("user")).isAdmin()
				&& req.getParameter("playlist") != null) {
			req.setAttribute("worked", Boolean.valueOf(true));
			req.setAttribute("control", this.getControl());
			String pid = Util.getPID(req.getParameter("playlist"));
			String said = Util.getSAID(req.getParameter("playlist"));
			if (pid != null) {
				try {
					Logger.getLogger(PlaylistImporter.class).debug(
							"Querying Youtube...");
					List<PlaylistItem> list = this.getControl().getYouTube().playlistItems().list("snippet,status").setKey(Controller.key).setPlaylistId(pid).setMaxResults(50L).setFields("items/snippet/title,items/snippet/resourceId/videoId,items/snippet/position").execute().getItems();
					if(list == null) {
						throw new IOException("Playlist not available");
					}
					Playlist playlistmeta = this.getControl().getYouTube().playlists().list("snippet").setKey(Controller.key).setId(pid).setFields("items/snippet/title,items/id").execute().getItems().get(0);
					req.setAttribute("playlist", list);
					req.setAttribute("playlistmeta", playlistmeta);
					if (req.getParameter("pimport") == null) {
						req.getRequestDispatcher("/importer-list.jsp").forward(
								req, resp);
					} else {
						String[] val = req.getParameterValues("pimport");
						HashMap<Integer, PlaylistItem> map = new HashMap<>();
						for (PlaylistItem e : list) {
							map.put(e.getSnippet().getPosition().intValue(), e);
						}
						for (String v : val) {
							this.getControl().addSong(
									"https://www.youtube.com/watch?v="+map.get(Integer.parseInt(v)).getSnippet().getResourceId().getVideoId(),
									(User) req.getSession().getAttribute(
											"user"),null);
						}
						Logger.getLogger(PlaylistImporter.class).info(
								"Playlist Importiert: "
										+ req.getParameter("playlist")
										+ " by User: "
										+ req.getSession().getAttribute(
												"playlist"));
						resp.sendRedirect("/");
					}
					return;
				} catch (IOException e) {
					Logger.getLogger(PlaylistImporter.class).error(
							"Error loading Playlist", e);
				}
			} else if (said != null) {

			} else {
				Logger.getLogger(PlaylistImporter.class).warn(
						"Error importing Playlist, wrong Link");
			}
		}
		resp.sendRedirect("/import/");
	}

	private void writeObject(java.io.ObjectOutputStream stream)
			throws java.io.IOException {
		throw new java.io.NotSerializableException(getClass().getName());
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws java.io.IOException, ClassNotFoundException {
		throw new java.io.NotSerializableException(getClass().getName());
	}
}
