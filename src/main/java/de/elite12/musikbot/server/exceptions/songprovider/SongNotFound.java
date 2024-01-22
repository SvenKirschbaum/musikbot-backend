package de.elite12.musikbot.server.exceptions.songprovider;

public class SongNotFound extends Exception {
    public SongNotFound(String reason) {
        super(reason);
    }

    public SongNotFound(String reason, Throwable cause) {
        super(reason, cause);
    }

    public SongNotFound(Throwable cause) {
        super(cause);
    }

    public SongNotFound() {
        super();
    }
}
