package de.elite12.musikbot.server.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

import de.elite12.musikbot.shared.Command;
import de.elite12.musikbot.shared.Song;

public class ConnectionHandler {

    private final String authkey = "zHqan6B7wYfWi8nuOf482PDSY";
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket sock;
    private boolean waitforsong = false;
    private Controller ctr;
    private boolean ispaused;
    private String state = "started";

    public ConnectionHandler(Socket client, Controller control) {
        Logger.getLogger(ConnectionHandler.class).debug("Initializing Handler");
        try {
            this.ctr = control;
            this.out = new ObjectOutputStream(client.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(client.getInputStream());
            this.sock = client;
        } catch (IOException e) {
            Logger.getLogger(ConnectionHandler.class).error("Unknown Exception", e);
        }
    }

    public void run() {
        this.getController().setState("Verbunden");
        Command authcmd = null;
        try {
            Logger.getLogger(ConnectionHandler.class).debug("Requesting Auth");
            this.out.writeObject(new Command(Command.REQUEST_AUTH));
            this.out.flush();
            authcmd = (Command) in.readObject();
        } catch (ClassNotFoundException | IOException e1) {
            Logger.getLogger(ConnectionHandler.class).error("Unknown Error", e1);
        }
        if (authcmd.getCmd() != Command.AUTH || !authcmd.getdata().equals(this.authkey)) {
            try {
                Logger.getLogger(ConnectionHandler.class).warn("Authentification Failure, clsing connection!");
                this.out.writeObject(new Command(Command.INVALID));
                this.out.flush();
                this.sock.close();
            } catch (IOException e) {
                Logger.getLogger(ConnectionHandler.class).error("Unknown Exception", e);
            }
            return;
        } else {
            try {
                this.out.writeObject(new Command(Command.AUTH));
                this.out.flush();
                Logger.getLogger(ConnectionHandler.class).info("Authentification Succesfull");
            } catch (IOException e) {
                Logger.getLogger(ConnectionHandler.class).error("Unknown Exception", e);
            }
        }
        while (!this.sock.isClosed() && this.sock.isConnected()) {
            try {
                Command cmd = (Command) in.readObject();
                Logger.getLogger(ConnectionHandler.class).debug("Got CMD: " + cmd.getCmd());
                switch (cmd.getCmd()) {
                case Command.EMPTY:
                case Command.INVALID: {
                    break;
                }
                case Command.REQUEST_SONG: {
                    Logger.getLogger(ConnectionHandler.class).debug("Got Song Request");
                    Song song = this.getController().getnextSong();
                    if (song != null) {
                        this.sendSong(song);
                    } else {
                        this.out.writeObject(new Command(Command.NO_SONG_AVAILABLE));
                        this.out.flush();
                        this.getController().setState("Warte auf neue Lieder");
                        this.getController().setSongtitle(null);
                        this.getController().setSonglink(null);
                        this.waitforsong = true;
                    }
                    break;
                }
                case Command.SONG_FINISHED: {
                    Logger.getLogger(ConnectionHandler.class).debug("Got Song finished");
                    Song song = this.getController().getnextSong();
                    if (song != null) {
                        this.sendSong(song);
                    } else {
                        this.waitforsong = true;
                        this.out.writeObject(new Command(Command.NO_SONG_AVAILABLE));
                        this.getController().setState("Warte auf neue Lieder");
                        this.getController().setSongtitle(null);
                        this.getController().setSonglink(null);
                        this.out.flush();
                    }
                    break;
                }
				case Command.PLAYBACK_ERROR: {
					Logger.getLogger(ConnectionHandler.class)
							.error("Client reported PLAYBACK_ERROR: " + (String) cmd.getdata());
					Song song = this.getController().getnextSong();
					if (song != null) {
						this.sendSong(song);
					} else {
						this.waitforsong = true;
						this.out.writeObject(new Command(Command.NO_SONG_AVAILABLE));
						this.getController().setState("Warte auf neue Lieder");
						this.getController().setSongtitle(null);
						this.getController().setSonglink(null);
						this.out.flush();
					}
					break;
				}
                }
            } catch (SocketException | EOFException e) {
                Logger.getLogger(ConnectionHandler.class).debug("Die Verbindung wurde vom Client getrennt!", e);
                break;
            } catch (ClassNotFoundException | IOException e) {
                Logger.getLogger(ConnectionHandler.class).error("Unknown Error", e);
                break;
            }
        }
        Logger.getLogger(ConnectionHandler.class).info("Connection closed");
    }

    public void notifynewSong() {
        Logger.getLogger(ConnectionHandler.class).debug("Notify Song");
        if (this.waitforsong) {
            this.sendSong(this.getController().getnextSong());
            this.waitforsong = false;
        }
    }

    public void pause() {
        Logger.getLogger(ConnectionHandler.class).debug("Pausing");
        if (this.state.equals("started")) {
            this.ispaused = !this.ispaused;
            try {
                if (this.ispaused) {
                    this.getController().setState("Paused");
                } else {
                    this.getController().setState("Playing");
                }
                this.out.writeObject(new Command(Command.PAUSE));
                this.out.flush();
            } catch (IOException e) {
                Logger.getLogger(this.getClass()).error("Unknown Error", e);
            }
        }
    }

    public void stop() {
        Logger.getLogger(ConnectionHandler.class).debug("Stopping");
        if (this.state.equals("started")) {
            this.state = "stopped";
            try {
                this.getController().setState("Stopped");
                this.getController().setSongtitle("Kein Song");
                this.getController().setSonglink(null);
                this.out.writeObject(new Command(Command.STOP));
                this.out.flush();
            } catch (IOException e) {
                Logger.getLogger(this.getClass()).error("Unknown Error", e);
            }
        }
    }

    public void start() {
        Logger.getLogger(ConnectionHandler.class).debug("Starting...");
        if (this.state.equals("stopped")) {
            this.state = "started";
            try {
                Song song = this.getController().getnextSong();
                if (song != null) {
                    this.sendSong(song);
                } else {
                    this.out.writeObject(new Command(Command.NO_SONG_AVAILABLE));
                    this.out.flush();
                    this.getController().setState("Warte auf neue Lieder");
                    this.getController().setSongtitle(null);
                    this.getController().setSonglink(null);
                    this.waitforsong = true;
                }
            } catch (IOException e) {
                Logger.getLogger(this.getClass()).error("Unknown Error", e);
            }
        }
    }

    private void sendSong(Song song) {
        Logger.getLogger(ConnectionHandler.class).debug("Sending Song...");
        try {
            this.getController().setState("Playing");
            this.getController().setSongtitle(song.getTitle());
            this.getController().setSonglink(song.getLink());
            Command c = new Command(Command.SONG, song);
            Logger.getLogger(ConnectionHandler.class).debug("Sending Command: " + c);
            this.out.writeObject(c);
            this.out.flush();

        } catch (IOException e) {
            Logger.getLogger(ConnectionHandler.class).error("Unknown Exception", e);
        }
    }

    public void sendShutdown() {
        Logger.getLogger(ConnectionHandler.class).debug("Sending Shutdown...");
        try {
            Command c = new Command(Command.SHUTDOWN);
            this.out.writeObject(c);
            this.out.flush();
        } catch (IOException e) {
            Logger.getLogger(ConnectionHandler.class).error("Unknown Exception", e);
        }
    }

    public Controller getController() {
        return ctr;
    }

    public Socket getsocket() {
        return this.sock;
    }
}
