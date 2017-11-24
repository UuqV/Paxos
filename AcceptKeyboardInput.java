import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.concurrent.*;

public class AcceptKeyboardInput extends Thread {
	Paxos _p;

	public AcceptKeyboardInput(Paxos p) {
		_p = p; 
		//testIfSocketClosed(wu.hosts[1], "AcceptKeyboardInputConstructor");
	}

	public void run() {
		Scanner sc = new Scanner(System.in);
		String userInput = null;
		while((userInput = sc.nextLine()) != null) { //constantly accept user commands
			String[] inputTokens = userInput.split(" ", 2);

			if (inputTokens.length < 1) {
				System.out.println("Unknown command");
				continue;
			}
			if (inputTokens[0].equals("view")) {
				//view();
				continue;
			}
			if (inputTokens.length < 2) {
				System.out.println("Incomplete command");
				continue;
			}
			if (inputTokens[0].equals("tweet")) {

				Message m = Message.fromString("TODO: Change this");

				EventRecord tweet = new EventRecord();
				tweet.realtime = System.currentTimeMillis();
				tweet.username = _p._hosts[_p._id]._name;
				tweet.content = inputTokens[1];
				tweet.id = _p._id;
				tweet.operation = EventRecord.Operation.TWEET;
				
				//testIfSocketClosed(wu.hosts[1], "AcceptKeyboardInput");
				/*
				System.out.println("wu.id: " + wu.id);
				synchronized(wu){
					wu.tsMatrix.get(wu.id).set(wu.id, tsMatrix.get(wu.id).get(wu.id) + 1);
					tweet.timestamp = wu.tsMatrix.get(wu.id).get(wu.id);
			
					wu.log.add(tweet);
				}
				*/

				System.out.println("Sending tweet (not implemented)");
				//wu.sendMessage(); 
			}
		}
	}
}