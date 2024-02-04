package de.elite12.musikbot.backend.config;

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
public class ServiceProperties {

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

	/**
	 * Maximum amount of songs included in gapcloser preview
	 */
	private Integer maxGapcloserPreview;


    @Getter
    @Setter
    public static class Youtube {

		private boolean enabled;

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

		private boolean enabled;

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
