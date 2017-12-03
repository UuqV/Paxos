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
		String userInput = new String();
		while((userInput = sc.nextLine()) != null) { //constantly accept user commands
			System.out.println("Accepting new input");
			String[] inputTokens = userInput.split(" ", 2);

			if (inputTokens.length < 1) {
				System.out.println("Unknown command");
				continue;
			}
			if (inputTokens[0].equals("view")) {
				System.out.println("Called view");
				_pc._p.view();
				continue;
			}
			if (inputTokens[0].equals("exit")) {
				System.out.println("Called exit");
				System.exit(0);
			}
			if (inputTokens.length < 2) {
				System.out.println("Incomplete command");
				continue;
			}
			if (inputTokens[0].equals("tweet")) {
				System.out.println("Called tweet");
				//create a tweet event record
				EventRecord tweet = new EventRecord();
				tweet.realtime = System.currentTimeMillis();
				tweet.username = _pc._p._hosts[_pc._p._id]._name;
				tweet.content = inputTokens[1];
				tweet.id = _pc._p._id;
				tweet.operation = EventRecord.Operation.TWEET;

				//add that tweet to the QUEUE
				//TODO: SYNCHRONIZE
				_pc._p._qMyEvents.add(tweet); //TODO: make sure that if we don't actually end up
				//accepting this event right away that we resend it eventually

				//initiate a prepare message, or accept if we are the distinguished proposer
				
				if (_pc._p.log.size() == 0) {
					_pc.prepare();
				}
				else if (_pc._p.log.get(_pc._p.log.size() - 1).id == _pc._p._id) {
						_pc._p.pleaseAccept(_pc._p.log.size());
				} else {
					_pc.prepare();
				}

				//TODO: AFTER ADDING DISTINGUISHED PROPOSER LOGIC, THE FIRST SITE'S VIEW CALL BLOCKS?  OR FAILS?
				//SOMETHING'S HAPPNEING WHERE IT DOESNT WORK.  THE SECOND SITE WORKS JUST FINE, AND IT SEEMS THAT
				//THE FIRST SITE IS STILL CAPABLE OF PROCESSING MESSAGES AFTER THE FACT, IT'S JUST UNCLEAR WHAT'S
				//IN THE LOG BECAUSE I CAN'T PRINT ITS CONTENTS
				
				//if the most recent log element is this site, then we can skip
				//the prepare phase as the distinguished proposer and go right
				//to calling accept
				
			}

			//TODO: Add block/unblock commands
		}
	}
}