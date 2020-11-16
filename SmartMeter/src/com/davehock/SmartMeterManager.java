package com.davehock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

//import javax.comm.*;
import gnu.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SmartMeterManager implements Runnable, SerialPortEventListener {

	
// sudo apt-get install librxtx-java
// http://rxtx.qbang.org/wiki/index.php/Download
	
	
   //FTDI USB Serial Device converter now attached to ttyUSB0
//	   private String port = "/dev/ttyUSB0";
	   private String port = "/dev/usbMeter";
// File: /etc/udev/rules.d/99_usbdevices.rules
//SUBSYSTEM=="tty", ATTRS{idVendor}=="1a86", ATTRS{idProduct}=="7523", SYMLINK+="usbPool", GROUP="dialout", MODE="0666"
//SUBSYSTEM=="tty", ATTRS{idVendor}=="0403", ATTRS{idProduct}=="8a28", SYMLINK+="usbMeter", GROUP="dialout", MODE="0666"
//dave@lubuntu:/usr/share/tomcat7/bin$ cat setenv.sh
 //export CATALINA_OPTS="-Dgnu.io.rxtx.SerialPorts=/dev/usbMeter:/dev/usbPool"

   private static final String fileNameTemplate = "meterdata_%s.txt";
  private static final String logDir = "/var/log/tomcat7/smartmeterlogs/";
	private static final int TIMEOUTSECONDS = 20;
	private static final int SLEEPTIMEMS = 100;
	
	private static final String RESPONSE_INSTANTANEOUSDEMAND = "InstantaneousDemand";
	   public static final String TAG_DEMAND = "Demand";

	public static final String RESPONSE_CONNECTIONSTATUS = "ConnectionStatus";

	public static final String RESPONSE_SUMMATIONDELIVERED = "CurrentSummationDelivered";
	public static final String TAG_SUMMATIONRECEIVED = "SummationReceived";
	   public static final String TAG_SUMMATIONDELIVERED = "SummationDelivered";

   public static final String RESPONSE_CURRENTPERIODUSAGE = "CurrentPeriodUsage";
   public static final String TAG_CURRENTUSAGE = "CurrentUsage";
   public static final String TAG_STARTDATE = "StartDate";
	
	
   public static final String TAG_TIMESTAMP = "TimeStamp";
	
	
	private static final String COMMAND_STRING = "<Command><Name>%s</Name></Command>";
	private static final String COMMAND_STRING_LONG = "<Command><Name>%s</Name><MeterMacId>%s</MeterMacId></Command>";
   private static final String CMD_GET_CONNECTION_STATUS = "get_connection_status";
   private static final String CMD_GET_CURRENT_SUMMATION_DELIVERED = "get_current_summation_delivered";
   
   private static final String CMD_GET_CURRENT_PERIOD_USAGE = "get_current_period_usage";	
	private static final String CMD_INITIALIZE = "initialize";
   private static final String CMD_RESTART = "restart";
   private static final String CMD_GET_DEVICE_INFO = "get_device_info";
   private static final String CMD_GET_SCHEDULE = "get_schedule";
   private static final String CMD_GET_METER_LIST = "get_meter_list";
   private static final String CMD_GET_METER_INFO = "get_meter_info";
   private static final String CMD_GET_NETWORK_INFO = "get_network_info";
   private static final String CMD_GET_TIME = "get_time";
   private static final String CMD_GET_MESSAGE = "get_message";
   private static final String CMD_GET_CURRENT_PRICE = "get_current_price";
   private static final String CMD_GET_INSTANTANEOUS_DEMAND = "get_instantaneous_demand";
   private static final String CMD_GET_LAST_PERIOD_USAGE = "get_last_period_usage";
   private static final String CMD_GET_PROFILE_DATA = "get_profile_data";
   
   private boolean done=false;
   
	private String status = "";
	private String statusMessage = "" ;
	private String timestamp;
	private String startdate;
	private long demand=0;
	private long currentUsage=0;
	private long summationDelivered=0;
	private long summationReceived=0;
	private Calendar now = Calendar.getInstance();
	
	private long wattHourAt12AM = 0L;
	private long wattHourAt2PM = 0L;
	private long wattHourAt6AM = 0L;
	private long wattHourAt4PM = 0L;
   private long wattHourAt9PM = 0L;
   private long wattHourAtBillingStart = 0L;
   private long daysSinceBillingStart = 0L;
   private long onPeakUsage = 0L;
   private long offPeakUsage = 0L;
   private long semiPeakUsage = 0L;
   private long onPeakUsageYesterday = 0L;
   private long offPeakUsageYesterday = 0L;
   private long semiPeakUsageYesterday = 0L;
		  
	private long dayOfYear = -1;
   private long lastMonth = -1;
   private  String meterMacIds[];


	private BufferedReader inputStream;
	private OutputStream outputStream;
	private SerialPort serialPort1;
	private Thread readThread;
   private String connectionStatusNotificationResponse="";
   private BufferedWriter logWriter = null;
   private boolean logging=false;
private String currentUsageString;

   

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		SmartMeterManager smm = new SmartMeterManager();
		System.out.print(smm.getCurrentSolarGeneration());
//		List<String> ports = smm.getPorts();
//		
//		smm.setPort(ports.get(ports.size()-1));
//		
//      System.out.println(smm.isWeekend());
//      System.out.println(smm.isHoliday());
//		System.out.println(smm.isOnPeak());
//      System.out.println(smm.isOffPeak());
//      System.out.println(smm.isSemiPeak());
//      System.out.println(smm.getTodaySemiPeak());
//      System.out.println(smm.getTodayOffPeak());
//      System.out.println(smm.getTodayOnPeak());
//      smm.init();
	

	}

	public SmartMeterManager() {

	}

	public void init() {

		try {
			CommPortIdentifier portId1 = CommPortIdentifier
					.getPortIdentifier(port);
			
			serialPort1 = (SerialPort) portId1.open("SmartMeterManager", 2000);

			serialPort1.setSerialPortParams(115200, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			serialPort1.notifyOnDataAvailable(true);
			serialPort1.addEventListener(this);
			outputStream = serialPort1.getOutputStream();
			inputStream = new BufferedReader(new InputStreamReader(
					serialPort1.getInputStream()));

			readThread = new Thread(this);
			readThread.start();
			
//	      sendConnectionStatusCommand();
//	      sendCommand(CMD_GET_METER_LIST);

		} catch (Exception e) {
			this.handleException(e);
		}

	}
	
	private void handleException (Exception e)
	{
		this.status = "Exception";
		
	    StackTraceElement[] elements = (e.getStackTrace());

	    StringBuffer buf = new StringBuffer();

	    for (int i = 0; i < elements.length; i++) {
	      buf.append("    " + elements[i].getClassName() + "."
	          + elements[i].getMethodName() + "(" + elements[i].getFileName() + ":"
	          + elements[i].getLineNumber() + ")");
	    }


		this.statusMessage = (new Date()).toString() + " " + e.toString() + " StackTrace=["+buf.toString()+ " " + this.port;
 	   log(this.statusMessage);
	}

	public List<String> getPorts() {
		List<String> ports = new ArrayList<String>();
		// get list of ports available on this particular computer,
		// by calling static method in CommPortIdentifier.
		Enumeration pList = CommPortIdentifier.getPortIdentifiers();

		// Process the list, putting serial and parallel into ComboBoxes
		while (pList.hasMoreElements()) {
			CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
			if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				ports.add(cpi.getName());
			}
		}
		return ports;
	}

	public void serialEvent(SerialPortEvent event) {

		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			StringBuffer readBuffer = new StringBuffer();
			String line = "";
			try {
				do {
					line = inputStream.readLine();
					readBuffer.append(line);
					readBuffer.append("\r\n");
//	println("READ: " + line);  //debug log
				} while (!line.startsWith("</"));
				
				String result = readBuffer.toString();

 			    log(result);

				if (!result.startsWith("<"))
				   break; //XML Fragment
				if (result.contains(RESPONSE_CONNECTIONSTATUS))
				{
					this.connectionStatusNotificationResponse = result;
				}
				else if (result.contains(RESPONSE_INSTANTANEOUSDEMAND))
					this.readInstantaneousDemand(result);
				else if (result.contains(RESPONSE_CURRENTPERIODUSAGE))
					this.readCurrentPeriodUsage(result);
				else if (result.contains(RESPONSE_SUMMATIONDELIVERED))
					this.readSummationDelivered(result);
			} catch (IOException e) {
				this.handleException(e);
			}

			break;
		}
	}
	

   @Override
   public void run() {

      DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      DateFormat sdfDay = new SimpleDateFormat("yyyy-MM");

      while (!done) {
         try {
            sendCommand(CMD_GET_CURRENT_SUMMATION_DELIVERED);
            Thread.sleep(SLEEPTIMEMS);
            
            sendCommand(CMD_GET_CURRENT_PERIOD_USAGE);
            Thread.sleep(SLEEPTIMEMS);

            if (setDailyData()) //log only if crossed billing threshold
            {
               String logEntry = String.format("%s,%d\r\n", sdf.format(new Date()), getSummationNet());

               long currentMonth = getNow().get(Calendar.MONTH);

               if (lastMonth != currentMonth || logWriter == null) {
                  lastMonth = currentMonth;
                  if (logWriter != null)
                     logWriter.close();
                  logWriter = new BufferedWriter(new FileWriter(new File(String.format(logDir+fileNameTemplate, sdfDay.format(getNow().getTime()))), true)); // append
                  // close file and create new
               }
               logWriter.write(logEntry);      
               logWriter.flush();
            }            
         } catch (Exception e) {
            this.handleException(e);
         }
      }
   }	
   
   private boolean setDailyData() {
      boolean isBillingThresholdTime=true;
      Calendar currentTime = getNow();
      long currentDay = currentTime.get(Calendar.DAY_OF_YEAR);
      long hour=currentTime.get(Calendar.HOUR_OF_DAY);

      if (currentDay!=this.dayOfYear)
      {
         daysSinceBillingStart++;
         this.offPeakUsage += this.offPeakUsageYesterday;
         this.semiPeakUsage += this.semiPeakUsageYesterday;
         this.onPeakUsage += this.onPeakUsageYesterday;
         
         this.wattHourAt12AM=this.getSummationNet();
         this.wattHourAt2PM=0;
         this.wattHourAt6AM=0;
         this.wattHourAt4PM=0;
         this.wattHourAt9PM=0;
         this.dayOfYear=currentDay;
         if(currentTime.get(Calendar.DAY_OF_MONTH)==12)
         {
            wattHourAtBillingStart = this.getSummationNet();
            daysSinceBillingStart = 1;
            this.offPeakUsage = 0L;
            this.semiPeakUsage = 0L;
            this.onPeakUsage = 0L;
         }
      }
      else if (wattHourAt6AM==0 && hour==6)
         this.wattHourAt6AM=this.getSummationNet();
      else if (wattHourAt2PM==0 && hour==14)
         this.wattHourAt2PM=this.getSummationNet();
      else if (wattHourAt4PM==0 && hour==16)
         this.wattHourAt4PM=this.getSummationNet();
      else if (wattHourAt9PM==0 && hour==21)
         this.wattHourAt9PM=this.getSummationNet();
      else
         isBillingThresholdTime=false;
      
      //Preserve latest values to use after midnight
      this.onPeakUsageYesterday=this.getTodayOnPeak();
      this.semiPeakUsageYesterday=this.getTodaySemiPeak();
      this.offPeakUsageYesterday=this.getTodayOffPeak();
      
      //initialize if tomcat started today
      if (wattHourAt12AM==0) wattHourAt12AM=getSummationNet();
      if (wattHourAtBillingStart==0) wattHourAtBillingStart=getSummationNet();
      if (wattHourAt6AM==0 && hour>=6) wattHourAt6AM=getSummationNet();
      if (wattHourAt2PM==0 && hour>=14) wattHourAt2PM=getSummationNet();
      if (wattHourAt4PM==0 && hour>=16) wattHourAt4PM=getSummationNet();
      if (wattHourAt9PM==0 && hour>=21) wattHourAt9PM=getSummationNet();
      
      return isBillingThresholdTime;
   }

   private void setTimeStamp (Document doc)
   {
      this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      .format(new Date(
            (Integer.decode(getTagValue(doc, TAG_TIMESTAMP))) * 1000L));      
   }
   
   private void setStartdate(Document doc) {
      this.startdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    	      .format(new Date(
    	            (Integer.decode(getTagValue(doc, TAG_STARTDATE))) * 1000L));      
	}


	public void readInstantaneousDemand(String instantaneousDemandNotificationResponse){
      if (instantaneousDemandNotificationResponse.length()==0)
         return;
		try {
         Document doc = this.parseMessage(instantaneousDemandNotificationResponse);
         this.demand = (int) Long.parseLong(getTagValue(doc, TAG_DEMAND).replace("0x",""), 16);
         this.setTimeStamp(doc);
     } catch (Exception e) {
         this.handleException(e);
      }
	}
	
	  public void readSummationDelivered(String currentSummationDeliveredResponse) {
         if (currentSummationDeliveredResponse.length()==0)
            return;
         try {
   	      Document doc = this.parseMessage(currentSummationDeliveredResponse);
   	      this.summationDelivered = Long.decode(getTagValue(doc, TAG_SUMMATIONDELIVERED));
   	      this.summationReceived = Long.decode(getTagValue(doc, TAG_SUMMATIONRECEIVED));
            this.setTimeStamp(doc);
         } catch (Exception e) {
            this.handleException(e);
         }
	   }
	  
     public void readCurrentPeriodUsage(String currentPeriodUsageResponse) {
        if (currentPeriodUsageResponse.length()==0)
           return;
        try {
           Document doc = this.parseMessage(currentPeriodUsageResponse);
           this.currentUsageString = getTagValue(doc, TAG_CURRENTUSAGE);
//           this.currentUsage = Long.decode(this.currentUsageString);
           this.currentUsage = (int) Long.parseLong(this.currentUsageString.replace("0x",""), 16);
           this.setStartdate(doc);
        } catch (Exception e) {
           this.handleException(e);
        }
     }

	public String sendConnectionStatusCommand() throws Exception {
		this.sendCommand(CMD_GET_CONNECTION_STATUS);

		Date startTime = new Date();
		while (connectionStatusNotificationResponse.length() == 0) {
			Thread.sleep(SLEEPTIMEMS);
			if ((new Date()).getTime() - startTime.getTime() > TIMEOUTSECONDS * 1000)
				throw new Exception("Timeout reading connectionStatus");
		}
		Document doc = this.parseMessage(connectionStatusNotificationResponse);
		this.status = getTagValue(doc, "Status");
		this.statusMessage = getTagValue(doc, "Description");
		return connectionStatusNotificationResponse;
	}
	
	  public void sendCommand(String command) throws Exception
	   {
	      outputStream
	      .write(String.format(COMMAND_STRING, command)
	            .getBytes());
	   }

	  public void sendCommandLong(String command, String meterMacId) throws Exception
	   {
	      outputStream
	      .write(String.format(COMMAND_STRING_LONG, command, meterMacId)
	            .getBytes());
	   }

	public void close() throws IOException {
		if (outputStream != null)
			outputStream.close();
		if (inputStream != null)
			inputStream.close();
		if (serialPort1 != null)
			serialPort1.close();
		if (logWriter != null)
		   logWriter.close();
	}

	private Document parseMessage(String message)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(new InputSource(new StringReader(message)));
	}

	private static String getTagValue(Document doc, String tagName) {
		Node node = doc.getDocumentElement();
		if (node == null)
			return null;
		NodeList nodes = ((Element) node).getElementsByTagName(tagName);
		if (nodes == null || nodes.getLength() == 0)
			return null;
		String content = nodes.item(0).getTextContent();
		if (content == null || content.trim().length() == 0)
			return null;
		return content;
	}
	
	private String xmlEncode(String data)
	{
      data = data.replaceAll("&", "&amp;");
	   data = data.replaceAll(">", "&gt;");
      data = data.replaceAll("<", "&lt;");
      return data;
	}

	public String getStatus() {
		if (status==null) return "";
		return status;
	}

	public String getStatusMessage() {
		if (statusMessage==null) return "";
		return xmlEncode(statusMessage);
	}

	public String getTimestamp() {
		if (timestamp==null) return "";
		return timestamp;
	}
	
	public String getStartdate() {
		if (startdate==null) return "";
		return startdate;
	}

	public long getDemand() {
		return demand;
	}

	public String getPort() {
		return port;
	}

   public long getCurrentUsage() {
      return currentUsage;
   }

   public void setCurrentUsage(long currentUsage) {
      this.currentUsage = currentUsage;
   }
   
   public long getSummationNet()
   {
      return summationDelivered - summationReceived;
   }

   public long getSummationNetRounded()
   {
      return (summationDelivered - summationReceived + 500)/1000;
   }

   public long getSummationDelivered() {
      return summationDelivered;
   }

   public void setSummationDelivered(long summationDelivered) {
      this.summationDelivered = summationDelivered;
   }

   public long getSummationReceived() {
      return summationReceived;
   }

   public void setSummationReceived(long summationReceived) {
      this.summationReceived = summationReceived;
   }
   
   private boolean isSummer()
   {
      //May thru Oct
      Calendar currentTime = getNow();
      return currentTime.get(Calendar.MONTH)>=Calendar.MAY && currentTime.get(Calendar.MONTH)<=Calendar.OCTOBER;
   }
   
   private boolean isWeekend()
   {
      Calendar currentTime = getNow();
      if (currentTime.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY || currentTime.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY)
         return true;
      
      return false;
   }
      
   private boolean isHoliday()
   {
      Calendar currentTime = getNow();
      //holidays
      int month = currentTime.get(Calendar.MONTH);
      int day = currentTime.get(Calendar.DAY_OF_MONTH);
      
      if (month==Calendar.JANUARY && day==1) return true;
      if (month==Calendar.JULY && day==4) return true;
      if (month==Calendar.DECEMBER && day==25) return true;
      
      //TODO, Thanksgiving, Memorial Day, Labor Day...
          
      return false;
   }
   
   public boolean isOffPeak()
   {
      if (!isOnPeak() && !isSemiPeak()) return true;
      return false;
   }
   
   public boolean isSemiPeak()
   {
      Calendar currentTime = getNow();
      int hour = currentTime.get(Calendar.HOUR_OF_DAY);
      if (isHoliday() || isWeekend())
         return false;
      if(isSummer())
      {        
         if ((hour>=6 && hour<11) || (hour>=18 && hour<=23))
            return true;
      }
      else
      {
         if (hour>=6 && hour<18)
            return true;
      }
            
      return false;
   }
   
   public boolean isOnPeak()
   {
      Calendar currentTime = getNow();

      int hour = currentTime.get(Calendar.HOUR_OF_DAY);
	    if (hour>=16 && hour<21)
	       return true;
            
      return false;
   }

   
   public long getTodayTotal()
   {
      return getSummationNet()-getWattHourAt12AM();
   }
   
   public long getTodayOnPeak()
   {
      Calendar currentTime = getNow();     
      int hour = currentTime.get(Calendar.HOUR_OF_DAY);
      
      if (hour<16) return 0;
      
      return getWattHourAt9PM()-getWattHourAt4PM();
   }
  
   public long getTodayOffPeak()
   {
      long usage=0;
      
      if (isWeekend() || isHoliday())
      {
          usage = getWattHourAt2PM() - getWattHourAt12AM();    	  
      } else {
          usage = getWattHourAt6AM() - getWattHourAt12AM();    	  
      }
      
      return usage;
   }
   
   public long getTodaySemiPeak()
   {
      Calendar currentTime = getNow();

      int hour = currentTime.get(Calendar.HOUR_OF_DAY);
      
      long usage=0;
      if (isWeekend() || isHoliday())
      {
          if (hour>=14) { 
              usage += getWattHourAt4PM()-getWattHourAt2PM();        
              if (hour>=21)
                 usage += this.getSummationNet()-getWattHourAt9PM();
          }
      }
      else
      {
         if (hour>=6) { 
             usage += getWattHourAt4PM()-getWattHourAt6AM();        
             if (hour>=21)
                usage += this.getSummationNet()-getWattHourAt9PM();
         }
      }

      return usage;
   }

   public void setPort(String port) {
      this.port = port;
   }

   public void interrupt() {
     done=true;      
   }

   public boolean isAlive() {
      return true;
   }
   
   public static String getName()
   {
      return "SmartMeterManager";
   }

   public long getWattHourAt12AM() {
      return wattHourAt12AM;
   }
   
   public long getWattHourAtBillingStart() {
      return wattHourAtBillingStart;
   }

   public long getDaysSinceBillingStart() {
      return daysSinceBillingStart;
   }

   public long getAveragePerDayThisPeriod() {
	  if (daysSinceBillingStart==0) return 0;
	  return (getSummationNet()-wattHourAtBillingStart)/daysSinceBillingStart;
   }
	   
   public long getProjectedThisPeriod() {
	  if (daysSinceBillingStart==0) return 0;
	  return 30*(getSummationNet()-wattHourAtBillingStart)/daysSinceBillingStart;
   }
   
   public long getProjectedOnPeakThisPeriod() {
	  if (daysSinceBillingStart==0) return 0;
	  return 30*(this.onPeakUsage)/daysSinceBillingStart;
   }
   
   public long getProjectedSemiPeakThisPeriod() {
	  if (daysSinceBillingStart==0) return 0;
	  return 30*(this.semiPeakUsage)/daysSinceBillingStart;
   }
   
   public long getProjectedOffPeakThisPeriod() {
	  if (daysSinceBillingStart==0) return 0;
	  return 30*(this.offPeakUsage)/daysSinceBillingStart;
   }
	   
   public long getWattHourAt2PM() {
      if (wattHourAt2PM==0) return this.getSummationNet();
      return wattHourAt2PM;
   }

   public long getWattHourAt6AM() {
      if (wattHourAt6AM==0) return this.getSummationNet();
      return wattHourAt6AM;
   }

   public long getWattHourAt4PM() {
      if (wattHourAt4PM==0) return this.getSummationNet();
      return wattHourAt4PM;
   }

   public long getWattHourAt9PM() {
      if (wattHourAt9PM==0) return this.getSummationNet();
      return wattHourAt9PM;
   }
   private Calendar getNow()
   {
      now.setTime(new Date());
      return now;
   }

   public boolean isLogging() {
      return logging;
   }

   public void setLogging(boolean logging) {
      this.logging = logging;
   }

   private void log(String string)
   {
      if (logging)
      System.out.println(string);
   }

public String getCurrentUsageString() {
	return currentUsageString;
}

public long getOnPeakUsageBillingPeriod() {
	return onPeakUsage;
}

public long getOffPeakUsageBillingPeriod() {
	return offPeakUsage;
}

public long getSemiPeakUsageBillingPeriod() {
	return semiPeakUsage;
}

public long getCurrentSolarGeneration() {
	long generation = 0;
	URL url;
	try {
		url = new URL("http://192.168.30.126/home");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		String target="<td>Currently generating</td>    <td>";
		String value;
		int start = content.indexOf(target)+target.length();
		int end = content.indexOf(" kW");
		int multiplier = 1000;
		if (end==-1)
		{
			end = content.indexOf(" W</td>");
			multiplier = 1;
		}
		if (end!=-1)
		{
			value = content.substring(start, end).trim();
			generation = (long)(Double.parseDouble(value)*multiplier);
		}
	} catch (Exception e) {
		this.handleException(e);
	}
	return generation;
}

}
