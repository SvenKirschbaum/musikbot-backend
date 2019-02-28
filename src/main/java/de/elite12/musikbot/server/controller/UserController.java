package de.elite12.musikbot.server.controller;

import java.util.ArrayList;
import java.util.UUID;

import javax.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import de.elite12.musikbot.server.data.UserPrincipal;
import de.elite12.musikbot.server.data.entity.User;
import de.elite12.musikbot.server.data.repository.SongRepository;
import de.elite12.musikbot.server.services.UserService;
import de.elite12.musikbot.server.util.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userservice;

	@Autowired
	private SongRepository songs;

	@GetMapping("{user}")
	public String getAction(@PathVariable String user, Model model) {
		Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User u = null;
		boolean admin = false;
		boolean self = false;
		boolean guest = false;
		if (p instanceof UserPrincipal) {
			UserPrincipal t = (UserPrincipal) p;
			u = t.getUser();
			admin = u.isAdmin();
		}
		User target = userservice.findUserbyName(user);
		if (target != null) {
			if (target.getId() == u.getId())
				self = true;
		}

		try {
			String name = UUID.fromString(user).toString();
			target = new User();
			target.setName(name);
			guest = true;
		} catch (IllegalArgumentException e) {

		}

		if (target == null) {
			throw new NotFoundException();
		}

		ArrayList<DataEntry> userinfo = new ArrayList<>();
		if (!guest) {
			userinfo.add(new DataEntry("ID:", target.getId().toString(), false, "id"));
		}
		userinfo.add(new DataEntry("Username:", target.getName(), admin && (!guest), "username"));
		if (!guest && (self || admin)) {
			userinfo.add(new DataEntry("Email:", target.getEmail(), true, "email"));
			userinfo.add(new DataEntry("Passwort:", "****", true, "password"));
			userinfo.add(new DataEntry("Admin: ", target.isAdmin() ? "Ja" : "Nein", admin && !guest, "admin"));
		}

		Long songcount;
		if (guest) {
			songcount = songs.countByGuestAuthor(target.getName());
		} else {
			songcount = songs.countByUserAuthor(target);
		}
		userinfo.add(new DataEntry("Wünsche: ", songcount.toString(), false, null));

		Long skippedcount;
		if (guest) {
			skippedcount = songs.countByGuestAuthorAndSkipped(target.getName(), true);
		} else {
			skippedcount = songs.countByUserAuthorAndSkipped(target, true);
		}
		userinfo.add(new DataEntry("Davon übersprungen: ", skippedcount.toString(), false, null));

		Iterable<Tuple> topsongs;
		if (guest) {
			topsongs = songs.findTopByGuest(target.getName());
		} else {
			topsongs = songs.findTopByUser(target);
		}

		Iterable<Tuple> topskipped;
		if (guest) {
			topskipped = songs.findTopSkippedByGuest(target.getName());
		} else {
			topskipped = songs.findTopSkippedByUser(target);
		}

		Iterable<Tuple> recent;
		if (guest) {
			recent = songs.findRecentByGuest(target.getName());
		} else {
			recent = songs.findRecentByUser(target);
		}

		model.addAttribute("userinfo", userinfo);
		model.addAttribute("topsongs", topsongs);
		model.addAttribute("topskipped", topskipped);
		model.addAttribute("recent", recent);
		model.addAttribute("gravatarid", target.getGravatarId());
		return "user";
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@Setter
	public static class DataEntry {
		private String name;
		private String value;
		private boolean changeable;
		private String urlname;
	}
}
