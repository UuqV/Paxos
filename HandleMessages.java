import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.concurrent.*;

public class HandleMessages extends Thread {
	PaxosClient _pc;

	public HandleMessages(PaxosClient pc) {
		_pc = pc;
	}

	public void run() {
		//constantly check for messages in the queue
		while (true) {
			Message m = null;
			synchronized(_pc._p) {
				m = _pc._p._qMessages.poll();
			}

			//if the queue was not empty, handle the message
			if (m != null) {
				handle(m);
			}
		}
	}

	public void handle(Message m) {
		//NOTE: THE PAXOSCLIENT AND PAXOSSERVER OBJECTS DO NOT 
		//CORRESPOND TO THE PROPOSER AND ACCEPTOR ROLES.  THE
		//PAXOSSERVER WILL RECV ALL MESSAGES WHETHER THEY ARE 
		//SENT BY THE PROPOSER OR THE ACCEPTOR.  THUS, THE 
		//HANDLER WILL PLAY THE ROLE OF BOTH PROPOSER AND 
		//ACCEPTOR DEPENDING ON THE MSGTYPE, AS INDICATED BELOW

		switch (m._msgType) {

			//handled by the acceptor, sent by proposer
			case PREPARE:
				if (m._number > _pc._p._maxPrepare) {
					_pc._p._maxPrepare = m._number;
					_pc.promise();
				}

				break;
			case ACCEPT:
				break;

			//handled by the proposer, sent by acceptor in response
			case PROMISE:
				//TODO: IT'S POSSIBLE TO RECEIVE PROMISE MESSAGES
				//FROM THE SAME PROCESS TWICE, BECAUSE WE SEND OUT 
				//RESPONSE MESSAGES BEFORE WE HAVE RECEIVED PROMISES
				//FROM ALL SITES.  FOR THIS REASON, WE SHOULD OVERWRITE
				//IF A PROMISE MESSAGE FROM THE SAME SITE IS ALREADY IN 
				//THE PROMISES ARRAYLIST.

				//add the promise message to the list of received promises
				_pc._p._promises.add(m);

				//if we have received a promise from majority of 
				//acceptors, select a value and send accept message
				//to all sites
				if (_pc._p._promises.size() > _pc._p._hosts.length /2) {
					//TODO: 
				}

				break;
		}
	}
}