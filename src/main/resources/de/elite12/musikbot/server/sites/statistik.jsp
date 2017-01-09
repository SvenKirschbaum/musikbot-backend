<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="de.elite12.musikbot.server.Weblet.TopEntry"%>
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
					for(TopEntry e:((List<TopEntry>)request.getAttribute("mostplayed"))) {
				%>
					<tr>
						<td><%= i %>.</td>
						<td class="link"><a
							href="<%= e.getLink() %>"><%= e.getName() %></a></td>
						<td><%= e.getCount() %></td>
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
					for(TopEntry e:((List<TopEntry>)request.getAttribute("mostskipped"))) {
				%>
					<tr>
						<td><%= i %>.</td>
						<td class="link"><a
							href="<%= e.getLink() %>"><%= e.getName() %></a></td>
						<td><%= e.getCount() %></td>
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
					for(TopEntry e:((List<TopEntry>)request.getAttribute("topusers"))) {
				%>
					<tr>
						<td><%= i %>.</td>
						<td>
							<% 
								try {
									UUID id = UUID.fromString(e.getName()); 
									%>Gast<%
								}
								catch (IllegalArgumentException err) {%> <%= e.getName() %>
							<%} %>
						</td>
						<td><%= e.getCount() %></td>
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
							<%= ((List<TopEntry>)request.getAttribute("allgemein")).get(0).getCount() %></td>
					</tr>
					<tr>
						<td>Admins:</td>
						<td>
							<%= ((List<TopEntry>)request.getAttribute("allgemein")).get(1).getCount() %></td>
					</tr>
					<tr>
						<td>Gäste:</td>
						<td>
							<%= ((List<TopEntry>)request.getAttribute("allgemein")).get(2).getCount() %></td>
					</tr>
					<tr>
						<td>Wünsche:</td>
						<td>
							<%= ((List<TopEntry>)request.getAttribute("allgemein")).get(3).getCount() %></td>
					</tr>
					<tr>
						<td>Skippes:</td>
						<td>
							<%= ((List<TopEntry>)request.getAttribute("allgemein")).get(4).getCount() %></td>
					</tr>
					<tr>
						<td>Gesamte Dauer:</td>
						<td>
							<%= ((List<TopEntry>)request.getAttribute("allgemein")).get(5).getCount()/60/60 %>
							Stunden
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>

	<%@ include file="footer.jsp"%>
