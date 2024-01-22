package de.elite12.musikbot.server.exceptions.songprovider;

public class PlaylistNotFound extends Exception {
    public PlaylistNotFound(String reason) {
        super(reason);
    }

    public PlaylistNotFound(String reason, Throwable cause) {
        super(reason, cause);
    }

    public PlaylistNotFound(Throwable cause) {
        super(cause);
    }

    public PlaylistNotFound() {
        super();
    }
}
