import java.util.*;

public class PaxosClient extends Thread {

	Paxos _p;
	Integer _timeout;

	public PaxosClient(Paxos p, Integer t) {
		_p = p;
		_timeout = t;
	}

	public void run() {
		for (int i = 0; i < _p._hosts.length; i++) {
			_p._hosts[i].connectToHost();
		}

		AcceptKeyboardInput keyboardInputThread = new AcceptKeyboardInput(this);
		keyboardInputThread.start();

		HandleMessages handleMessagesThread = new HandleMessages(_p);
		handleMessagesThread.start();
	}
	
	//Select a proposal number and send a prepare request to all acceptors
	//Wait for acceptor response with a promise not to accept any proposals numbered
	//less than n and with the highest-number proposal it has completed
	public void prepare() {
		//Increment proposal number (get a new ticket)
		_p._propNumber = _p.nextHighestPropNum(_p._propNumber);
		//Request a log entry be added at the first index we have available
		Message msg = new Message(_p._id, _p._propNumber, _p.log.size());
		for (int i = 0; i < _p._hosts.length; i++) {
			_p._hosts[i].sendToHost(msg);
		}
		_p._sentPropNumber = _p._propNumber;
	}

	
}