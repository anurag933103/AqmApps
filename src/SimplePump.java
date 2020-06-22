import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.jms.*;
import javax.naming.*;

/*import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;*/

public class SimplePump {
	private static final String USAGE = "java edu.asupoly.heal.aqm.clients.SimplePump -url <URL> -device dylos|sensordrone -lat1 float -lat2 float -long1 float -long2 float -rate int -num int";
	
	private static final String URL   = "-url";
	private static final String RATE  = "-rate";
	private static final String NUM   = "-num";
	private static final String LAT1  = "-lat1";
	private static final String LONG1 = "-long1";
	private static final String LAT2  = "-lat2";
	private static final String LONG2 = "-long2";
	private static final String DEVICE = "-device";

	private static final String DYLOS_TEMPLATE_JSON = "{ \"type\":\"dylos\",\"deviceId\":\"DEVICE\",\"userId\":\"simple_pump\"," + " \"dateTime\":\"DATE_TIME\",\"smallParticle\":READING1,\"largeParticle\":READING2," + " \"geoLatitude\":GEO_LAT,\"geoLongitude\":GEO_LONG,\"geoMethod\":\"random\" }";
	private static final String SENSORDRONE_TEMPLATE_JSON = "{ \"coData\":READING1,\"dateTime\":\"DATE_TIME\",\"geoLongitude\":GEO_LONG," + " \"co2Data\":READING2,\"co2DeviceID\":\"UNKNOWN\",\"geoMethod\":\"random\",\"type\":\"sensordrone\"," + " \"pressureData\":-1,\"tempData\":-1,\"geoLatitude\":GEO_LAT, \"deviceId\":\"DEVICE\",\"humidityData\":-1 }";
			
	private static final int DEFAULT_RATE = 10; // number of pushes per minute
	private static final int DEFAULT_NUM = 20; // total number of pushes
	private static final float DEFAULT_LAT1  = 33.42f;  // these default coords draw a bounding box from tempe campus to poly campus
	private static final float DEFAULT_LONG1 = -111.963f;
	private static final float DEFAULT_LAT2  = 33.28f;
	private static final float DEFAULT_LONG2 = -111.6f;
	private static final String DEFAULT_URL  = "http://localhost:8080/aqm/aqmimport";
	private static final String DEFAULT_DEVICE_ID = "UNKNOWN";
	private static final int DYLOS_UPPER_BOUND_SMALL = 1500;
	private static final int SENSORDRONE_UPPER_BOUND_SMALL = 50;
	private static final int DYLOS_UPPER_BOUND_LARGE = 1500;
	private static final int SENSORDRONE_UPPER_BOUND_LARGE = 2000;
	
	/**
	 * The SimplePump will push sensor events to the specified server
	 * The command-line makes extensive use of flags UNIX style
	 * Two sensor types are presently support, Dylos and Sensordrone.
	 * Sample Dylos format (http://www.dylosproducts.com/dcairqumowip.html):
	 * { "type":"dylos","deviceId":"aqm1","userId":"user1",
	 *   "dateTime":"Fri May 29 13:27:09 MST 2020","smallParticle":93,"largeParticle":26,
	 *   "geoLatitude":33.3099177,"geoLongitude":-111.6726974,"geoMethod":"manual" }
	 * Sample Sensordrone format (https://www.kickstarter.com/projects/453951341/sensordrone-the-6th-sense-of-your-smartphoneand-be):
	 * { "coData":3,"dateTime":"20200529_124956","geoLongitude":-111.6823322,
	 *   "co2Data":5,"co2DeviceID":"UNKNOWN","geoMethod":"Network","type":"sensordrone",
	 *   "presureData":96464,"tempData":25,"geoLatitude":33.2993061,
	 *   "deviceId":"SensorDroneB8:FF:FE:B9:C3:FA","humidityData":32 }
	 *  
	 * @param args
	 */
	
	private TopicSession pubSession;
    private TopicPublisher publisher;
    private TopicConnection connection;

    /* Establish JMS publisher */
    public SimplePump(String topicName, String username, String password) throws Exception {
	// Obtain a JNDI connection - see jndi.properties
	InitialContext jndi = new InitialContext();
	// Look up a JMS connection factory
	TopicConnectionFactory conFactory = (TopicConnectionFactory)jndi.lookup("topicConnectionFactry");
	// Create a JMS connection
	connection = conFactory.createTopicConnection(username, password);
	// Create JMS session objects for publisher
	pubSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
	// Look up a JMS topic
	Topic chatTopic = (Topic) jndi.lookup(topicName);
	// Create a JMS publisher
	publisher = pubSession.createPublisher(chatTopic);
	publisher.setDeliveryMode(DeliveryMode.PERSISTENT);
    }	
    
    private boolean goPublish(String s) {
    	boolean rval = true;
    	try {
    	    // Create and send message using topic publisher
    	    TextMessage message = pubSession.createTextMessage();
    	    message.setText(s);
    	    publisher.publish(message);
    	} catch (Throwable thw1) {
    	    thw1.printStackTrace();
    	    rval = false;
    	}
    	return rval;
        }
	
	
	public static void main(String[] args) {
		try {
		float lat1 = DEFAULT_LAT1;
		float lat2 = DEFAULT_LAT2;
		float long1 = DEFAULT_LONG1;
		float long2 = DEFAULT_LONG2;
		int rate = DEFAULT_RATE;
		int num = DEFAULT_NUM;
		String device = DEFAULT_DEVICE_ID;
		String url = DEFAULT_URL;
		int sensorReading1=0, sensorReading2=0;
		float latitude=DEFAULT_LAT1, longitude=DEFAULT_LONG1;   // the actual coordinates for the sensor readings.
		
		// Process command-line params
		if (args.length % 2 != 1) {  // gotta have odd number of args
			System.out.println(USAGE);
			System.exit(-1);
		}
		
		SimplePump demo = new SimplePump(args[0], args[1], args[2]);

		for (int i = 3; i < args.length; i++) {
			// if I wasn't lazy I would've made a String enum or something
			System.out.println("Processing arg " + args[i] + " value " + args[i+1]);
			if (args[i].equals(URL)) url = args[++i];
			else if (args[i].equals(LAT1))   lat1   = Integer.parseInt(args[++i]);
			else if (args[i].equals(LAT2))   lat2   = Integer.parseInt(args[++i]);
			else if (args[i].equals(LONG1))  long1  = Integer.parseInt(args[++i]);
			else if (args[i].equals(LONG2))  long2  = Integer.parseInt(args[++i]);
			else if (args[i].equals(RATE))   rate   = Integer.parseInt(args[++i]);
			else if (args[i].equals(NUM))    num    = Integer.parseInt(args[++i]);
			else if (args[i].equals(DEVICE)) device = args[++i];
			else {
				System.out.println(USAGE); // we got some unexpected flag or value
				System.exit(-1);
			}
		}
		// let's arrange lat/long in ascending order to make our random num gen code easier
		if (lat1 > lat2) {
			float dummy = lat2;
			lat2 = lat1;
			lat1 = dummy;
		}
		if (long1 > long2) {
			float dummy = long2;
			long2 = long1;
			long1 = dummy;
		}
		// geoLocation within the lat/long bounding box
		Random randomGen = new Random(System.currentTimeMillis());
		String json = null;
	 
	    SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z YYYY");
	    		
	    for (int i = 0; i < num; i++) {
	    	String nextDate = dateFormatter.format(new Date(System.currentTimeMillis()));
	    	
	    	// need to know which sensor
	    	if (device.equals(DEFAULT_DEVICE_ID)) {
	    		device = randomGen.nextBoolean() ? "dylos" : "sensordrone";
	    	}
	    	// set device-specific values for readings
	    	if (device.equalsIgnoreCase("dylos")) {
	    		json = DYLOS_TEMPLATE_JSON;
	    		sensorReading1 = randomGen.nextInt(DYLOS_UPPER_BOUND_SMALL);
	    		sensorReading2 = randomGen.nextInt(DYLOS_UPPER_BOUND_LARGE);
	    	} else if (device.equalsIgnoreCase("sensordrone")) {
	    		json = SENSORDRONE_TEMPLATE_JSON;
	    		sensorReading1 = randomGen.nextInt(SENSORDRONE_UPPER_BOUND_SMALL);
	    		sensorReading2 = randomGen.nextInt(SENSORDRONE_UPPER_BOUND_LARGE);
	    	}

	    	// need to gen lat long within our box.
	    	latitude  = randomGen.nextFloat() * (lat2-lat1) + lat1;
	    	longitude = randomGen.nextFloat() * (long2-long1) + long1;
	    	
	    	// set the fields, same sub in both
	    	json = json.replace("READING1", ""+sensorReading1);
	    	json = json.replace("READING2", ""+sensorReading2);
	    	json = json.replace("GEO_LAT", ""+latitude);
	    	json = json.replace("GEO_LONG", ""+longitude);
	    	json = json.replace("DATE_TIME", nextDate);
	    	json = json.replace("DEVICE", device);
	    	
	    	// prepare the HTTP POST
			// From https://www.baeldung.com/httpclient-post-http-request with mods
//      	CloseableHttpClient client = HttpClients.createDefault();
// 		    HttpPost httpPost = new HttpPost(url);
// 	    	StringEntity entity = new StringEntity("["+json+"]");
// 	    	httpPost.setEntity(entity);
// 	    	httpPost.setHeader("Accept", "application/json");
// 	    	httpPost.setHeader("Content-type", "application/json");
	 
//	    	CloseableHttpResponse response = null;
//			try {
//				response = client.execute(httpPost);
//				client.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//				System.exit(-1);
//			} 
//	    	System.out.println("POSTed " + json + "\n with response code " + response.getCode());

	    	
			if (demo.goPublish(json)) {
			System.out.println("Published " + json);
			} else {
			System.out.println("Unable to publish " + json);
			}

	    	// wait for the next one
	    	try {
	    		Thread.sleep(60/rate * 1000);
	    	} catch (InterruptedException ie) {
	    		ie.printStackTrace();
	    		demo.connection.close();
				System.exit(0);
	    	}
	    }
	  }catch (Exception e) {
		    e.printStackTrace();
		}
	}
}
