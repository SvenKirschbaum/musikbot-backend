package de.elite12.musikbot.server.services;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;

import de.elite12.musikbot.server.core.MusikbotServiceProperties;

@Service
public class YouTubeService {

	private YouTube yt;
	
	public YouTubeService(@Autowired MusikbotServiceProperties config) throws GeneralSecurityException, IOException {
		
		this.yt = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), new JacksonFactory(), new HttpRequestInitializer() {
			
			@Override
			public void initialize(HttpRequest request) throws IOException {
				
			}
		}).setYouTubeRequestInitializer(new YouTubeRequestInitializer(config.getYoutube().getApikey())).setApplicationName("e12-musikbot").build();
	}
	
	public YouTube api() {
		return this.yt;
	}
	
}
