import java.io.Serializable;

public class EventRecord implements Serializable {
	public enum Operation { TWEET, BLOCK, UNBLOCK };

	Integer timestamp;
	Operation operation;
	String username; //user that does the thing
	String content; //either: user that is blocked, or tweet text
	long realtime;
	Integer id;

	public String toString() {
		String erStr = timestamp.toString() + "`" + operation.name() + "`" + username + "`" + content + "`" + String.valueOf(realtime) + "`" + id.toString();
		return erStr;
	}

	public static EventRecord fromString(String erStr) {
		String[] tokens = erStr.split("`");
		EventRecord eR = new EventRecord();
		eR.timestamp = Integer.parseInt(tokens[0]);
		eR.operation = Operation.valueOf(tokens[1]);
		eR.username = tokens[2];
		eR.content = tokens[3];
		eR.realtime = Long.valueOf(tokens[4]);
		eR.id = Integer.parseInt(tokens[5]);
		return eR;
	}
}