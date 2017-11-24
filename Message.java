import java.io.*;
import java.util.ArrayList;

public class Message implements Serializable{
	/*
	ArrayList<EventRecord> log;
	ArrayList<ArrayList<Integer>> tsMatrix;
	Integer id;
	*/

	//does not deep copy
	public Message(/*ArrayList<EventRecord> l, ArrayList<ArrayList<Integer>> ts, Integer senderID*/) {
		/*
		log = l;
		tsMatrix = ts;
		id = senderID;
		*/
	}


	//OLD FORMAT: ID|TSMATRIX|LOG
	//TODO: IMPLEMENT NEW MESSAGING FORMAT
	public String toString() {
		String mStr = "";
		/*
		mStr += id.toString() + "~";
		for (int i = 0; i < tsMatrix.size(); i++) {
			for (int j = 0; j < tsMatrix.get(0).size(); j++) {
				mStr += tsMatrix.get(i).get(j);
				if (j != tsMatrix.get(i).size() - 1) {
					mStr += " ";
				}
			}
			if (i != tsMatrix.size() - 1) {
				mStr += ",";
			}
		}
		mStr += "~";
		for (int i = 0; i < log.size(); i++) {
			mStr += log.get(i).toString();
			if (i != log.size() - 1) {
				mStr += ";";
			}
		}
		mStr += "\n";
		*/
		return mStr;
	}

	public static Message fromString(String mStr) {
		/*
		String[] paramTokens = mStr.split("~");
		System.out.println("ParamTokens: \n\t" + paramTokens[0] + "\n\t" + paramTokens[1] + "\n\t" + paramTokens[2]);
		
		Integer mid = Integer.parseInt(paramTokens[0]);
		ArrayList<ArrayList<Integer>> mtsMatrix = new ArrayList<ArrayList<Integer>>();
		String[] matRowTokens = paramTokens[1].split(",");
		for (int i = 0; i < matRowTokens.length; i++) {
			String[] matRowStr = matRowTokens[i].split(" ");
			ArrayList<Integer> matRow = new ArrayList<Integer>();
			for (int j = 0; j < matRowStr.length; j++) {
				matRow.add(Integer.parseInt(matRowStr[j]));
			}
			mtsMatrix.add(matRow);
		}
		
		
		String[] erTokens = paramTokens[2].split(";");
		ArrayList<EventRecord> mLog = new ArrayList<EventRecord>();
		for (int i = 0; i < erTokens.length; i++) {
			mLog.add(EventRecord.fromString(erTokens[i]));
		}
		
		return new Message(mLog, mtsMatrix, mid);
		*/
		return new Message();
	}

	public void printMessage() {
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