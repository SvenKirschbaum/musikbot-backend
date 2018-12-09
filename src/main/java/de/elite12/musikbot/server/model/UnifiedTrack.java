package de.elite12.musikbot.server.model;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import com.google.api.services.youtube.model.Video;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Track;

import de.elite12.musikbot.server.core.Controller;
import de.elite12.musikbot.server.util.Spotify;
import de.elite12.musikbot.shared.Song;
import de.elite12.musikbot.shared.SongIDParser;

public class UnifiedTrack {

	private Song song;

	private Video video = null;
	private Track track = null;
	private Type type;

	private UnifiedTrack(Song s) throws IOException, TrackNotAvailableException, InvalidURLException {
		this.song = s;
		String VID = SongIDParser.getVID(s.getLink());
		String SID = SongIDParser.getSID(s.getLink());
		if (VID == null && SID == null) {
			throw new InvalidURLException("URL invalid");
		}
		if (VID != null) {
			this.type = Type.YOUTUBE;
			List<Video> list = Controller.getInstance().getYouTube().videos().list("status,snippet,contentDetails")
					.setKey(Controller.key).setId(SongIDParser.getVID(s.getLink()))
					.setFields(
							"items/status/uploadStatus,items/status/privacyStatus,items/contentDetails/duration,items/snippet/categoryId,items/snippet/title,items/contentDetails/regionRestriction")
					.execute().getItems();
			if (list != null) {
				if (!list.get(0).getStatus().getUploadStatus().equals("processed")
						|| list.get(0).getStatus().getUploadStatus().equals("private")) {
					throw new TrackNotAvailableException("private/processing");
				}
				if (list.get(0).getContentDetails() != null) {
					if (list.get(0).getContentDetails().getRegionRestriction() != null) {
						if (list.get(0).getContentDetails().getRegionRestriction().getBlocked() != null) {
							if (list.get(0).getContentDetails().getRegionRestriction().getBlocked().contains("DE")) {
								throw new TrackNotAvailableException("RegionBlock (blocklist)");
							}
						}
						if (list.get(0).getContentDetails().getRegionRestriction().getAllowed() != null) {
							if (!list.get(0).getContentDetails().getRegionRestriction().getAllowed().contains("DE")) {
								throw new TrackNotAvailableException("RegionBlock (whitelist)");
							}
						}
					}
				}
				this.video = list.get(0);
			} else {
				throw new TrackNotAvailableException("Not found");
			}
		}
		else if (SID != null) {
			this.type = Type.SPOTIFY;
			try {
				this.track = Spotify.getTrackRaw(SongIDParser.getSID(s.getLink()));
			} catch (SpotifyWebApiException e) {
				throw new TrackNotAvailableException("Error loading Track", e);
			}
			if (this.track == null) {
				throw new IOException("Identified as spotify, but no sid");// This should be impossible to reach
			}
		}
	}

	public static UnifiedTrack fromSong(Song s) throws IOException, TrackNotAvailableException, InvalidURLException {
		return new UnifiedTrack(s);
	}

	public static UnifiedTrack fromURL(String url) throws IOException, TrackNotAvailableException, InvalidURLException {
		Song song = new Song();
		song.setLink(url);
		return new UnifiedTrack(song);
	}

	public Integer getDuration() {
		if (this.type == Type.YOUTUBE) {
			return (int) Duration.parse(this.video.getContentDetails().getDuration()).getSeconds();
		}
		if (this.type == Type.SPOTIFY) {
			return this.track.getDurationMs()/1000;
		}
		throw new RuntimeException("Invalid State");
	}
	
	public String getTitle() {
		if (this.type == Type.YOUTUBE) {
			return this.video.getSnippet().getTitle();
		}
		if (this.type == Type.SPOTIFY) {
			return "[" + this.track.getArtists()[0].getName() + "] " + this.track.getName();
		}
		throw new RuntimeException("Invalid State");
	}
	
	public Integer getCategoryId() {
		if (this.type == Type.YOUTUBE) {
			return Integer.parseInt(this.video.getSnippet().getCategoryId());
		}
		if (this.type == Type.SPOTIFY) {
			return 1;
		}
		throw new RuntimeException("Invalid State");
	}
	
	public String getId() {
		if (this.type == Type.YOUTUBE) {
			return SongIDParser.getVID(this.song.getLink());
		}
		if (this.type == Type.SPOTIFY) {
			return SongIDParser.getSID(this.song.getLink());
		}
		throw new RuntimeException("Invalid State");
	}
	
	public String getLink() {
		if (this.type == Type.YOUTUBE) {
			return "https://www.youtube.com/watch?v=" + this.getId();
		}
		if (this.type == Type.SPOTIFY) {
			return "https://open.spotify.com/track/" + this.getId();
		}
		throw new RuntimeException("Invalid State");
	}

	private enum Type {
		YOUTUBE, SPOTIFY
	}

	public static class TrackNotAvailableException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9003042675486431962L;

		public TrackNotAvailableException(String s) {
			super(s);
		}

		public TrackNotAvailableException(String s, Throwable e) {
			super(s, e);
		}
	}
	public static class InvalidURLException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4761583274648596930L;

		public InvalidURLException(String s) {
			super(s);
		}

		public InvalidURLException(String s, Throwable e) {
			super(s, e);
		}
	}
}
