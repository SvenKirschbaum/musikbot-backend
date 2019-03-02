package de.elite12.musikbot.server.services;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import de.elite12.musikbot.server.core.MusikbotServiceProperties;
import de.elite12.musikbot.server.data.entity.Song;
import de.elite12.musikbot.shared.Command;

@Service
public class ClientService implements Runnable {
	
	@Autowired
	private TaskExecutor taskExecutor;
	
	@Autowired
	private SongService songservice;
	
	@Autowired
	private MusikbotServiceProperties config;

	
	private ServerSocket sock;
	private Socket client;
	
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean waitforsong = false;
    private boolean ispaused = false;
    private State state = State.STARTED;
    private Thread thread;
	
	private static Logger logger = LoggerFactory.getLogger(ClientService.class);

	public ClientService() throws IOException {
		logger.debug("Initializing ConnectionListener...");

		try {
			logger.debug("Creating Server Socket...");
			sock = new ServerSocket(65534);
		} catch (IOException e) {
			logger.error("Error Creating ServerSocket", e);
			throw e;
		}
	}
	
	@PostConstruct
	private void postConstruct() {
		logger.debug("Starting Thread...");
		taskExecutor.execute(this);
	}
	
	@PreDestroy
	private void preDestroy() {
		thread.interrupt();
		try {
			sock.close();
		} catch(IOException e) {
			logger.warn("Exception",e);
		}
		try {
			client.close();
		} catch(IOException e) {
			logger.warn("Exception",e);
		}
	}

	@Override
	public void run() {
		this.thread = Thread.currentThread();
		while (!thread.isInterrupted()) {
			try {
				logger.debug("Wait for Connection...");
				client = sock.accept();
				logger.info("Incoming Connection");

				this.out = new ObjectOutputStream(client.getOutputStream());
	            this.out.flush();
	            this.in = new ObjectInputStream(client.getInputStream());
	            
	            Command authcmd = null;
	            logger.debug("Requesting Auth");
	            this.out.writeObject(new Command(Command.REQUEST_AUTH));
	            this.out.flush();
	            authcmd = (Command) in.readObject();
	            
	            if (authcmd.getCmd() != Command.AUTH || !authcmd.getdata().equals(config.getClientkey())) {
	                try {
	                    logger.warn("Authentification Failure, closing connection!");
	                    this.out.writeObject(new Command(Command.INVALID));
	                    this.out.flush();
	                    this.client.close();
	                } catch (IOException e) {
	                    logger.error("Unknown Exception", e);
	                }
	                continue;
	            } else {
	                try {
	                    this.out.writeObject(new Command(Command.AUTH));
	                    this.out.flush();
	                    logger.info("Authentification Succesfull");
	                } catch (IOException e) {
	                    logger.error("Unknown Exception", e);
	                }
	            }
	            
	            songservice.setState("Verbunden");
	            
	            while (!this.client.isClosed() && this.client.isConnected() && !thread.isInterrupted()) {
	                try {
	                    Command cmd = (Command) in.readObject();
	                    logger.debug("Got CMD: " + cmd.getCmd());
	                    switch (cmd.getCmd()) {
		                    case Command.EMPTY:
		                    case Command.INVALID: {
		                        break;
		                    }
		                    case Command.REQUEST_SONG: {
		                        logger.debug("Got Song Request");
		                        Song song = songservice.getnextSong();
		                        if (song != null) {
		                            this.sendSong(song);
		                        } else {
		                            this.out.writeObject(new Command(Command.NO_SONG_AVAILABLE));
		                            this.out.flush();
		                            songservice.setState("Warte auf neue Lieder");
		                            songservice.setSongtitle(null);
		                            songservice.setSonglink(null);
		                            this.waitforsong = true;
		                        }
		                        break;
		                    }
		                    case Command.SONG_FINISHED: {
		                        logger.debug("Got Song finished");
		                        Song song = songservice.getnextSong();
		                        if (song != null) {
		                            this.sendSong(song);
		                        } else {
		                            this.waitforsong = true;
		                            this.out.writeObject(new Command(Command.NO_SONG_AVAILABLE));
		                            songservice.setState("Warte auf neue Lieder");
		                            songservice.setSongtitle(null);
		                            songservice.setSonglink(null);
		                            this.out.flush();
		                        }
		                        break;
		                    }
		    				case Command.PLAYBACK_ERROR: {
		    					logger.error("Client reported PLAYBACK_ERROR: " + (String) cmd.getdata());
		    					Song song = songservice.getnextSong();
		    					if (song != null) {
		    						this.sendSong(song);
		    					} else {
		    						this.waitforsong = true;
		    						this.out.writeObject(new Command(Command.NO_SONG_AVAILABLE));
		    						songservice.setState("Warte auf neue Lieder");
		    						songservice.setSongtitle(null);
		    						songservice.setSonglink(null);
		    						this.out.flush();
		    					}
		    					break;
		    				}
	                    }
	                } catch (SocketException | EOFException e) {
	                    logger.debug("Die Verbindung wurde vom Client getrennt!", e);
	                    break;
	                } catch (ClassNotFoundException | IOException e) {
	                    logger.error("Unknown Error", e);
	                    break;
	                }
	            }
	            try {
	            	client.close();
	            }
	            catch(IOException e) {
	            	logger.error("Unknown Error", e);
	            }
	            logger.info("Connection closed");
	            

				songservice.setState("Keine Verbindung zum BOT");
			} catch (SocketException e) {
				logger.debug("Socket Exception", e);
			} catch (IOException e) {
				logger.error("Unknown IOException", e);
			} catch (ClassNotFoundException e) {
				logger.error("ClassNotFound", e);
			}
		}
		logger.debug("Exiting Loop due to Interrupt");
		try {
			sock.close();
		} catch (IOException e) {
			logger.error("Unknown Exception while closing Socket", e);
		}
		logger.info("Shutting down ConnectionListener");
	}
	
	public void notifynewSong() {
        logger.debug("Notify Song");
        if(!checkConnected()) return;
        if (this.waitforsong) {
            this.sendSong(songservice.getnextSong());
            this.waitforsong = false;
        }
    }
	
	public void pause() {
        logger.debug("Pausing");
        if(!checkConnected()) return;
        if (this.state == State.STARTED) {
            this.ispaused = !this.ispaused;
            try {
                if (this.ispaused) {
                    songservice.setState("Paused");
                } else {
                    songservice.setState("Playing");
                }
                this.out.writeObject(new Command(Command.PAUSE));
                this.out.flush();
            } catch (IOException e) {
                logger.error("Unknown Error", e);
            }
        }
    }

    public void stop() {
        logger.debug("Stopping");
        if(!checkConnected()) return;
        if (this.state == State.STARTED) {
            this.state = State.STOPPED;
            try {
            	songservice.setState("Stopped");
            	songservice.setSongtitle("Kein Song");
            	songservice.setSonglink(null);
                this.out.writeObject(new Command(Command.STOP));
                this.out.flush();
            } catch (IOException e) {
                logger.error("Unknown Error", e);
            }
        }
    }

    public void start() {
        logger.debug("Starting...");
        if(!checkConnected()) return;
        if (this.state == State.STOPPED) {
            this.state = State.STARTED;
            try {
                Song song = songservice.getnextSong();
                if (song != null) {
                    this.sendSong(song);
                } else {
                    this.out.writeObject(new Command(Command.NO_SONG_AVAILABLE));
                    this.out.flush();
                    songservice.setState("Warte auf neue Lieder");
                    songservice.setSongtitle(null);
                    songservice.setSonglink(null);
                    this.waitforsong = true;
                }
            } catch (IOException e) {
                logger.error("Unknown Error", e);
            }
        }
    }
    
    private void sendSong(Song song) {
        logger.debug("Sending Song...");
        if(!checkConnected()) return;
        try {
        	this.waitforsong = false;
        	songservice.setState("Playing");
        	songservice.setSongtitle(song.getTitle());
        	songservice.setSonglink(song.getLink());
        	
        	de.elite12.musikbot.shared.Song tmp = new de.elite12.musikbot.shared.Song();
        	tmp.setLink(song.getLink());
        	tmp.setTitle(song.getTitle());
        	tmp.settype(song.getLink().contains("spotify") ? "spotify" : "youtube");
        	
            Command c = new Command(Command.SONG, tmp);
            logger.debug("Sending Command: " + c);
            this.out.writeObject(c);
            this.out.flush();

        } catch (IOException e) {
            logger.error("Unknown Exception", e);
        }
    }

    public void sendShutdown() {
        logger.debug("Sending Shutdown...");
        if(!checkConnected()) return;
        try {
            Command c = new Command(Command.SHUTDOWN);
            this.out.writeObject(c);
            this.out.flush();
        } catch (IOException e) {
            logger.error("Unknown Exception", e);
        }
    }
    
    private boolean checkConnected() {
    	return this.client != null && !this.client.isClosed() && this.client.isConnected();
    }
	
	public static enum State {
		STARTED,
		PAUSED,
		STOPPED
	}
}
