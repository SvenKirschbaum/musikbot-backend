package de.elite12.musikbot.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Musikbot Configuration
 */
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix="musikbot")
@Getter
@Setter
public class MusikbotServiceProperties {
	
	
	/**
	 * Version String displayed on the start page
	 */
	
	private String version;
	
	/**
	 * Key used to authenticate Client
	 */
	private String clientkey;

    /**
     * Youtube related Configuration
     */
    private Youtube youtube;

    /**
     * Youtube related Configuration
     */
    private Spotify spotify;

    /**
     * Resource Name to convert roles from
     */
    private String oauthResourceName;


    @Getter
    @Setter
    public static class Youtube {

        /**
         * API-Key used to Access the Youtube-API
         */
		@Getter
		private String apikey;
		
		/**
		 * Set of allowed Categories
		 */
		@Getter
		private Set<Integer> categories;
		
	}
	
	@Getter
	@Setter
	public static class Spotify {
		
		/**
		 * Spotify API ID
		 */
		@Getter
		private String id;
		
		/**
		 * Spotify API Secret
		 */
		@Getter
		private String secret;
		
	}
}
