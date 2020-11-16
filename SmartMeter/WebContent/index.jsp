<%@ page language="java" contentType="text/xml; charset=ISO-8859-1"
    pageEncoding="UTF-8" import="com.davehock.*,javax.comm.*" %><?xml version="1.0" encoding="UTF-8"?>
<%
SmartMeterManager smm = (SmartMeterManager) this.getServletContext().getAttribute(SmartMeterManager.getName());
if (request.getParameter("logging")!=null)
{
   smm.setLogging(request.getParameter("logging").equals("true"));
}   
%>
<SmartMeter>
    <Demand><%=smm.getDemand()%></Demand>
    <TimeStamp><%=smm.getTimestamp()%></TimeStamp>
    <SummationDelivered><%=smm.getSummationDelivered() %></SummationDelivered>
    <SummationReceived><%=smm.getSummationReceived() %></SummationReceived>
    <CurrentUsage><%=smm.getCurrentUsage() %></CurrentUsage>
    <CurrentUsageString><%=smm.getCurrentUsageString() %></CurrentUsageString>
    <StartDate><%=smm.getStartdate() %></StartDate>
    
    <Net><%=smm.getSummationNetRounded() %></Net>
    <TodayTotal><%=smm.getTodayTotal() %></TodayTotal>
    <TodayOnPeak><%=smm.getTodayOnPeak() %></TodayOnPeak>
    <TodayOffPeak><%=smm.getTodayOffPeak() %></TodayOffPeak>
    <TodaySemiPeak><%=smm.getTodaySemiPeak() %></TodaySemiPeak>
    <WattHourAt12AM><%=smm.getWattHourAt12AM()%></WattHourAt12AM>
    <WattHourAt6AM><%=smm.getWattHourAt6AM()%></WattHourAt6AM>
    <WattHourAt2PM><%=smm.getWattHourAt2PM()%></WattHourAt2PM>
    <WattHourAt4PM><%=smm.getWattHourAt4PM()%></WattHourAt4PM>
    <WattHourAt9PM><%=smm.getWattHourAt9PM()%></WattHourAt9PM>
    <WattHourAtBillingStart><%=smm.getWattHourAtBillingStart()%></WattHourAtBillingStart>
    <DaysSinceBillingStart><%=smm.getDaysSinceBillingStart()%></DaysSinceBillingStart>
    <AveragePerDayThisPeriod><%=smm.getAveragePerDayThisPeriod()%></AveragePerDayThisPeriod>
    <ProjectedThisPeriod><%=smm.getProjectedThisPeriod()%></ProjectedThisPeriod>
    <ProjectedOnPeakThisPeriod><%=smm.getProjectedOnPeakThisPeriod()%></ProjectedOnPeakThisPeriod>
    <ProjectedSemiPeakThisPeriod><%=smm.getProjectedSemiPeakThisPeriod()%></ProjectedSemiPeakThisPeriod>
    <ProjectedOffPeakThisPeriod><%=smm.getProjectedOffPeakThisPeriod()%></ProjectedOffPeakThisPeriod>
	<BillingStartPeakUsage><%=smm.getOnPeakUsageBillingPeriod()%></BillingStartPeakUsage>
	<BillingStartOffPeakUsage><%=smm.getOffPeakUsageBillingPeriod()%></BillingStartOffPeakUsage>
	<BillingStartSemiPeakUsage><%=smm.getSemiPeakUsageBillingPeriod()%></BillingStartSemiPeakUsage>
	<CurrentSolarGeneration><%=smm.getCurrentSolarGeneration()%></CurrentSolarGeneration>
    <Status><%=smm.getStatus() %></Status>
    <StatusMessage><%=smm.getStatusMessage() %></StatusMessage>    
</SmartMeter>
