package mainWindow;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

public class LoggingUtilities {
	
	enum LOG_TYPES {START, END, INPUT_SOLUTION, /*these are for the survey window only*/
		BUTTON_CLICK, TEXT_ENTER, QUERY_PLAN_NODE_CLICK, OTHER};/*these are for the debugger window only*/

	public LoggingUtilities ()
	{
		String desktopPath = System.getProperty("user.home") + "/Desktop";
		desktopPath = desktopPath.replace("\\", "/");
		String timestamp = getCurrentTimestamp();
		filename = desktopPath + "/"+ filename_partial +"_"+ timestamp +".csv";;
		System.out.print(filename);
		
		try 
		{
			writer = new PrintWriter(filename, encoding);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void log(LOG_TYPES logType, String log)
	{
		String logContent = "";
		switch(logType)
		{
			case BUTTON_CLICK:
			{
				logContent = logContent + "[BUTTON_CLICK] ";
				break;
			}
			
			case TEXT_ENTER:
			{
				logContent = logContent +  "[TEXT_ENTER] ";
				break;
			}
			
			case QUERY_PLAN_NODE_CLICK:
			{
				logContent = logContent +  "[NODE_CLICK] ";
				break;
			}
			
			case OTHER:
			{
				logContent = logContent +  "[OTHER] ";
				break;
			}
		}
		
		logContent = logContent + ", " + getCurrentTimestampShort() + "";
		logContent = logContent + ", " + log + "";
		writer.println(logContent);
	}
	
	public void sendLog()
	{
		writer.close();
		String logs = "";
		try {
			logs = readFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendFromGMail(sender, pwd, new String[] {receipt}, "584log", logs);
		
		System.out.println("\nlog sent");
	}
	
	private String readFile() throws IOException
	{
		 byte[] encoded = Files.readAllBytes(Paths.get(filename));
		 return new String(encoded, encoding);
	}
	
	private String getCurrentTimestamp()
	{
		String timestamp = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
		
		return timestamp;
	}
	
	private String getCurrentTimestampShort()
	{
		String timestamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		
		return timestamp;
	}
	
	private void sendFromGMail(String from, String pass, String[] to, String subject, String body) 
	{
       Properties props = System.getProperties();
       String host = "smtp.gmail.com";
       props.put("mail.smtp.starttls.enable", "true");
       props.put("mail.smtp.host", host);
       props.put("mail.smtp.user", from);
       props.put("mail.smtp.password", pass);
       props.put("mail.smtp.port", "587");
       props.put("mail.smtp.auth", "true");

       Session session = Session.getDefaultInstance(props);
       MimeMessage message = new MimeMessage(session);

       try {
           message.setFrom(new InternetAddress(from));
           InternetAddress[] toAddress = new InternetAddress[to.length];

           // To get the array of addresses
           for( int i = 0; i < to.length; i++ ) {
               toAddress[i] = new InternetAddress(to[i]);
           }

           for( int i = 0; i < toAddress.length; i++) {
               message.addRecipient(Message.RecipientType.TO, toAddress[i]);
           }

           message.setSubject(subject);
           //message.setText(body);
           
           Multipart multipart = new MimeMultipart();
           String attachmentName = filename.substring(filename.indexOf(filename_partial));
           // part 1
           MimeBodyPart messageBodyPart = new MimeBodyPart();
           messageBodyPart.setText(attachmentName);
           multipart.addBodyPart(messageBodyPart);
           // part 2
           messageBodyPart = new MimeBodyPart();
           DataSource source = new FileDataSource(filename);
           messageBodyPart.setDataHandler( new DataHandler(source));
           messageBodyPart.setFileName(attachmentName);
           multipart.addBodyPart(messageBodyPart);
           
           message.setContent(multipart);
           
           Transport transport = session.getTransport("smtp");
           transport.connect(host, from, pass);
           transport.sendMessage(message, message.getAllRecipients());
           transport.close();
       }
       catch (AddressException ae) {
           ae.printStackTrace();
       }
       catch (MessagingException me) {
           me.printStackTrace();
       }
   }
	
	//private String receipt = "584debuggerlog@umich.edu";
	private String receipt = "yjtang@umich.edu";
	private String sender = "584logger@gmail.com";
	private String pwd = "584loggerlogger";
	private PrintWriter writer = null;
	private String filename = ""; 
	private String encoding = "UTF-8";
	private String filename_partial = "584DebuggerLog";

	/*
	 * testing only
	 */
	public static void main(String [] args)
	{
		LoggingUtilities logger = new LoggingUtilities();
		logger.log(LOG_TYPES.BUTTON_CLICK, "action");
		logger.sendLog();
	}
}
