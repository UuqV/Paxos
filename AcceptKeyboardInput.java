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
				EventRecord tweet = new EventRecord();
				tweet.realtime = System.currentTimeMillis();
				tweet.username = _p._hosts[_p._id]._name;
				tweet.content = inputTokens[1];
				tweet.id = _p._id;
				tweet.operation = EventRecord.Operation.TWEET;

				
				Message m = new Message(_p._id, Message.MsgType.PREPARE, 1, tweet);

				System.out.println("Message m toString: " + m.toString());
				System.out.println("Message m fromString: ");
				Message.fromString(m.toString()).printMessage();
				
				_p.pc.send(m);

				System.out.println("Sending tweet (not implemented)");
				//wu.sendMessage(); 
			}
		}
	}
}