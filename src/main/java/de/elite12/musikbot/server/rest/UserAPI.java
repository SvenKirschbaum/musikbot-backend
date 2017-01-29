package de.elite12.musikbot.server.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import de.elite12.musikbot.server.Controller;
import de.elite12.musikbot.server.User;
import de.elite12.musikbot.shared.Util;

@Path("/user")
public class UserAPI {

	@Context
	HttpServletRequest req;
	@Context
	SecurityContext sc;

	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Path("{userid}/{attribute}")
	public Response deleteSong(@PathParam("userid") int userid, @PathParam("attribute") String attr, String value) {
		User user = (User) sc.getUserPrincipal();
		User target = Controller.getInstance().getUserservice().getUserbyId(userid);
		switch (attr) {
			case "email": {
				if(selforadmin(user, target)) {
					if(Util.isValidEmailAddress(value)) {
						target.setEmail(value);
						Controller.getInstance().getUserservice().changeUser(target);
						if(((User)req.getSession().getAttribute("user")).getId() == target.getId()) {
							req.getSession().setAttribute("user", target);
						}
						return Response.status(200).build();
					}
					else {
						return Response.status(400).build();
					}
				}
				else {
					return Response.status(401).build();
				}
			}
			case "password": {
				if(selforadmin(user, target)) {
					target.setPassword(Controller.getInstance().getUserservice().hashPW(value));
					Controller.getInstance().getUserservice().changeUser(target);
					if(((User)req.getSession().getAttribute("user")).getId() == target.getId()) {
						req.getSession().setAttribute("user", target);
					}
					return Response.status(200).build();
				}
				else {
					return Response.status(401).build();
				}
			}
			case "admin": {
				if(user.isAdmin()) {
					boolean admin = value.equalsIgnoreCase("ja") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
					target.setAdmin(admin);
					Controller.getInstance().getUserservice().changeUser(target);
					if(((User)req.getSession().getAttribute("user")).getId() == target.getId()) {
						req.getSession().setAttribute("user", target);
					}
					return Response.status(200).build();
				}
				else {
					return Response.status(401).build();
				}
			}
			default: {
				return Response.status(404).entity("Userid ungültig").build();
			}
		}
	}
	private boolean selforadmin(User user,User target) {
		return user.equals(target)||user.isAdmin();
	}
}
