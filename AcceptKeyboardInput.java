import java.io.*;
import java.nio.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.concurrent.*;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class AcceptKeyboardInput extends Thread {
	PaxosClient _pc;

	public AcceptKeyboardInput(PaxosClient pc) {
		_pc = pc;
	}

	public void run() {
		Scanner sc = new Scanner(System.in);
		String userInput = new String();
		while((userInput = sc.nextLine()) != null) { //constantly accept user commands
			String[] inputTokens = userInput.split(" ", 2);

			if (inputTokens.length < 1) {
				System.out.println("Unknown command");
				continue;
			}
			if (inputTokens[0].equals("view")) {
				_pc._p.view();
				continue;
			}
			if (inputTokens[0].equals("reset")) {
				try {
					File log = new File(_pc._p._logFile);
					Files.deleteIfExists(log.toPath());
					File state = new File(_pc._p._stateFile);
					Files.deleteIfExists(state.toPath());
					File promises = new File(_pc._p._promisesFile);
					Files.deleteIfExists(promises.toPath());
					File learns = new File(_pc._p._learnsFile);
					Files.deleteIfExists(learns.toPath());
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}

				System.out.println("Resetting and stopping server");
				System.exit(0);
				continue;
			}

			if (inputTokens[0].equals("exit")) {
				System.out.println("Crashing Server");
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
					_pc._p.prepare();
				} else if (_pc._p.log.get(_pc._p.log.size() - 1).id == _pc._p._id) {
					_pc._p.pleaseAccept(_pc._p.log.size());
				} else {
					_pc._p.prepare();
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
					_pc._p.prepare();
				} else if (_pc._p.log.get(_pc._p.log.size() - 1).id == _pc._p._id) {
					_pc._p.pleaseAccept(_pc._p.log.size());
				} else {
					_pc._p.prepare();
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
				_pc._p._qMyEvents.add(tweet);

				//initiate a prepare message, or accept if we are the distinguished proposer
				
				if (_pc._p.log.size() == 0) {
					_pc._p.prepare();
				}
				else if (_pc._p.log.get(_pc._p.log.size() - 1).id == _pc._p._id) {
						_pc._p.pleaseAccept(_pc._p.log.size());
				} else {
					_pc._p.prepare();
				}
			}
		}
	}
}