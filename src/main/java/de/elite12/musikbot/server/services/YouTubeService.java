package de.elite12.musikbot.server.services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import de.elite12.musikbot.server.config.MusikbotServiceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class YouTubeService {

	private YouTube yt;
	
	public YouTubeService(@Autowired MusikbotServiceProperties config) throws GeneralSecurityException, IOException {
		
		this.yt = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), new JacksonFactory(), request -> {}).setYouTubeRequestInitializer(new YouTubeRequestInitializer(config.getYoutube().getApikey())).setApplicationName("e12-musikbot").build();
	}
	
	public YouTube api() {
		return this.yt;
	}
	
}
