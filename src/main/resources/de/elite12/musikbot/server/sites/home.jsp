
<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="header.jsp"%>
<% 
	ResultSet rs = (ResultSet)request.getAttribute("result");
	SimpleDateFormat timeformat = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");
	timeformat.setTimeZone(TimeZone.getDefault());
%>
<script type="text/javascript">
	function start() {
		if(typeof update=='function') {
			setInterval(function(){update()},8000);
			<% if(user != null && user.isAdmin()) { %>
			initdnd();
			<% } %>
		}
		else {
			window.setTimeout(start,1000);
		}
	}
	start();
</script>
<title>Elite12 - Musikbot</title>
<body>
	<script>
	  document.addEventListener('DOMContentLoaded', function(event) {
	    cookieChoices.showCookieConsentBar('Cookies erleichtern die Bereitstellung unserer Dienste. Mit der Nutzung unserer Dienste erklären Sie sich damit einverstanden, dass wir Cookies verwenden.',
	      'Schließen', 'Weitere Informationen', '/impressum/');
	  });
	</script>
	<div id="topic"></div>
	<div id="content">

		<div id="content2">

			<%@ include file="messages.jsp"%>

			<div id="currentsong" class="bordered">
				<div id="state"><%= ((Controller)request.getAttribute("control")).getState() %></div>
				<div id="playlistdauer">
					Die aktuelle Playlist umfasst <span>0</span> Minuten Musik!
				</div>
				<div
					class='<% if(((Controller)request.getAttribute("control")).getSonglink() != null) { %>link<% } %>'
					title="<%= HtmlUtils.htmlEscape(((Controller)request.getAttribute("control")).getSongtitle()) %>"
					id="songtitle">
					<% if(((Controller)request.getAttribute("control")).getSonglink() != null) { %>
					<a
						href="<%= HtmlUtils.htmlEscape(((Controller)request.getAttribute("control")).getSonglink()) %>">
						<%= HtmlUtils.htmlEscape(((Controller)request.getAttribute("control")).getSongtitle()) %>
					</a>
					<% } else { %>Kein Song!<% } %>
				</div>
			</div>


			<% if(user != null && user.isAdmin()) { %>
			<div id="control">
				<table>
					<tbody>
						<tr>
							<td><input type="button" name="action" value="Start"
								onclick='$.ajax({url: "/api/control/start",	method: "POST",	headers: {"Authorization": (typeof authtoken !== "undefined")?authtoken:""},contentType: false}).done(function(data,textStatus,jqXHR) {handleAPIResponse(data,textStatus,jqXHR);update()}).fail(function(data,textStatus,jqXHR) {handleAPIResponse(data,textStatus,jqXHR);update()});' />
							</td>
							<td><input type="button" name="action" value="Pause"
								onclick='$.ajax({url: "/api/control/pause",	method: "POST",	headers: {"Authorization": (typeof authtoken !== "undefined")?authtoken:""},contentType: false}).done(function(data,textStatus,jqXHR) {handleAPIResponse(data,textStatus,jqXHR);update()}).fail(function(data,textStatus,jqXHR) {handleAPIResponse(data,textStatus,jqXHR);update()});' />
							</td>
							<td><input type="button" name="action" value="Stop"
								onclick='$.ajax({url: "/api/control/stop",	method: "POST",	headers: {"Authorization": (typeof authtoken !== "undefined")?authtoken:""},contentType: false}).done(function(data,textStatus,jqXHR) {handleAPIResponse(data,textStatus,jqXHR);update()}).fail(function(data,textStatus,jqXHR) {handleAPIResponse(data,textStatus,jqXHR);update()});' />
							</td>
							<td><input type="button" name="action" value="Skip"
								onclick='$.ajax({url: "/api/control/skip",	method: "POST",	headers: {"Authorization": (typeof authtoken !== "undefined")?authtoken:""},contentType: false}).done(function(data,textStatus,jqXHR) {handleAPIResponse(data,textStatus,jqXHR);update()}).fail(function(data,textStatus,jqXHR) {handleAPIResponse(data,textStatus,jqXHR);update()});' />
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			<% } %>


			
			<form id="pform">
			<% if(user != null && user.isAdmin()) { %>
				<input type="hidden" name="action" value="delete" />
				<% } %>
				<table
					class='bordered <% if(user != null && user.isAdmin()) { %>admin<% } %>'
					id="playlist">
					<thead>
						<tr>
							<th>Song ID</th>
							<th>Eingefügt am</th>
							<th>Eingefügt von</th>
							<th>Titel</th>
							<th>Link</th>
							<% if(user != null && user.isAdmin()) { %><th>Löschen?</th>
							<% } %>
						</tr>
					</thead>
					<tbody>
						<% 
						int dauer = 0;
						while(rs.next()) { 
						dauer += rs.getInt("SONG_DAUER");
						%>
						<tr class="songentry" id='song_<%= rs.getInt("SONG_ID") %>'>
							<td style="text-align: center"
								<% if(user != null && user.isAdmin()) {%>
								class="draghandle" <% } %>><%= rs.getInt("SONG_ID") %></td>
							<td><%= timeformat.format(rs.getObject("SONG_INSERT_AT")) %></td>
							<td
								<%
							     User luser = ((Controller)request.getAttribute("control")).getUserservice().getUserbyName(rs.getString("AUTOR"));
							     String gravatarid = luser==null?Util.md5Hex("null"):Util.md5Hex(luser.getEmail().toLowerCase(Locale.GERMAN));
								%>
								<% if(user != null && user.isAdmin()) {%>
								title="<%= HtmlUtils.htmlEscape(rs.getString("AUTOR")) %>" <% } %>>
								<img alt="pb_playlist" src="https://www.gravatar.com/avatar/<%= gravatarid %>?s=20&d=<%=URLEncoder.encode("https://musikbot.elite12.de/res/favicon_small.png","UTF-8") %>" />
								<a href="/user/<%= URLEncoder.encode(rs.getString("AUTOR")).replace("+","%20") %>"><% 
								try {
									UUID id = UUID.fromString(rs.getString("AUTOR")); 
									%>Gast<%
								}
								catch (IllegalArgumentException e) {%> <%= rs.getString("AUTOR") %>
								<%} %></a>
							</td>
							<td
								title="<%= HtmlUtils.htmlEscape(rs.getString("SONG_NAME")) %>"><%= rs.getString("SONG_NAME").length() > 60 ? HtmlUtils.htmlEscape(rs.getString("SONG_NAME").substring(0, 60)) + "..." : HtmlUtils.htmlEscape(rs.getString("SONG_NAME")) %></td>
							<td><a href="<%= rs.getString("SONG_LINK") %>"
								target="_blank"><%= rs.getString("SONG_LINK") %></a></td>
							<% if(user != null && user.isAdmin()) { %><td><input
								type="checkbox" name="song" value="<%= rs.getInt("SONG_ID")%>" /></td>
							<% } %>
						</tr>
						<% } %>
					</tbody>
				</table>
				<script type="text/javascript">
					function start2() {
						if(typeof $=='function') {
							$("#playlistdauer span").html("<%= dauer/60 %>");$("#playlistdauer").fadeIn();
						}
						else {
							window.setTimeout(start2,1000);
						}
					}
					start2();
				</script>
				<div id="archivlink" class="link">
					<a href="/archiv/">Zum Archiv</a>
				</div>
				<% if(user != null && user.isAdmin()) { %>
				    <input id="deletebutton" type="button" value="Löschen" onclick='var a = "";$("input[type=checkbox]:checked").each(function (data){a=a+"/"+$(this).val()});$.ajax({url: "/api/songs"+a,	method: "DELETE",	headers: {"Authorization": (typeof authtoken !== "undefined")?authtoken:""},contentType: false}).done(function(data,textStatus,jqXHR) {update()}).fail(function(data,textStatus,jqXHR) {handleAPIResponse(data,textStatus,jqXHR);update()});' />
				    <input id="deleteandlockbutton" type="button" value="Löschen und Sperren" onclick='var a = "";$("input[type=checkbox]:checked").each(function (data){a=a+"/"+$(this).val()});$.ajax({url: "/api/songs"+a+"?lock=true",  method: "DELETE",   headers: {"Authorization": (typeof authtoken !== "undefined")?authtoken:""},contentType: false}).done(function(data,textStatus,jqXHR) {update()}).fail(function(data,textStatus,jqXHR) {handleAPIResponse(data,textStatus,jqXHR);update()});' />
			<% } %>
			</form>
		</div>
	</div>

	<div id="form" class="bordered">
		<form>
			<table>
				<tbody>
					<tr>
						<td><input type="text" name="link"
							onKeyPress='if(event.keyCode == 13){$("#submit").trigger("click");}return event.keyCode != 13;'
							autocomplete="off" /></td>
						<td><input id="submit" type="button" name="Button"
							value="Abschicken"
							onclick='$.ajax({url: "/api/songs",	data:$("input[name=link]").val(),	method: "POST",	headers: {"Authorization": (typeof authtoken !== "undefined")?authtoken:""},contentType: false}).done(function(data,textStatus,jqXHR) {handleAPIResponse(data,textStatus,jqXHR);update()}).fail(function(data,textStatus,jqXHR) {handleAPIResponse(data,textStatus,jqXHR);update()});$("input[name=link]").val("");' />
						</td>
					</tr>
				</tbody>
			</table>

		</form>
	</div>
	<%@ include file="footer.jsp"%>