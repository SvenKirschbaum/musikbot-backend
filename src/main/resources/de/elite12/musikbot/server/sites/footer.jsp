<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.*"%>
<%@ page import="java.net.URL"%>
<%@ page import="sun.net.www.content.text.PlainTextInputStream"%>
<%@ page import="java.io.InputStream"%>
<%@ page import="de.elite12.musikbot.server.util.Util"%>

<%
	if (request.getAttribute("worked") == null) {
		response.sendRedirect("/");
		return;
	}
	SimpleDateFormat time = new SimpleDateFormat("H:mm");
%>
<%@ page import="de.elite12.musikbot.server.*"%>
<div id="footer">
	<%
		if (user == null) {
	%>
	<div id="login">
		<span onclick="show(loginbox)" class="link">Login</span> <span
			class="link"><a href="/register/">Registrieren</a></span>
		<div id="loginbox">
			<form method="post" action="/">
				<input type="hidden" name="action" value="login" />
				<table>
					<tbody>
						<tr>
							<td colspan="2"></td>

							<td onclick="hide(loginbox);" onmouseover="pointer();"
								onmouseout="returncursor();">x</td>
						</tr>
						<tr>
							<td>Username:</td>
							<td><input type="text" name="user" /></td>
							<td></td>

						</tr>
						<tr>
							<td>Passwort:</td>
							<td><input type="password" name="password" /></td>
							<td></td>

						</tr>
						<tr>

							<td></td>
							<td><input class="button" type="submit" value="Einloggen" />

							</td>
							<td></td>


						</tr>
					</tbody>
				</table>
			</form>
		</div>
	</div>
	<%
		} else {
	%>
	<div id="login">
		<div id="profilbild" class="tooltip">
			<img alt="profilbild" src="https://www.gravatar.com/avatar/<%= Util.md5Hex(user.getEmail().toLowerCase(Locale.GERMAN)) %>?s=20" />
			<div class="bordered"><img alt="profilbild" src="https://www.gravatar.com/avatar/<%= Util.md5Hex(user.getEmail().toLowerCase(Locale.GERMAN)) %>?s=350" /></div>
		</div>
		<form id="logoutform" action="/" method="post">
			<input type="hidden" name="action" value="logout" />
		</form>
		<div class="userbar">
			Willkommen
			<a href="/user/<%=user.getName()%>"><%=user.getName()%></a><span
				onclick="document.getElementById('logoutform').submit()"
				class="link">(Logout)</span>
				
		</div>
	</div>

			
			<div id="amenu">
				<span class="link" onclick="togglevis(amenubox)">Menü</span>
				<div id="amenubox">
					<ul>
						<li><a href="/">Startseite</a></li>
						<li><a href="/archiv/">Archiv</a></li>
						<li><a href="/statistik/">Statistik</a></li>
						<li><a href="/tokens/">Auth-Token</a></li>
						<%
							if (user.isAdmin()) {
						%>
							<li><a href="/import/">Playlist Importieren</a></li>
							<li><a href="/songs/">Gesperrte Songs</a></li>
							<li><a href="/gapcloser/">Gapcloser</a></li>
							<li><a href="/log/">Log</a></li>
							<li><a href="/whoisonline/">Wer ist online?</a></li>
							<li><a href="/debug/">Entwicklermenü</a></li>
						<%
							}
						%>
					</ul>
				</div>
			</div>
	<%
		}
	%>
	
	<!--
		<div id="styleselect">
			<span class="link" onclick="togglevis(styleselectbox)">Style ändern</span>
			<div id="styleselectbox">
				<ul>
					<li><a href="/setstyle/radio">radio</a></li>
				</ul>
			</div>
		</div>
	 -->
	
	<span id="stats" class="link"><a href="/statistik/">Statistik</a></span>
	
	<span id="impressum" class="link"><a href="https://datenschutz.elite12.de/">Impressum/Disclaimer/Datenschutz</a></span>

	<div id="time"><%=time.format(new java.util.Date())%></div>

	<div id="branding">
	</div>

	<div id="spotify">
		<img alt="spotify Logo" src="/res/images/spotify.svg">
	</div>
	<div id="html5">
		<img alt="HTML5 Logo" src="/res/images/html5.svg">
	</div>
	<div class="version">v<%= ((Controller)request.getAttribute("control")).version %></div>
</div>
</body>
</html>
