package de.elite12.musikbot.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

public class ConnectionListener extends Thread {

	private ServerSocket sock;
	private ConnectionHandler handle;
	private Controller ctr;

	public ConnectionListener(Controller control) {

		super();
		Logger.getLogger(ConnectionListener.class).debug(
				"Initializing ConnectionListener...");

		this.ctr = control;

		try {
			Logger.getLogger(ConnectionListener.class).debug(
					"Creating Server Socket...");
			sock = new ServerSocket(65534);
		} catch (IOException e) {
			Logger.getLogger(ConnectionListener.class).fatal(
					"Error Creating ServerSocket, System will Exit", e);
			Runtime.getRuntime().exit(-1);
		}
		Logger.getLogger(ConnectionListener.class).debug("Starting Thread...");
	}

	@Override
	public void run() {
		Thread.currentThread().setName("Connection");
		while (!this.isInterrupted()) {
			try {
				Logger.getLogger(ConnectionListener.class).debug(
						"Wait for Connection...");
				Socket client = sock.accept();
				Logger.getLogger(ConnectionListener.class).info(
						"Incoming Connection");
				Logger.getLogger(ConnectionListener.class).debug(
						"Incoming Connection, delegating to new Handler...");
				this.handle = new ConnectionHandler(client,
						this.getController());
				this.handle.run();
				Logger.getLogger(ConnectionListener.class).info(
						"Handler exitet, waiting for new Connection");
				this.handle = null;
				this.getController().setState("Keine Verbindung zum BOT");
			} catch (SocketException e) {
				Logger.getLogger(ConnectionListener.class).debug(
						"Socket Exception", e);
			} catch (IOException e) {
				Logger.getLogger(ConnectionListener.class).error(
						"Unknown IOException", e);
			}
		}
		Logger.getLogger(ConnectionListener.class).debug(
				"Exiting Loop due to Interrupt");
		try {
			sock.close();
		} catch (IOException e) {
			Logger.getLogger(ConnectionListener.class).error(
					"Unknown Exception while closing Socket", e);
		}
		Logger.getLogger(ConnectionListener.class).info(
				"Shutting down ConnectionListener");
	}

	public ServerSocket getSocket() {
		return this.sock;
	}

	public ConnectionHandler getHandle() {
		return handle;
	}

	public Controller getController() {
		return this.ctr;
	}
}
