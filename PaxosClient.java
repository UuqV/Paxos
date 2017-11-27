import java.util.*;

public class PaxosClient extends Thread {

	Paxos _p;
	Queue<EventRecord> _q = new LinkedList<EventRecord>();

	public PaxosClient(Paxos p) {
		_p = p;
	}

	public void run() {
		for (int i = 0; i < _p._hosts.length; i++) {
			System.out.println(_p._id + " Trying to connect to instance " + i);
			_p._hosts[i].connectToHost();
		}

		AcceptKeyboardInput keyboardThread = new AcceptKeyboardInput(this);
		keyboardThread.start();
	}
	
	//Select a proposal number and send a prepare request to majority of acceptors
	//Wait for acceptor response with a promise not to accept any proposals numbered
	//less than n and with the highest-number proposal it has completed
	public void prepare(Integer number) {
		Message msg = new Message(_p._id, number);
		for (int i = 0; i < _p._hosts.length; i++) {
			_p._hosts[i].sendToHost(msg);
		}
	}
	
	//Upon reciept of a response for the 'prepare' message from the majority of acceptors,
	//send an accept request to majority of acceptors for a proposal n with value v
	public void propose() {
		prepare(0);
		
	}
	
	//When we want to submit a tweet for consensus
	public void send(Message tweet) {
		propose();
	}
}