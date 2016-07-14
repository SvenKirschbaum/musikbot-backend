package de.elite12.musikbot.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.core.io.ClassPathResource;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.common.io.Closeables;
import com.wrapper.spotify.models.Track;

import de.elite12.musikbot.server.Gapcloser.Mode;
import de.elite12.musikbot.shared.Song;
import de.elite12.musikbot.shared.Util;

public class Controller {

    public static final String key = "AIzaSyAsDZP0xvd0cyr7JjyR7SlkFEJhFMHF2ik";
    public final String version;

    private Logger logger;
    private static Controller instance;
    private YouTube yt;

    private ConnectionListener connectionListener;
    private Server server;
    private org.hsqldb.Server hsqlServer;
    private Connection connection = null;
    private Userservice userservice;
    private String songtitle = "Kein Song";
    private String state = "Keine Verbindung zum BOT";
    private Thread t;
    private Gapcloser g;

    private static List<Integer> allowed = Arrays.asList(new Integer[] { 1, 10, 18, 20, 24, 30, 44 });

    private String songlink;

    public Controller() {
        ClassPathResource resource = new ClassPathResource("app.properties");
        Properties p = new Properties();
        InputStream in = null;
        try {
            in = resource.getInputStream();
            p.load(in);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            Closeables.closeQuietly(in);
        }
        this.version = p.getProperty("application.version");
        try {
            this.logger = Logger.getLogger(Controller.class);
            logger.debug("Running on: " + System.getProperty("os.arch"));
            logger.info("Starting Musikbot Controller...");
            this.server = new Server(8080);
            logger.debug("Webserver initialised");
            this.hsqlServer = new org.hsqldb.Server();
            logger.debug("SQL Server initialised");

            HttpTransport transport = new NetHttpTransport();
            JsonFactory jsonfactory = new JacksonFactory();
            this.yt = new YouTube.Builder(transport, jsonfactory, new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("e12-musikbot").build();

            logger.debug("Youtube API Connection initialised");
            hsqlServer.setLogWriter(null);
            hsqlServer.setSilent(true);
            hsqlServer.setDatabaseName(0, "xdb");
            hsqlServer.setDatabasePath(0, "file:musikbotdb");
            hsqlServer.start();

            try {
                Class.forName("org.hsqldb.jdbcDriver");
                this.connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/xdb", "sa",
                        getSecurePassword());
                this.userservice = new Userservice(this);
                logger.debug("Userservice initialised");
            } catch (ClassNotFoundException | SQLException e1) {
                logger.fatal("Error initialising Database-Connection. System will Exit!", e1);
                Runtime.getRuntime().exit(-1);
            }

            this.connectionListener = new ConnectionListener(this);
            logger.debug("ConnectionListener initialised");

            logger.debug("Configuring jetty....");

            HandlerList hl = new HandlerList();
            String warUrlString = Controller.class.getClassLoader().getResource("de/elite12/musikbot/server/sites")
                    .toExternalForm();
            logger.debug(warUrlString);
            WebAppContext sites = new WebAppContext(warUrlString, "/");
            HashSessionManager sm = new HashSessionManager();
            sm.setLazyLoad(true);
            sm.setSavePeriod(60);
            try {
                sm.setStoreDirectory(new File("sessions"));
            } catch (IOException e1) {
                Logger.getLogger(this.getClass()).error("Unknown Error", e1);
            }
            sites.setSessionHandler(new SessionHandler(sm));
            sites.setErrorHandler(new MBErrorHandler(this));
            sites.addServlet(new ServletHolder(new Weblet(this)), "/");
            sites.addServlet(new ServletHolder(new PlaylistServlet(this)), "/import/");
            this.g = new Gapcloser(this);
            sites.addServlet(new ServletHolder(this.g), "/gapcloser/");
            sites.addServlet(new ServletHolder(new FeedGenerator(this)), "/feed/");
            sites.addServlet(new ServletHolder(new SongManagement(this)), "/songs/");
            sites.addServlet(new ServletHolder(new UserServlet(this)), "/user/");
            sites.addServlet(new ServletHolder(new OnlineServlet(this)), "/whoisonline/");
            sites.addServlet(new ServletHolder(new LogServlet(this)), "/log/");

            ResourceConfig config = new ResourceConfig().register(new RolesAllowedDynamicFeature())
                    .packages("de.elite12.musikbot.server.rest");
            ServletHolder sh = new ServletHolder(new ServletContainer(config));
            sh.setInitOrder(0);
            sites.addServlet(sh, "/api/*");

            ContextHandler ctx = new ContextHandler();
            ctx.setContextPath("/res");
            ResourceHandler res = new ResourceHandler();
            res.setResourceBase(Controller.class.getClassLoader().getResource("de/elite12/musikbot/server/resources")
                    .toExternalForm());
            res.setDirectoriesListed(false);
            res.setCacheControl("max-age=2592000,public");
            ctx.setHandler(res);
            hl.addHandler(ctx);

            hl.addHandler(sites);
            server.setHandler(hl);

            logger.debug("Configuration complete, starting...");
            try {
                server.start();
            } catch (Exception e) {
                logger.fatal("Error starting jetty, System will Exit!", e);
                Runtime.getRuntime().exit(-1);
            }
            Runtime.getRuntime().addShutdownHook(new Shutdown());
            logger.debug("Added Shutdown Hook");
            logger.info("Musikbot Controller started, System is ready");

            t = new Thread(new R());
        } catch (Exception e) {
            logger.fatal("Error starting Controller, System will Exit!", e);
            Runtime.getRuntime().exit(-1);
        }
        logger.debug("End of Controller");
    }

    public void start() {
        this.connectionListener.start();
        t.start();
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public Connection getDB() {
        return connection;
    }

    public static Controller getInstance() {
        return Controller.instance;
    }

    public YouTube getYouTube() {
        return yt;
    }

    public Song getnextSong() {
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        try {
            stmnt = this.getDB().prepareStatement(
                    "select * from PLAYLIST WHERE SONG_PLAYED = FALSE ORDER BY SONG_SORT ASC LIMIT 0,1");
            rs = stmnt.executeQuery();
            if (rs.next()) {
                stmnt.close();
                logger.debug("Found Song in Database");
                stmnt = this.getDB().prepareStatement(
                        "UPDATE PLAYLIST SET SONG_PLAYED = TRUE, SONG_PLAYED_AT = NOW() WHERE SONG_ID = ?");
                stmnt.setInt(1, rs.getInt("SONG_ID"));
                stmnt.execute();
                Song s = new Song(rs);
                if (s.gettype().equals("youtube")) {
                    try {
                        List<Video> list = this.getYouTube().videos().list("status,contentDetails").setKey(key)
                                .setId(Util.getVID(s.getLink()))
                                .setFields(
                                        "items/status/uploadStatus,items/status/privacyStatus,items/contentDetails/regionRestriction")
                                .execute().getItems();
                        if (list != null) {
                            if (!list.get(0).getStatus().getUploadStatus().equals("processed")
                                    || list.get(0).getStatus().getUploadStatus().equals("private")) {
                                throw new IOException("Video not available");
                            }
                            if (list.get(0).getContentDetails() != null) {
                                if (list.get(0).getContentDetails().getRegionRestriction() != null) {
                                    if (list.get(0).getContentDetails().getRegionRestriction().getBlocked() != null) {
                                        if (list.get(0).getContentDetails().getRegionRestriction().getBlocked()
                                                .contains("DE")) {
                                            throw new IOException("Video not available");
                                        }
                                    }
                                }
                            }
                        } else {
                            throw new IOException("Video not available");
                        }
                    } catch (IndexOutOfBoundsException | IOException e) {
                        logger.warn("Song seems to got deleted, skipping", e);
                        return this.getnextSong();
                    }
                }
                return s;
            } else {
                logger.debug("No further Song found");
                if (g.getMode() != Mode.OFF) {
                    return g.getnextSong();
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading next Song from Database", e);
            return null;
        } finally {
            try {
                rs.close();
            } catch (NullPointerException | SQLException e) {
                logger.error("Error closing ResultSet");
            }
            try {
                stmnt.close();
            } catch (NullPointerException | SQLException e) {
                logger.error("Error closing Statement");
            }
        }
    }

    public static void main(String[] args) {
        PropertyConfigurator.configureAndWatch("log4j.properties");
        (Controller.instance = new Controller()).start();
    }

    public String getSongtitle() {
        return songtitle;
    }

    public void setSongtitle(String songtitle) {
        this.songtitle = songtitle;
    }

    public String getSonglink() {
        return songlink;
    }

    public void setSonglink(String songlink) {
        this.songlink = songlink;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Userservice getUserservice() {
        return userservice;
    }

    public void markskipped() {
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        try {
            logger.debug("Marking last Song as skipped");
            stmnt = this.getDB().prepareStatement(
                    "select * from PLAYLIST WHERE SONG_PLAYED = TRUE ORDER BY SONG_ID DESC LIMIT 0,1");
            rs = stmnt.executeQuery();
            if (rs.next()) {
                PreparedStatement stmnt2 = null;
                try {
                    stmnt2 = this.getDB().prepareStatement(
                            "UPDATE PLAYLIST SET SONG_SKIPPED = TRUE WHERE SONG_ID = " + rs.getInt("SONG_ID"));
                    stmnt2.execute();
                } catch (SQLException e) {
                    logger.error("SQL Error marking last Song as skipped", e);
                } finally {
                    try {
                        stmnt2.close();
                    } catch (NullPointerException | SQLException e) {
                        logger.error("Error closing Statement2");
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("SQL Error marking last Song as skipped", e);
        } finally {
            try {
                rs.close();
            } catch (NullPointerException | SQLException e) {
                logger.error("Error closing ResultSet");
            }
            try {
                stmnt.close();
            } catch (NullPointerException | SQLException e) {
                logger.error("Error closing Statement");
            }
        }
    }

    public String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes("UTF-8"));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString(array[i] & 0xFF | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException | UnsupportedEncodingException e) {
            logger.error("Error calculating MD5", e);
        }
        return null;
    }

    private void shutdown() {
        logger.info("Shutting down Controller");
        try {
            logger.debug("Interrupting Listener Thread...");
            this.getConnectionListener().interrupt();
            if (this.getConnectionListener().getHandle() != null) {
                logger.debug("Closing Existing Connection...");
                this.getConnectionListener().getHandle().getsocket().close();
            }
            logger.debug("Closing Server Socket...");
            this.getConnectionListener().getSocket().close();
            logger.debug("Shutting down jetty...");
            this.server.stop();
            logger.debug("Shutting down SQL Server");
            this.hsqlServer.shutdown();
            logger.debug("Interrupting Main Thread...");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.fatal("Exception in Shutdown Routine", e);
        }
    }

    public Response addSong(String url, User user, String gid) {
        String VID = Util.getVID(url);
        String SID = Util.getSID(url);
        if (VID != null) {
            return this.addYSong(VID, user, gid);
        }
        if (SID != null) {
            return this.addSSong(SID, user, gid);
        }
        return Response.status(400).entity("URL ungültig").build();
    }

    private Response addSSong(String SID, User user, String gid) {
        logger.debug("Adding new Song to Playlist... :" + SID);
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        String notice = null;
        try {
            if (this.islocked(SID)) {
                if (user != null && user.isAdmin()) {
                    logger.debug("Song is locked, but User is Admin, creating Notice");
                    notice = "Hinweis: Dieser Song wurde gesperrt!";
                } else {
                    logger.debug("Song is locked, denying");
                    return Response.status(403).entity("Dieser Song wurde leider gesperrt!").build();
                }

            }
            stmnt = this.getDB().prepareStatement("select COUNT(*) from PLAYLIST WHERE SONG_PLAYED = FALSE");
            rs = stmnt.executeQuery();
            rs.next();
            if (rs.getInt(1) < 24) {
                rs.close();
                stmnt.close();
                stmnt = this.getDB()
                        .prepareStatement("select COUNT(*) from PLAYLIST WHERE SONG_PLAYED = FALSE AND SONG_LINK = ?");
                stmnt.setString(1, "http://open.spotify.com/track/" + SID);
                rs = stmnt.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    rs.close();
                    stmnt.close();
                    stmnt = this.getDB()
                            .prepareStatement("select COUNT(*) from PLAYLIST WHERE SONG_PLAYED = FALSE AND AUTOR = ?");
                    if (user != null) {
                        stmnt.setString(1, user.getName());
                    } else {
                        stmnt.setString(1, gid);
                    }
                    rs = stmnt.executeQuery();
                    rs.next();
                    if (rs.getInt(1) < 2 || user != null && user.isAdmin()) {
                        stmnt.close();
                        Track track = Util.getTrack(SID);
                        if (track != null) {
                            if (track.getDuration() > 390000 && !(user != null && user.isAdmin())) {
                                return Response.status(409).entity("Dieses Video ist leider zu lang!").build();
                            }
                        } else {
                            logger.debug("Song not available");
                            return Response.status(404).entity("URL ungültig").build();
                        }
                        stmnt = this.getDB().prepareStatement(
                                "INSERT INTO PUBLIC.PLAYLIST (SONG_PLAYED, SONG_LINK, SONG_NAME, SONG_INSERT_AT, AUTOR, SONG_DAUER, SONG_SKIPPED) VALUES(?, ?, ?, NOW(), ?, ?, FALSE)",
                                Statement.RETURN_GENERATED_KEYS);
                        stmnt.setBoolean(1, false);
                        stmnt.setString(2, "http://open.spotify.com/track/" + SID);
                        stmnt.setString(3, "[" + track.getArtists().get(0).getName() + "] " + track.getName());
                        if (user != null) {
                            stmnt.setString(4, user.getName());
                        } else {
                            stmnt.setString(4, gid);
                        }
                        stmnt.setInt(5, (int) Math.round(new Integer(track.getDuration()).doubleValue() / 1000));
                        stmnt.executeUpdate();
                        ResultSet key = stmnt.getGeneratedKeys();
                        key.next();
                        try {
                            logger.info("Succesfully added Song (ID: " + key.getLong(1) + ") to Playlist: " + SID
                                    + " by User: " + user.getName());
                        } catch (NullPointerException e) {
                            logger.info("Succesfully added Song (ID: " + key.getLong(1) + ") to Playlist: " + SID
                                    + " by Guest: " + gid);
                        }

                        if (this.getConnectionListener().getHandle() != null) {
                            logger.debug("Notifing Client...");
                            this.getConnectionListener().getHandle().notifynewSong();
                        }
                        return Response.status(201).entity(notice != null ? notice : "Song erfolgreich hinzugefügt")
                                .build();
                    } else {
                        logger.debug("Adding Song aborted, User reached maximum");
                        return Response.status(403).entity("Du hast bereits die maximale Anzahl an Songs eingestellt!")
                                .build();
                    }
                } else {
                    logger.debug("Adding Song aborted, Song allready in Playlist");
                    return Response.status(403).entity("Dieser Song befindet sich bereits in der Playlist!").build();
                }
            } else {
                logger.debug("Adding Song aborted, Playlist is full");
                return Response.status(403).entity("Die Playlist ist leider voll!").build();
            }
        } catch (SQLException e) {
            logger.error("Error adding Song \"" + SID + "\"", e);
        } finally {
            try {
                rs.close();
            } catch (NullPointerException | SQLException e) {
                logger.error("Error closing ResultSet");
            }
            try {
                stmnt.close();
            } catch (NullPointerException | SQLException e) {
                logger.error("Error closing Statement");
            }
        }
        return Response.status(500).entity("Unbekannter Fehler").build();
    }

    private Response addYSong(String vID, User user, String gid) {
        logger.debug("Adding new Song to Playlist... :" + user);
        PreparedStatement stmnt = null;
        ResultSet rs = null;
        String notice = null;
        try {
            if (this.islocked(vID)) {
                if (user != null && user.isAdmin()) {
                    logger.debug("Song is locked, but User is Admin, creating Notice");
                    notice = "Hinweis: Dieser Song wurde gesperrt!";
                } else {
                    logger.debug("Song is locked, denying");
                    return Response.status(403).entity("Dieser Song wurde leider gesperrt!").build();
                }

            }
            stmnt = this.getDB().prepareStatement("select COUNT(*) from PLAYLIST WHERE SONG_PLAYED = FALSE");
            rs = stmnt.executeQuery();
            rs.next();
            if (rs.getInt(1) < 24) {
                rs.close();
                stmnt.close();
                stmnt = this.getDB()
                        .prepareStatement("select COUNT(*) from PLAYLIST WHERE SONG_PLAYED = FALSE AND SONG_LINK = ?");
                stmnt.setString(1, "https://www.youtube.com/watch?v=" + vID);
                rs = stmnt.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    rs.close();
                    stmnt.close();
                    stmnt = this.getDB()
                            .prepareStatement("select COUNT(*) from PLAYLIST WHERE SONG_PLAYED = FALSE AND AUTOR = ?");
                    if (user != null) {
                        stmnt.setString(1, user.getName());
                    } else {
                        stmnt.setString(1, gid);
                    }
                    rs = stmnt.executeQuery();
                    rs.next();
                    if (rs.getInt(1) < 2 || user != null && user.isAdmin()) {
                        stmnt.close();
                        Video v;
                        try {
                            List<Video> list = this.getYouTube().videos().list("status,snippet,contentDetails")
                                    .setKey(key).setId(vID)
                                    .setFields(
                                            "items/status/uploadStatus,items/status/privacyStatus,items/contentDetails/duration,items/snippet/categoryId,items/snippet/title,items/contentDetails/regionRestriction")
                                    .execute().getItems();
                            if (list != null) {
                                v = list.get(0);
                                if (!v.getStatus().getUploadStatus().equals("processed")
                                        || v.getStatus().getUploadStatus().equals("private")) {
                                    throw new IOException("Video not available");
                                }
                                if (v.getContentDetails() != null) {
                                    if (v.getContentDetails().getRegionRestriction() != null) {
                                        if (v.getContentDetails().getRegionRestriction().getBlocked() != null) {
                                            if (v.getContentDetails().getRegionRestriction().getBlocked()
                                                    .contains("DE")) {
                                                throw new IOException("Video not available");
                                            }
                                        }
                                    }
                                }
                            } else {
                                throw new IOException("Video not available");
                            }

                            if (Duration.parse(v.getContentDetails().getDuration()).getSeconds() > 390
                                    && !(user != null && user.isAdmin())) {
                                return Response.status(409).entity("Dieses Video ist leider zu lang!").build();
                            }
                            if (!Controller.allowed.contains(Integer.parseInt(v.getSnippet().getCategoryId()))) {
                                if (user != null && user.isAdmin()) {
                                    logger.debug(
                                            "Song is not in allowed Categorys, but User is Admin, creating Notice");
                                    notice = "Hinweis: Dieser Song befindet sich nicht in einer der erlaubten Kategorien!";
                                } else {
                                    logger.debug("Song is not in allowed Categorys, denying");
                                    return Response.status(403)
                                            .entity("Dieses Video gehört nicht zu einer der erlaubten Kategorien!")
                                            .build();
                                }
                            }
                        } catch (IndexOutOfBoundsException | IOException e) {
                            logger.debug("Video not available", e);
                            return Response.status(404).entity("URL ungültig").build();
                        }
                        stmnt = this.getDB().prepareStatement(
                                "INSERT INTO PUBLIC.PLAYLIST (SONG_PLAYED, SONG_LINK, SONG_NAME, SONG_INSERT_AT, AUTOR, SONG_DAUER, SONG_SKIPPED) VALUES(?, ?, ?, NOW(), ?, ?, FALSE)",
                                Statement.RETURN_GENERATED_KEYS);
                        stmnt.setBoolean(1, false);
                        stmnt.setString(2, "https://www.youtube.com/watch?v=" + vID);
                        stmnt.setString(3, v.getSnippet().getTitle());
                        if (user != null) {
                            stmnt.setString(4, user.getName());
                        } else {
                            stmnt.setString(4, gid);
                        }
                        stmnt.setInt(5, (int) Duration.parse(v.getContentDetails().getDuration()).getSeconds());
                        stmnt.executeUpdate();
                        ResultSet key = stmnt.getGeneratedKeys();
                        key.next();
                        try {
                            logger.info("Succesfully added Song (ID: " + key.getLong(1) + ") to Playlist: " + vID
                                    + " by User: " + user.getName());
                        } catch (NullPointerException e) {
                            logger.info("Succesfully added Song (ID: " + key.getLong(1) + ") to Playlist: " + vID
                                    + " by Guest: " + gid);
                        }

                        if (this.getConnectionListener().getHandle() != null) {
                            logger.debug("Notifing Client...");
                            this.getConnectionListener().getHandle().notifynewSong();
                        }
                        return Response.status(201).entity(notice != null ? notice : "Song erfolgreich hinzugefügt")
                                .build();
                    } else {
                        logger.debug("Adding Song aborted, User reached maximum");
                        return Response.status(403).entity("Du hast bereits die maximale Anzahl an Songs eingestellt!")
                                .build();
                    }
                } else {
                    logger.debug("Adding Song aborted, Song allready in Playlist");
                    return Response.status(403).entity("Dieser Song befindet sich bereits in der Playlist!").build();
                }
            } else {
                logger.debug("Adding Song aborted, Playlist is full");
                return Response.status(403).entity("Die Playlist ist leider voll!").build();
            }
        } catch (SQLException e) {
            logger.error("Error adding Song \"" + user + "\"", e);
        } finally {
            try {
                rs.close();
            } catch (NullPointerException | SQLException e) {
                logger.error("Error closing ResultSet");
            }
            try {
                stmnt.close();
            } catch (NullPointerException | SQLException e) {
                logger.error("Error closing Statement");
            }
        }
        return Response.status(500).entity("Unbekannter Fehler").build();
    }

    public boolean islocked(String VID) {
        logger.debug("Checking if Song is locked: " + VID);
        boolean result = false;
        if (VID != null) {
            PreparedStatement stmnt = null;
            ResultSet rs = null;
            try {
                stmnt = this.connection.prepareStatement("SELECT * FROM LOCKED_SONGS WHERE ytid = ?");
                stmnt.setString(1, VID);
                rs = stmnt.executeQuery();
                if (rs.next()) {
                    logger.debug("Song is locked");
                    result = true;
                }
            } catch (SQLException e) {
                logger.error("SQL Exception while checking Song Lock", e);
            } finally {
                try {
                    rs.close();
                } catch (NullPointerException | SQLException e) {
                    logger.error("Error closing ResultSet");
                }
                try {
                    stmnt.close();
                } catch (NullPointerException | SQLException e) {
                    logger.error("Error closing Statement");
                }
            }
        }
        logger.debug("Song not locked");
        return result;
    }

    public void addmessage(HttpServletRequest req, String message, String type) {
        this.logger.debug("Adding Message to User Session");
        @SuppressWarnings("unchecked")
        List<UserMessage> msgs = (List<UserMessage>) req.getSession().getAttribute("msg");
        if (msgs == null) {
            msgs = new LinkedList<UserMessage>();
        }
        msgs.add(new UserMessage(message, type));
        req.getSession().setAttribute("msg", msgs);
    }

    private static String getSecurePassword() {
        return "a17cfe1428afc4ce22da7cebe0f33b6b21b1eff5dd2842678afa479e3f";
    }

    private static class Shutdown extends Thread {
        @Override
        public void run() {
            Controller.getInstance().shutdown();
        }
    };

    private static class R implements Runnable {

        @Override
        public void run() {
            Thread.currentThread().setName("Exit");
            Scanner sc = new Scanner(System.in, "UTF-8");
            String s;
            while (!Thread.currentThread().isInterrupted()) {
                s = sc.next();
                if (s.equalsIgnoreCase("exit")) {
                    sc.close();
                    Controller.getInstance().shutdown();
                    Runtime.getRuntime().exit(0);
                    break;
                }
            }
        }
    };
}
