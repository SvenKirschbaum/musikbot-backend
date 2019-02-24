package de.elite12.musikbot.server.data;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2958362572903498154L;
	private String msg;
	private String type;

	public static final String TYPE_ERROR = "error";
	public static final String TYPE_NOTIFY = "notify";
	public static final String TYPE_SUCCESS = "success";
}
