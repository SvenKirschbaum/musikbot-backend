package de.elite12.musikbot.server.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;

import de.elite12.musikbot.server.Controller;

@Path("/control")
@RolesAllowed("admin")
public class Control {
	
	@Context SecurityContext sc;
	
	@POST
	@Path("/start")
	public void start() {
		if (Controller.getInstance().getConnectionListener().getHandle() != null) {
			Controller.getInstance().getConnectionListener().getHandle().start();
			Logger.getLogger(this.getClass()).info("Botstart by User: "+ sc.getUserPrincipal());
		}
	}
	
	@POST
	@Path("/stop")
	public void stop() {
		if (Controller.getInstance().getConnectionListener().getHandle() != null) {
			Controller.getInstance().getConnectionListener().getHandle().stop();
			Logger.getLogger(this.getClass()).info("Botstop by User: "+ sc.getUserPrincipal());
		}
	}
	
	@POST
	@Path("/pause")
	public void pause() {
		if (Controller.getInstance().getConnectionListener().getHandle() != null) {
			Controller.getInstance().getConnectionListener().getHandle().pause();
			Logger.getLogger(this.getClass()).info("Botpause by User: "+ sc.getUserPrincipal());
		}
	}
	
	@POST
	@Path("/skip")
	public void skip() {
		if (Controller.getInstance().getConnectionListener().getHandle() != null) {
			Logger.getLogger(this.getClass()).info("Song skipped by User: "+ sc.getUserPrincipal()+ " Song: "+ Controller.getInstance().getSongtitle());
			Controller.getInstance().markskipped();
			Controller.getInstance().getConnectionListener().getHandle().stop();
			Controller.getInstance().getConnectionListener().getHandle().start();
		}
	}
}
