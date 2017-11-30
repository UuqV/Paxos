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

	//TODO:theres something wrong with the message passing protocol,
	//not sure what's happening but when a site receives a prepare
	//from a site that isn't itself, ,it doesn't respond with promise
	
	//Select a proposal number and send a prepare request to majority of acceptors
	//Wait for acceptor response with a promise not to accept any proposals numbered
	//less than n and with the highest-number proposal it has completed
	public void prepare() {
		//Increment proposal number (get a new ticket)
		_p._n++;
		Message msg = new Message(_p._id, _p._n);
		for (int i = 0; i < _p._hosts.length; i++) {
			_p._hosts[i].sendToHost(msg);
		}
		
		//Wait for response from majority of acceptors to prepare
		Integer timeCount = 0;
		while(_p._prepared == false && timeCount < _timeout) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				
			}
			timeCount++;
		}
		if (_p._prepared == true) {
			pleaseAccept();
		}
	}

	public void pleaseAccept() {
		if (_p._prepared == true) {
			//If all promises are null, use my proposal value
			//Otherwise, send an accept with the other's largest proposal value (accVal)
			//No matter what, use OWN proposal number
			Message msg = new Message(_p._id, Message.MsgType.ACCEPT, 
				_p._accNumber, _p._accValue);

			for (int i = 0; i < _p._hosts.length; i++) {
				_p._hosts[i].sendToHost(msg);
			}
			//Clear the promises whether accepted or not
			_p._promises = new ArrayList<Message>();
			_p._prepared = false;
		}
	}
	

}

