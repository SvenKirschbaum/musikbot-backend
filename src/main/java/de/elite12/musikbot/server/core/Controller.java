package de.elite12.musikbot.server.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

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
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.common.io.Closeables;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import de.elite12.musikbot.server.core.Gapcloser.Mode;
import de.elite12.musikbot.server.model.UnifiedTrack;
import de.elite12.musikbot.server.model.User;
import de.elite12.musikbot.server.model.UserMessage;
import de.elite12.musikbot.server.model.UnifiedTrack.InvalidURLException;
import de.elite12.musikbot.server.model.UnifiedTrack.TrackNotAvailableException;
import de.elite12.musikbot.server.servlets.DebugServlet;
import de.elite12.musikbot.server.servlets.FeedGenerator;
import de.elite12.musikbot.server.servlets.GapcloserServlet;
import de.elite12.musikbot.server.servlets.LogServlet;
import de.elite12.musikbot.server.servlets.OnlineServlet;
import de.elite12.musikbot.server.servlets.PlaylistServlet;
import de.elite12.musikbot.server.servlets.SongManagement;
import de.elite12.musikbot.server.servlets.UserServlet;
import de.elite12.musikbot.server.servlets.Weblet;
import de.elite12.musikbot.server.util.MBErrorHandler;
import de.elite12.musikbot.shared.Song;

public class Controller {

    public static final String key = "AIzaSyAsDZP0xvd0cyr7JjyR7SlkFEJhFMHF2ik";
    public final String version;

    private Logger logger;
    private static Controller instance;
    private YouTube yt;

    private ConnectionListener connectionListener;
    private Server server;
    private ComboPooledDataSource ds;
    private Userservice userservice;
    private String songtitle = "Kein Song";
    private String state = "Keine Verbindung zum BOT";
    private Gapcloser g;
    
	private static List<Integer> allowed = Arrays.asList(new Integer[] { 1, 10, 18, 20, 24, 30, 44 });

    private String songlink;

    public Controller() {
    	ClassPathResource cpr = new ClassPathResource("app.properties");
        Properties p = new Properties();
        InputStream in = null;
        try {
            in = cpr.getInputStream();
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
            logger.debug("SQL Server initialised");

            this.yt = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("e12-musikbot").build();

            logger.debug("Youtube API Connection initialised");

            /*
             * this.bds.setUrl("jdbc:mariadb://localhost:3306/musikbot"); this.bds.setUsername("musikbot");
             * this.bds.setPassword(getSecurePassword());
             */
            logger.debug("Initialising DataSource");
            Class.forName("org.mariadb.jdbc.Driver");
            ComboPooledDataSource cpds = new ComboPooledDataSource();
            cpds.setDriverClass("org.mariadb.jdbc.Driver");
            cpds.setJdbcUrl("jdbc:mariadb://localhost:3306/musikbot");
            cpds.setUser("musikbot");
            cpds.setPassword(getPassword());
            cpds.setMinPoolSize(2);
            cpds.setMaxPoolSize(20);
            cpds.setInitialPoolSize(2);
            cpds.setAcquireIncrement(2);
            cpds.setTestConnectionOnCheckout(true);

            this.ds = cpds;

            this.userservice = new Userservice(this);
            logger.debug("Userservice initialised");

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
            sm.getSessionCookieConfig().setMaxAge(604800);
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
            sites.addServlet(new ServletHolder(new GapcloserServlet(this)), "/gapcloser/");
            sites.addServlet(new ServletHolder(new FeedGenerator(this)), "/feed/");
            sites.addServlet(new ServletHolder(new SongManagement(this)), "/songs/");
            sites.addServlet(new ServletHolder(new UserServlet(this)), "/user/*");
            sites.addServlet(new ServletHolder(new OnlineServlet(this)), "/whoisonline/");
            sites.addServlet(new ServletHolder(new LogServlet(this)), "/log/");
            sites.addServlet(new ServletHolder(new DebugServlet(this)), "/debug/");

            ResourceConfig config = new ResourceConfig().register(new RolesAllowedDynamicFeature())
                    .packages("de.elite12.musikbot.server.api");
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
        } catch (Exception e) {
            logger.fatal("Error starting Controller, System will Exit!", e);
            Runtime.getRuntime().exit(-1);
        }
        logger.debug("End of Controller");
    }

    public void start() {
        this.connectionListener.start();
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public Connection getDB() throws SQLException {
        return this.ds.getConnection();
    }

    public static Controller getInstance() {
        return Controller.instance;
    }

    public YouTube getYouTube() {
        return yt;
    }

    public Song getnextSong() {
        try (
	        Connection c = this.getDB();
	        PreparedStatement stmnt = c.prepareStatement(
	            "select * from PLAYLIST WHERE SONG_PLAYED = FALSE ORDER BY SONG_SORT ASC LIMIT 0,1");
	        PreparedStatement stmnt2 = c.prepareStatement(
	            "UPDATE PLAYLIST SET SONG_PLAYED = TRUE, SONG_PLAYED_AT = NOW() WHERE SONG_ID = ?");
        ) {
            ResultSet rs = stmnt.executeQuery();
            if (rs.next()) {
                logger.debug("Found Song in Database");
                
                stmnt2.setInt(1, rs.getInt("SONG_ID"));
                stmnt2.execute();
                Song s = new Song(rs);
                try {
                	UnifiedTrack.fromSong(s);
                }
                catch(IOException e) {
                	logger.error("Error Loading Track", e);
                    return null;
                }
                catch(TrackNotAvailableException e) {
                	logger.warn("Song seems to got deleted, skipping", e);
                    return this.getnextSong();
                } catch (InvalidURLException e) {
                	logger.fatal("Impossible Error",e);
                	return this.getnextSong();
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
    
    public Gapcloser getGapcloser() {
		return g;
	}

    public void markskipped() {
        try (
            Connection c = this.getDB();
            PreparedStatement stmnt = c.prepareStatement(
                "select * from PLAYLIST WHERE SONG_PLAYED = TRUE ORDER BY SONG_ID DESC LIMIT 0,1");
        ) {
            logger.debug("Marking last Song as skipped");

            ResultSet rs = stmnt.executeQuery();
            if (rs.next()) {
                try (
                        PreparedStatement stmnt2 = c.prepareStatement(
                                "UPDATE PLAYLIST SET SONG_SKIPPED = TRUE WHERE SONG_ID = " + rs.getInt("SONG_ID"));
                ) {
                    stmnt2.execute();
                } catch (SQLException e) {
                    logger.error("SQL Error marking last Song as skipped", e);
                }
            }
        } catch (SQLException e) {
            logger.error("SQL Error marking last Song as skipped", e);
        }
    }

    private void shutdown() {
        logger.info("Shutting down Controller");
        try {
            logger.debug("Interrupting Listener Thread...");
            this.getConnectionListener().interrupt();
            if (this.getConnectionListener().getHandle() != null) {
            	if(this.getConnectionListener().getHandle().getsocket() != null) {
	                logger.debug("Closing Existing Connection...");
	                this.getConnectionListener().getHandle().getsocket().close();
	            }
            }
            logger.debug("Closing Server Socket...");
            this.getConnectionListener().getSocket().close();
            logger.debug("Shutting down jetty...");
            this.server.stop();
            logger.debug("Destroying DataSource...");
            DataSources.destroy(this.ds);
            logger.debug("Interrupting Main Thread...");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.fatal("Exception in Shutdown Routine", e);
        }
    }

    public Response addSong(String url, User user, String gid) {
    	try (
			Connection c = this.getDB();
            PreparedStatement statusstmnt = c.prepareStatement(
        		"SELECT s1.queued, s2.identical, s3.author, s4.locked\n" + 
        		"FROM\n" + 
        		"	(select COUNT(*) as queued from PLAYLIST WHERE SONG_PLAYED = FALSE) as s1\n" + 
        		"JOIN\n" + 
        		"	(select COUNT(*) as identical from PLAYLIST WHERE SONG_PLAYED = FALSE AND SONG_LINK = ?) as s2\n" + 
        		"JOIN\n" + 
        		"	(select COUNT(*) as author from PLAYLIST WHERE SONG_PLAYED = FALSE AND AUTOR = ?) as s3\n" + 
        		"JOIN\n" +
        		"	(select COUNT(*) as locked from LOCKED_SONGS WHERE ytid = ?) as s4"
            );
            PreparedStatement insertstmnt = c.prepareStatement(
                    "INSERT INTO PLAYLIST (SONG_PLAYED, SONG_LINK, SONG_NAME, SONG_INSERT_AT, AUTOR, SONG_DAUER, SONG_SKIPPED) VALUES(?, ?, ?, NOW(), ?, ?, FALSE)",
                    Statement.RETURN_GENERATED_KEYS);
    	) {
    		logger.debug("Trying to Add Song "+url);
			UnifiedTrack ut = UnifiedTrack.fromURL(url);
			String notice = null;
			
			statusstmnt.setString(1, ut.getLink());
			statusstmnt.setString(2, user != null ? user.getName() : gid);
			statusstmnt.setString(3, ut.getId());
			
			ResultSet rs = statusstmnt.executeQuery();
            rs.next();
            
            if (rs.getInt("locked") > 0) {
				if (user != null && user.isAdmin()) {
					logger.debug("Song is locked, but User is Admin, creating Notice");
					notice = "Hinweis: Dieser Song wurde gesperrt!";
				} else {
					logger.debug("Song is locked, denying");
					return Response.status(403).entity("Dieser Song wurde leider gesperrt!").build();
				}
            }
            
            if(rs.getInt("queued") >= 24) {
            	logger.debug("Adding Song aborted, Playlist is full");
                return Response.status(403).entity("Die Playlist ist leider voll!").build();
            }
            
            if(rs.getInt("identical") > 0) {
            	logger.debug("Adding Song aborted, Song allready in Playlist");
                return Response.status(403).entity("Dieser Song befindet sich bereits in der Playlist!").build();
            }
            
            if(rs.getInt("author") > 2 && (user == null || !user.isAdmin())) {
            	logger.debug("Adding Song aborted, User reached maximum");
                return Response.status(403).entity("Du hast bereits die maximale Anzahl an Songs eingestellt!").build();
            }
            
            if(ut.getDuration() > 600 && (user == null || !user.isAdmin())) {
	        	logger.debug("Adding Song aborted, Song to long");
	        	return Response.status(409).entity("Dieses Video ist leider zu lang!").build();
            }
            
            if (!Controller.allowed.contains(ut.getCategoryId())) {
                if (user != null && user.isAdmin()) {
                    logger.debug("Song is not in allowed Categorys, but User is Admin, creating Notice");
                    notice = "Hinweis: Dieser Song befindet sich nicht in einer der erlaubten Kategorien!";
                } else {
                    logger.debug("Song is not in allowed Categorys, denying");
                    return Response.status(403).entity("Dieses Song gehört nicht zu einer der erlaubten Kategorien!").build();
                }
            }
                
            insertstmnt.setBoolean(1, false);
            insertstmnt.setString(2, ut.getLink());
            insertstmnt.setString(3, ut.getTitle());
            insertstmnt.setString(4, user != null ? user.getName() : gid);
            insertstmnt.setInt(5, ut.getDuration());
            insertstmnt.executeUpdate();
            
            ResultSet key = insertstmnt.getGeneratedKeys();
            key.next();
            
            logger.info("Succesfully added Song (ID: " + key.getLong(1) + ") to Playlist: " + ut.getId()
                    + " by " + user != null ? ("User: " + user.getName()) : ("Guest: " + gid));
            
            if (this.getConnectionListener().getHandle() != null) {
                logger.debug("Notifing Client...");
                this.getConnectionListener().getHandle().notifynewSong();
            }
            
            return Response.status(201).entity(notice != null ? notice : "Song erfolgreich hinzugefügt").build();
		} catch (IOException e) {
			logger.error("Error adding Song" ,e);
			return Response.status(500).entity("Unbekannter Fehler").build();
		} catch (TrackNotAvailableException e) {
			logger.debug("Track not found",e);
			return Response.status(404).entity("Song nicht verfügbar: " + e.getMessage()).build();
		} catch (InvalidURLException e) {
			logger.debug("Invalid URL",e);
			return Response.status(400).entity("URL ungültig").build();
		} catch (SQLException e) {
			logger.error("Error adding Song " + url, e);
			return Response.status(500).entity("Unbekannter Fehler").build();
		}
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

    private static String getPassword() {
        return "Q8rIj4ziwIBI8O3id4SAfATOYOfo81";
    }

    private static class Shutdown extends Thread {
        @Override
        public void run() {
            Controller.getInstance().shutdown();
        }
    };
}
