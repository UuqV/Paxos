import java.util.*;
import java.text.*;

public class EventRecord {
	public enum Operation { NONE, TWEET, BLOCK, UNBLOCK, DUMMY };

	Operation operation;
	String username; //user that does the thing
	String content; //either: user that is blocked, or tweet text
	Long realtime;
	Integer id; //Identity of the host it came from

	public EventRecord() {
		operation = Operation.NONE;
		username = "";
		content = "";
		realtime = -1L;
		id = -1;
	}

	public String toString() {
		String erStr = operation.name() + "`";
		erStr += username + "`";
		erStr += content + "`";
		erStr += realtime.toString() + "`";
		erStr += id.toString() + "`";
		return erStr;
	}

	public static EventRecord fromString(String erStr) {
		String[] tokens = erStr.split("`");
		EventRecord eR = new EventRecord();
		eR.operation = Operation.valueOf(tokens[0]);
		eR.username = tokens[1];
		eR.content = tokens[2];
		eR.realtime = Long.valueOf(tokens[3]);
		eR.id = Integer.parseInt(tokens[4]);
		return eR;
	}

	public void printEventRecord() {
		System.out.println("EventRecord: \n\toperation: " + operation.name() + "\n\tusername: " + 
			username + "\n\tcontent: " + content + "\n\trealtime: " + 
			realtime.toString() + "\n\tid: " + id.toString());
	}

	public void printTweet() {
		long unixSeconds = 1372339860;
		Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of your date
		sdf.setTimeZone(TimeZone.getTimeZone("GMT-4")); // give a timezone reference for formating (see comment at the bottom
		String formattedDate = sdf.format(date);

		System.out.println("@" + username + " " + formattedDate);
		System.out.println(content);
	}
}