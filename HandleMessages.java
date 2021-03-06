import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.concurrent.*;

public class HandleMessages extends Thread {
	Paxos _p;

	public HandleMessages(Paxos p) {
		_p = p;
	}

	public void run() {
		//constantly check for messages in the queue
		while (true) {
			Message m = null;
			synchronized(_p) {
				m = _p._qMessages.poll();
			}

			//if the queue was not empty, handle the message
			if (m != null) {
				handle(m);
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				System.out.println("Interrupted");
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

			//Sent in PHASE 1
			//Proposer selects a proposal number n and sends a prepare request with number n
			//Handled by acceptor. On recv PREPARE -> acceptor sends PROMISE
			case PREPARE:
				if (m._number > _p._maxPrepare) {
					
					_p._maxPrepare = m._number;
					_p._propNumber = Math.max(_p._propNumber, _p.nextHighestPropNum(m._number)); //TODO: This should be set differently
					//ID contains the host to send the promise back to
					_p.promise(m._id, m);
				}

				break;

			//Sent in PHASE 1
			//handled by the proposer, sent by acceptor in response to prepare
			case PROMISE:

				//add the promise message to the list of received promises
				_p._promises.add(m);
				//if we have received a promise from majority of 
				//acceptors, select a value and send accept message
				//to all sites
				if (_p._promises.size() > _p._hosts.length /2) {
					_p.pleaseAccept(m._logIndex);
				}

				break;

			//Sent in PHASE 2
			//Handled by acceptor, accepts proposal unless it has already responded to a prepare request having number greater than n
			//Abandon proposal if some proposer has begun trying to issue a higher-numbered one
			case ACCEPT:
				if (m._number >= _p._maxPrepare) {
					//TODO: the thing
					//Set accNum and accVal
					_p._accNumber = m._number;
					_p._accValue = m._value;
					
					_p.learn(m._logIndex);
					
				}
				break;
				
			case LEARN:
			//It's in a map so that we can count the learns received of EACH VALUE
			//Check if it's already in the map
				if (_p._learns.get(m._value.toString()) == null) {
					_p._learns.put(m._value.toString(), 1);
				} else {
					_p._learns.put(m._value.toString(), _p._learns.get(m._value.toString()) + 1);
					if (_p._learns.get(m._value.toString()) > _p._hosts.length/2) {
						_p.commit(m);
					}
				}
				break;
			
			case COMMIT:
				_p.addToLog(m);
				break;
		}
	}
}