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
				System.out.println("Called view");
				_pc._p.view();
				continue;
			}
			if (inputTokens[0].equals("exit")) {
				System.exit(0);
			}
			if (inputTokens.length < 2) {
				System.out.println("Incomplete command");
				continue;
			}
			if (inputTokens[0].equals("block")) {
				EventRecord block = new EventRecord();
				block.realtime = System.currentTimeMillis();
				block.username = _pc._p._hosts[_pc._p._id]._name;
				block.content = inputTokens[1];
				block.id = _pc._p._id;
				block.operation = EventRecord.Operation.BLOCK;

				_pc._p._qMyEvents.add(block);

				if (_pc._p.log.size() == 0) {
					_pc.prepare();
				} else if (_pc._p.log.get(_pc._p.log.size() - 1).id == _pc._p._id) {
					_pc._p.pleaseAccept(_pc._p.log.size());
				} else {
					_pc.prepare();
				}
			}
			if (inputTokens[0].equals("unblock")) {
				EventRecord unblock = new EventRecord();
				unblock.realtime = System.currentTimeMillis();
				unblock.username = _pc._p._hosts[_pc._p._id]._name;
				unblock.content = inputTokens[1];
				unblock.id = _pc._p._id;
				unblock.operation = EventRecord.Operation.UNBLOCK;

				_pc._p._qMyEvents.add(unblock);

				if (_pc._p.log.size() == 0) {
					_pc.prepare();
				} else if (_pc._p.log.get(_pc._p.log.size() - 1).id == _pc._p._id) {
					_pc._p.pleaseAccept(_pc._p.log.size());
				} else {
					_pc.prepare();
				}
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
				_pc._p._qMyEvents.add(tweet); //TODO: make sure that if we don't actually end up
				//accepting this event right away that we resend it eventually

				//initiate a prepare message, or accept if we are the distinguished proposer
				if (_pc._p.log.size() == 0) {
					_pc.prepare();
				}

				//TODO: AFTER ADDING DISTINGUISHED PROPOSER LOGIC, THE FIRST SITE'S VIEW CALL BLOCKS?  OR FAILS?
				//SOMETHING'S HAPPNEING WHERE IT DOESNT WORK.  THE SECOND SITE WORKS JUST FINE, AND IT SEEMS THAT
				//THE FIRST SITE IS STILL CAPABLE OF PROCESSING MESSAGES AFTER THE FACT, IT'S JUST UNCLEAR WHAT'S
				//IN THE LOG BECAUSE I CAN'T PRINT ITS CONTENTS

				//if the most recent log element is this site, then we can skip
				//the prepare phase as the distinguished proposer and go right
				//to calling accept
				if (_pc._p.log.get(_pc._p.log.size() - 1).id == _pc._p._id) {
					_pc._p.pleaseAccept(_pc._p.log.size());
				} else {
					_pc.prepare();
				}
			}

			//TODO: Add block/unblock commands
		}
	}
}