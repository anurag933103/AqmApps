import javax.jms.*;
import javax.naming.*;
import org.apache.log4j.BasicConfigurator;

import edu.asupoly.heal.aqm.dmp.AQMDAOFactory;
import edu.asupoly.heal.aqm.dmp.IAQMDAO;
import edu.asupoly.heal.aqm.model.ServerPushEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimplePumpSubscriber implements javax.jms.MessageListener{
	private static Logger log = Logger.getLogger(SimplePumpSubscriber.class.getName());
	private TopicSession pubSession;
    private TopicConnection connection;
    
    public SimplePumpSubscriber(String topicName, String clientName, String username, String password)
    		throws Exception {
    	// Obtain a JNDI connection
    	InitialContext jndi = new InitialContext();
    	// Look up a JMS connection factory
    	TopicConnectionFactory conFactory = (TopicConnectionFactory)jndi.lookup("topicConnectionFactry");
    	// Create a JMS connection
    	connection = conFactory.createTopicConnection();
    	connection.setClientID(clientName);  // this is normally done by configuration not programmatically
    	// Look up a JMS topic - see jndi.properties in the classes directory
    	Topic chatTopic = (Topic) jndi.lookup(topicName);
    	TopicSession subSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
    	TopicSubscriber subscriber = subSession.createDurableSubscriber(chatTopic, "SimplePumpSubscriber");

    	subscriber.setMessageListener(this);  // so we will use onMessage
    	
    	// Start the JMS connection; allows messages to be delivered
    	connection.start();
        }

       public void onMessage(Message message) {
            try {
                if (message instanceof TextMessage) {
                    TextMessage txtMessage = (TextMessage) message;
                    System.out.println("Message received: " + txtMessage.getText());
                    processJson(txtMessage.getText());
                } else {
                    System.out.println("Invalid message received.");
                }
            } catch (JMSException e1) {
    	    e1.printStackTrace();
    	}
       }
       
       public void processJson(String jsonString) {
    	   int appReturnValue = ServerPushEvent.PUSH_UNSET;
    	   char firstBracket = '[';
    	   char secondBracket = ']';
    	   jsonString = firstBracket + jsonString;
    	   jsonString = jsonString + secondBracket;
    	   try {
    		   log.info("Getting Dao Object ");
			   IAQMDAO dao = AQMDAOFactory.getDAO();
			   log.info(" Dao Object is "+dao);
			   appReturnValue = (dao.importReadings(jsonString)) ? ServerPushEvent.SERVER_PUSH_OK : ServerPushEvent.SERVER_IMPORT_FAILED;
    	   }catch (StreamCorruptedException sce) {
    	       log.log(Level.SEVERE, "Server pushed stacktrace on response: " + sce);
    	       appReturnValue = ServerPushEvent.SERVER_STREAM_CORRUPTED_EXCEPTION;
   		   } catch (IOException ie) {
   			   log.log(Level.SEVERE, "Server pushed stacktrace on response: " + ie);
   			   appReturnValue = ServerPushEvent.SERVER_IO_EXCEPTION;
   		   } catch (SecurityException se) {
   			   log.log(Level.SEVERE, "Server pushed stacktrace on response: " + se);
   			   appReturnValue = ServerPushEvent.SERVER_SECURITY_EXCEPTION;
   		   } catch (NullPointerException npe) {
   			   log.log(Level.SEVERE, "Server pushed stacktrace on response: " + npe);
   			   appReturnValue = ServerPushEvent.SERVER_NULL_POINTER_EXCEPTION;
   		   } catch (Throwable t) {
   			   log.log(Level.SEVERE, "Server pushed stacktrace on response: " + t);
   			   appReturnValue = ServerPushEvent.SERVER_UNKNOWN_ERROR;
   		   }
   			log.info("Server returning value to subscriber: " + appReturnValue);
       }
        
        public static void main(String[] args) {
    	// uncomment this line for verbose logging to the screen
    	// BasicConfigurator.configure();
    	try {
    	    if (args.length != 4)
    		System.out.println("Please Provide the topic name,unique client id, username,password!");

    	    SimplePumpSubscriber demo =
    		new SimplePumpSubscriber(args[0], args[1], args[2], args[3]);
    	    BufferedReader commandLine = new java.io.BufferedReader(new InputStreamReader(System.in));
    	    
    	    // closes the connection and exit the system when 'exit' enters in
    	    // the command line
    	    while (true) {
    		String s = commandLine.readLine();
    		if (s.equalsIgnoreCase("exit")) {
    		    demo.connection.close();
    		    System.exit(0);
    		}
    	    }
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
        }
    }
