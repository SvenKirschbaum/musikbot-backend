<?xml version="1.0" encoding="UTF-8"?>
<% 
	if(request.getAttribute("worked") == null) {
		response.sendError(404);
		return;
	}
%>
<%@ page contentType="text/xml;charset=UTF-8" language="java"%>
<%@ page trimDirectiveWhitespaces="true"%>
<urlset
      xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9
            http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">
<url>
  <loc>https://musikbot.elite12.de/</loc>
  <priority>1.00</priority>
  <changefreq>always</changefreq>
</url>
<url>
  <loc>https://musikbot.elite12.de/archiv/</loc>
  <priority>0.80</priority>
  <changefreq>hourly</changefreq>
</url>
<url>
  <loc>https://musikbot.elite12.de/register/</loc>
  <priority>0.50</priority>
  <changefreq>never</changefreq>
</url>
<url>
  <loc>https://musikbot.elite12.de/statistik/</loc>
  <priority>0.70</priority>
  <changefreq>weekly</changefreq>
</url>
<url>
  <loc>https://musikbot.elite12.de/impressum/</loc>
  <priority>0.20</priority>
  <changefreq>monthly</changefreq>
</url>
</urlset>