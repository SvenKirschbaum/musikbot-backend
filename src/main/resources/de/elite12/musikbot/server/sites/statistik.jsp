<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="header.jsp"%>
<title>Elite12 - Musikbot</title>
</head>
<body>
	<div id="backlink" class="link">
		<a href="/">Zurück</a>
	</div>
	<div id="topic"></div>
	<div id="statistik">
		<div class="statistik">
			<div class="statsheadline">Am meisten gewünscht:</div>
			<table>
				<thead>
					<tr>
						<th>Nr.</th>
						<th>Titel</th>
						<th>Anzahl</th>
					</tr>
				</thead>
				<tbody>
					<% 
					int i = 1;
					while(((ResultSet)request.getAttribute("mostplayed")).next() && i<=10) {
				%>
					<tr>
						<td><%= i %>.</td>
						<td class="link"><a
							href="<%= ((ResultSet)request.getAttribute("mostplayed")).getString("SONG_LINK") %>"><%= ((ResultSet)request.getAttribute("mostplayed")).getString("SONG_NAME") %></a></td>
						<td><%= ((ResultSet)request.getAttribute("mostplayed")).getString("ANZAHL") %></td>
					</tr>
					<%
						i++;
					}
				%>
				</tbody>
			</table>
		</div>
		<div class="statistik">
			<div class="statsheadline">Am meisten geskippt:</div>
			<table>
				<thead>
					<tr>
						<th>Nr.</th>
						<th>Titel</th>
						<th>Anzahl</th>
					</tr>
				</thead>
				<tbody>
					<% 
					i = 1;
					while(((ResultSet)request.getAttribute("mostskipped")).next() && i<=10) {
				%>
					<tr>
						<td><%= i %>.</td>
						<td class="link"><a
							href="<%= ((ResultSet)request.getAttribute("mostskipped")).getString("SONG_LINK") %>"><%= ((ResultSet)request.getAttribute("mostskipped")).getString("SONG_NAME") %></a></td>
						<td><%= ((ResultSet)request.getAttribute("mostskipped")).getString("ANZAHL") %></td>
					</tr>
					<%
						i++;
					}
				%>
				</tbody>
			</table>
		</div>
		<div class="statistik">
			<div class="statsheadline">Top Wünscher:</div>
			<table>
				<thead>
					<tr>
						<th>Nr.</th>
						<th>Name</th>
						<th>Anzahl</th>
					</tr>
				</thead>
				<tbody>
					<% 
					i = 1;
					while(((ResultSet)request.getAttribute("topusers")).next() && i<=10) {
				%>
					<tr>
						<td><%= i %>.</td>
						<td>
							<% 
								try {
									UUID id = UUID.fromString(((ResultSet)request.getAttribute("topusers")).getString("AUTOR")); 
									%>Gast<%
								}
								catch (IllegalArgumentException e) {%> <%= ((ResultSet)request.getAttribute("topusers")).getString("AUTOR") %>
							<%} %>
						</td>
						<td><%= ((ResultSet)request.getAttribute("topusers")).getString("ANZAHL") %></td>
					</tr>
					<%
						i++;
					}
				%>
				</tbody>
			</table>
		</div>
		<div class="statistik">
			<div class="statsheadline">Allgemeines:</div>
			<table>
				<tbody>
					<tr>
						<td>User:</td>
						<td>
							<% ((ResultSet)request.getAttribute("allgemein")).next(); %><%= ((ResultSet)request.getAttribute("allgemein")).getInt(1) %></td>
					</tr>
					<tr>
						<td>Admins:</td>
						<td>
							<% ((ResultSet)request.getAttribute("allgemein")).next(); %><%= ((ResultSet)request.getAttribute("allgemein")).getInt(1) %></td>
					</tr>
					<tr>
						<td>Gäste:</td>
						<td>
							<% ((ResultSet)request.getAttribute("allgemein")).next(); %><%= ((ResultSet)request.getAttribute("allgemein")).getInt(1) %></td>
					</tr>
					<tr>
						<td>Wünsche:</td>
						<td>
							<% ((ResultSet)request.getAttribute("allgemein")).next(); %><%= ((ResultSet)request.getAttribute("allgemein")).getInt(1) %></td>
					</tr>
					<tr>
						<td>Skippes:</td>
						<td>
							<% ((ResultSet)request.getAttribute("allgemein")).next(); %><%= ((ResultSet)request.getAttribute("allgemein")).getInt(1) %></td>
					</tr>
					<tr>
						<td>Gesamte Dauer:</td>
						<td>
							<% ((ResultSet)request.getAttribute("allgemein")).next(); %><%= ((ResultSet)request.getAttribute("allgemein")).getInt(1)/60/60 %>
							Stunden
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
	<%
	try {
		((ResultSet)request.getAttribute("mostplayed")).close();
	}
	catch(SQLException e) {
		
	}
	try {
		((ResultSet)request.getAttribute("mostskipped")).close();
	}
	catch(SQLException e) {
		
	}
	try {
		((ResultSet)request.getAttribute("topusers")).close();
	}
	catch(SQLException e) {
		
	}
	try {
		((ResultSet)request.getAttribute("allgemein")).close();
	}
	catch(SQLException e) {
		
	}
	%>

	<%@ include file="footer.jsp"%>
