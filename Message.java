import java.io.*;
import java.util.ArrayList;

public class Message implements Serializable{
	public enum MsgType { PREPARE, PROPOSE, PROMISE, ACCEPT };
	/*
	ArrayList<EventRecord> log;
	ArrayList<ArrayList<Integer>> tsMatrix;
	Integer id;
	*/
	Integer _id;
	MsgType _msgType;
	Integer _propNumber;
	EventRecord _propValue;

	//does not deep copy
	public Message(Integer id, MsgType msgType, Integer propNumber, EventRecord propValue) {
		_id = id;
		_msgType = msgType;
		_propNumber = propNumber;
		_propValue = propValue;
	}

	public Message(Integer id, Integer propNumber) {
		_id = id;
		_msgType = MsgType.PREPARE;
		_propNumber = propNumber;
		_propValue = new EventRecord();
	}


	//OLD FORMAT: ID|TSMATRIX|LOG
	//TODO: IMPLEMENT NEW MESSAGING FORMAT
	public String toString() {
		String mStr = "";
		mStr += _id.toString() + "~";
		mStr += _msgType.name() + "~";
		mStr += _propNumber.toString() + "~";
		mStr += _propValue.toString() + "\n";
		return mStr;
	}

	public static Message fromString(String mStr) {
		String[] paramTokens = mStr.split("~");
		Integer id = Integer.parseInt(paramTokens[0]);
		MsgType msgType = MsgType.valueOf(paramTokens[1]);
		Integer propNumber = Integer.parseInt(paramTokens[2]);
		EventRecord propValue = EventRecord.fromString(paramTokens[3]);
		return new Message(id, msgType, propNumber, propValue);
	}

	public void printMessage() {
		System.out.print("Message: \n\tID: " + _id.toString() + 
			"\n\tmsgtype: " + _msgType.name() + 
			"\n\tpropNumber: " + _propNumber.toString() + 
			"\n\tpropValue:\t");
		_propValue.printEventRecord();

		/*
		if (tsMatrix.size() <= 0) {
			return;
		}
		for (int i = 0; i < tsMatrix.size(); i++) {
			System.out.print("[\t");
			for (int j = 0; j < tsMatrix.get(i).size(); j++) {
				System.out.print(tsMatrix.get(i).get(j) + "\t");
			}
			System.out.print("]\n");
		}
		System.out.println();
		for (int i = 0; i < log.size(); i++) {
			System.out.println("Event");
		}
		*/
	}
}