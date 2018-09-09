package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

public class Email_fotonabor {
	
	static final String SOURCEDIR = "D:/Burunin/";
	static final String FOTONABOR = "D:/Фотонабор/";
	static final String OFSET = "D:/Фотонабор/Офсет/";
	static final String LOGFILE = "D:/history-log.txt";
	static final String LINESEPARATOR = System.getProperty("line.separator");
	
	/*static final String SOURCEDIR = "C:/temp/";
	static final String FOTONABOR = "C:/temp1/";
	static final String OFSET = "C:/temp1/Офсет/";
	static final String LOGFILE = "C:/history-log.txt";*/
	
	static final Logger logger = Logger.getLogger("MyLog");
	static final String TODAY;
	static final String SUCCESS = "SUCCESS";
	
	static {
		
		FileHandler fh = null;
		
		try {
			fh = new FileHandler(LOGFILE,  true);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		
		logger.addHandler(fh);
		//SimpleFormatter formatter = new SimpleFormatter();
		//fh.setFormatter(formatter);
		fh.setFormatter(new MyFormatter());
		
		// this removes the console handler since the ConsoleHandler is
		// registered with the parent logger from which all the loggers derive
		logger.setUseParentHandlers(false);
		
		Date date = Calendar.getInstance().getTime();
		DateFormat dateFormatter = new SimpleDateFormat("dd_MM_yy_");
		TODAY = dateFormatter.format(date);
	}
	
	public static void main(String[] args) {
		
		submitKey();
		
		if( !process() ) {
			System.out.println(">>> There were ERRORs. >>> Details: " + LOGFILE);
		} else {
			System.out.println(SUCCESS);
		}
		
		log("Process finished");
		log(" ");
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
		
	}
	
	private static void submitKey() {
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		Integer number = Integer.MAX_VALUE;

		while( number!=0 ) {
			
			System.out.print("Enter key to continue...");
			
			try {
				
				/*try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}*/
				
				number = Integer.valueOf(in.readLine());
				
			} catch (NumberFormatException | IOException e) {
				System.out.println("Wrong key, try again!");
			}
		}
	}
	
	private static boolean process() {
		
		System.out.println("Process started.");

		// Read a folder and files in it
		File f = new File(SOURCEDIR);

		if (!f.exists()) {
			log("No Dir");
			return false;
		}

		if (!f.isDirectory()) {
			log(f.getAbsolutePath() + " is not a dir");
			return false;
		}
						
		if(f.listFiles().length == 0) {
			log(SOURCEDIR + " contains no files to upload");
			return false;
		}
		
		if(f.listFiles().length == 1) {
			
			File[] files = f.listFiles();
			
			if(files[0].getName().contains("readme") || files[0].getName().contains("ReadMe")) {
				log(SOURCEDIR + " contains no files to upload");
				return false;
			}
			
		}
		
		File[] directoryFiles = f.listFiles();
		
		try {
			sendEmail(directoryFiles, TODAY);
		} catch (Exception e) {
			return false;
		}
		
		directoryFiles = f.listFiles();
		
		for (File file : directoryFiles) {
			
			String filename = file.getName();
			
			try {
				moveFile(filename);
			} catch (Exception e) {
				return false;
			}

		}
		
		directoryFiles = f.listFiles();
		
		if(directoryFiles.length > 1) {
			log(SOURCEDIR + " was not cleared out!");
			return false;
		}
		
		if(directoryFiles.length == 1) {
			String fileName = directoryFiles[0].getName();
			if(!fileName.contains("readme") & !fileName.contains("ReadMe")) {
				log(SOURCEDIR + " was not cleared out!");
				return false;
			}
		}
		
		return true;
		
	}

	private static void sendEmail(File[] directoryFiles, String today) throws Exception {
		
		try {
			
			// Create the email message
			MultiPartEmail email = new MultiPartEmail();
			email.setHostName("smtp.ukr.net");
			email.setSmtpPort(465);
			//active ukr.net email account and password
			email.setAuthenticator(new DefaultAuthenticator("real_email@ukr.net", "real_password"));
			email.setSSLOnConnect(true);
			
			//адресат
			email.addTo("print@burunin.com.ua");
			
			email.setFrom("osho-studio@ukr.net");
			email.setSubject("osho-studio, fotonabor");
			
			final StringBuilder sb = new StringBuilder();
			sb.append("Высылаем файлы на фотонабор. Смотрите вложения.\r\n");
			sb.append("Студия ОШО\r\n");
			sb.append("тел. 099-999-99-99 \r\n");
			sb.append("Андрей");
			
			email.setMsg(sb.toString());
			
			for (File file : directoryFiles) {
				
				String filename = file.getName();
				
				if(file.getName().contains("Резервная_копия") || file.getName().contains("Backup_of")) {
					file.delete();
					log("Резервная_копия — deleted");
					continue;
				}
				
				try {
					rename(filename);
					// Create the attachment
					EmailAttachment attachment = new EmailAttachment();
					attachment.setPath(SOURCEDIR + today + filename);
					attachment.setDisposition(EmailAttachment.ATTACHMENT);
					attachment.setDescription("fotonabor osho-studio");
					attachment.setName(today + filename);
					// add the attachment
					email.attach(attachment);
				} catch (Exception e) {
					new Exception();
				}
				
			}			
	
			// send the email
			email.send();
		
		} catch (EmailException e) {
			String message = ExceptionUtils.getStackTrace(e);
			log("ERROR. E-mail was not sent: " + message);
			throw new Exception();
		}
		
		log("Email was sent successfully.");
		
	}
	
	private static void rename(String oldFileName) throws Exception {

		File oldfile = new File(SOURCEDIR, oldFileName);
		File newfile = new File(SOURCEDIR, TODAY + oldFileName);

		if (oldfile.renameTo(newfile) ==  false) {
			log("Rename failed file " + oldFileName);
			throw new Exception();
		}

	}

	private static void moveFile(String name) throws Exception {

		//File file = new File(SOURCEDIR + name);
		//File dest = new File(FOTONABOR + name);
		
		File file = new File(SOURCEDIR, name);
		File dest = new File(FOTONABOR, name);
		
		if(file.getName().contains("readme") || file.getName().contains("ReadMe")) {
			
			//making fresh last modified date of readme.txt because the file could not change 
			file.setLastModified( new Date().getTime() );
			
			try {
				Files.copy(file.toPath(), dest.toPath());
			} catch (IOException e) {
				
				log("File " + name + " was already present.");
				dest = new File(FOTONABOR, TODAY + new Date().getTime() + "_" + name);
				
				try {
					Files.copy(file.toPath(), dest.toPath());
				} catch (Exception e1) {
					throw new Exception();
				}
			}
			
			File oldfile = new File(SOURCEDIR, name);
			File newfile = new File(SOURCEDIR, name.substring(9));

			if (oldfile.renameTo(newfile) ==  false) {
				log("Rename failed file " + name);
				throw new Exception();
			}
			
			return;
		}
		
		boolean stickPrintFlag = false;
		
		try {
			
			if(file.getName().contains("StickPrint") || 
					file.getName().contains("Stick_Print") || 
					file.getName().contains("Stickprint") ||
					file.getName().contains("Stick_print")) {
				stickPrintFlag = true;
				dest = new File(OFSET, name);
			}
			
			Files.move(file.toPath(), dest.toPath());
			
		} catch (IOException e) {
			
			log("File " + name + " was already present.");
			
			if(stickPrintFlag) {
				dest = new File(OFSET, TODAY + new Date().getTime() + "_" + name);
			} else {
				dest = new File(FOTONABOR, TODAY + new Date().getTime() + "_" + name);
			}
			
			try {
				Files.move(file.toPath(), dest.toPath());
			} catch (Exception e1) {
				throw new Exception();
			}
		}
	}
	
	private static void log(String message) {
		logger.info(message + LINESEPARATOR);
	}	
	
}
