package de.elite12.musikbot.server;

import java.io.Serializable;

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

	public UserMessage() {

	}

	public UserMessage(String msg, String type) {
		this.setMsg(msg);
		this.setType(type);
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String gettype() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
