import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.concurrent.*;

public class AcceptKeyboardInput extends Thread {
	PaxosClient _pc;

	public AcceptKeyboardInput(PaxosClient pc) {
		_pc = pc;
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

				//create a tweet event record
				EventRecord tweet = new EventRecord();
				tweet.realtime = System.currentTimeMillis();
				tweet.username = _pc._p._hosts[_pc._p._id]._name;
				tweet.content = inputTokens[1];
				tweet.id = _pc._p._id;
				tweet.operation = EventRecord.Operation.TWEET;

				//add that tweet to the QUEUE
				//TODO: SYNCHRONIZE
				_pc._p._qMyEvents.add(tweet);

				//initiate a prepare message
				//TODO: Figure out what to use for a propNumber
				_pc.prepare();
			}

			//TODO: Add block/unblock commands
		}
	}
}