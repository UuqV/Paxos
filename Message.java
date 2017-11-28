import java.io.*;
import java.util.ArrayList;

public class Message implements Serializable{
	public enum MsgType { PREPARE, PROMISE, ACCEPT };
	
	//Message contains two IDs: _id, the sender of the message,
	//and _value._id, the creator of the tweet in the message
	Integer _id;
	MsgType _msgType;
	Integer _number;
	EventRecord _value;

	public Message(Integer id, MsgType msgType, Integer number, EventRecord value) {
		_id = id;
		_msgType = msgType;
		_number = number;
		_value = value;
	}

	public Message(Integer id, Integer number) {
		_id = id;
		_msgType = MsgType.PREPARE;
		_number = number;
		_value = new EventRecord();
	}

	public String toString() {
		String mStr = "";
		mStr += _id.toString() + "~";
		mStr += _msgType.name() + "~";
		mStr += _number.toString() + "~";
		mStr += _value.toString() + "\n";
		return mStr;
	}

	public static Message fromString(String mStr) {
		String[] paramTokens = mStr.split("~");
		Integer id = Integer.parseInt(paramTokens[0]);
		MsgType msgType = MsgType.valueOf(paramTokens[1]);
		Integer number = Integer.parseInt(paramTokens[2]);
		EventRecord value = EventRecord.fromString(paramTokens[3]);
		return new Message(id, msgType, number, value);
	}

	public void printMessage() {
		System.out.print("Message: \n\tID: " + _id.toString() + 
			"\n\tmsgtype: " + _msgType.name() + 
			"\n\tnumber: " + _number.toString() + 
			"\n\tvalue:\t");
		_value.printEventRecord();
	}
}