<%@ page language="java" contentType="text/xml; charset=ISO-8859-1"
    pageEncoding="UTF-8" import="java.io.*,com.davehock.*,org.json.*,java.net.*" %><?xml version="1.0" encoding="UTF-8"?>
<%
SmartMeterManager smm = (SmartMeterManager) this.getServletContext().getAttribute(SmartMeterManager.getName());
String url=request.getParameter("url");
url="https://api.enphaseenergy.com/api/systems/254278/summary?key=ae4bcc2e3f0f6ff2b5b95e0cabffe4b1";
InputStream is = (InputStream) (new URL(url)).getContent();

JSONTokener tokener = new JSONTokener(is);
JSONObject root = new JSONObject(tokener);
%>
<enphase><%=XML.toString(root)%></enphase>
