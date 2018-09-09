package main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MyFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		/*return record.getThreadID() + "::" + record.getSourceClassName() + "::"
				+ record.getSourceMethodName() + "::"
				+ new Date(record.getMillis()) + "::" + record.getMessage()
				+ "\n";*/
		
		DateFormat dateFormatter = new SimpleDateFormat("dd_MM_yy ");
		//TODAY = dateFormatter.format(date);
		
		return 
			record.getSourceMethodName() + "  " 
			+ dateFormatter.format(new Date(record.getMillis())) + "" 
			+ record.getMessage() + "\n";
	}

}
