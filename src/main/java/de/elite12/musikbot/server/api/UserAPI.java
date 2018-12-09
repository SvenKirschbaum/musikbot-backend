package de.elite12.musikbot.server.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;

import de.elite12.musikbot.server.core.Controller;
import de.elite12.musikbot.server.model.User;
import de.elite12.musikbot.server.util.Util;

@Path("/user")
public class UserAPI {
    
    @Context
    HttpServletRequest req;
    @Context
    SecurityContext sc;
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("{userid}/{attribute}")
    public Response updateUser(@PathParam("userid") int userid, @PathParam("attribute") String attr, String value) {
        User user = (User) sc.getUserPrincipal();
        User target = Controller.getInstance().getUserservice().getUserbyId(userid);
        switch (attr) {
            case "email": {
                if (selforadmin(user, target)) {
                    if (Util.isValidEmailAddress(value)) {
                        target.setEmail(value);
                        Controller.getInstance().getUserservice().changeUser(target);
                        Logger.getLogger(UserAPI.class)
                                .info(user + " changed Email-Address of " + target + "to " + target.getEmail());
                        return Response.status(200).build();
                    } else {
                        return Response.status(400).build();
                    }
                } else {
                    return Response.status(401).build();
                }
            }
            case "password": {
                if (selforadmin(user, target)) {
                    target.setPassword(Controller.getInstance().getUserservice().hashPW(value));
                    Controller.getInstance().getUserservice().changeUser(target);
                    Logger.getLogger(UserAPI.class).info(user + " changed the Password of " + target);
                    return Response.status(200).build();
                } else {
                    return Response.status(401).build();
                }
            }
            case "admin": {
                if (user.isAdmin()) {
                    boolean admin = value.equalsIgnoreCase("ja") || value.equalsIgnoreCase("yes")
                            || value.equalsIgnoreCase("true");
                    target.setAdmin(admin);
                    Controller.getInstance().getUserservice().changeUser(target);
                    Logger.getLogger(UserAPI.class)
                            .info(user + " changed the Admin-Status of " + target + " to " + target.isAdmin());
                    return Response.status(200).build();
                } else {
                    return Response.status(401).build();
                }
            }
            case "username": {
                if (user.isAdmin()) {
                    if (Controller.getInstance().getUserservice().getUserbyName(value) == null) {
                        try (
                                Connection c = Controller.getInstance().getDB();
                                PreparedStatement stmnt = c
                                        .prepareStatement("UPDATE PLAYLIST SET AUTOR = ? WHERE AUTOR = ?");
                        ) {
                            stmnt.setString(1, value);
                            stmnt.setString(2, target.getName());
                            stmnt.executeUpdate();
                            Logger.getLogger(UserAPI.class)
                                    .info(user + " changed the Username of " + target + " to " + value);
                            target.setName(value);
                            Controller.getInstance().getUserservice().changeUser(target);
                        } catch (SQLException e) {
                            Logger.getLogger(this.getClass()).error("SQLException", e);
                        }
                        return Response.status(200).build();
                    } else {
                        return Response.status(409).build();
                    }
                }
            }
            default: {
                return Response.status(404).entity("Userid ungültig").build();
            }
        }
    }
    
    private boolean selforadmin(User user, User target) {
        return user.equals(target) || user.isAdmin();
    }
}
