import java.util.*;

public class PaxosClient extends Thread {

	Paxos _p;

	public PaxosClient(Paxos p) {
		_p = p;
	}

	public void run() {
		for (int i = 0; i < _p._hosts.length; i++) {
			_p._hosts[i].connectToHost();
		}

		AcceptKeyboardInput keyboardInputThread = new AcceptKeyboardInput(this);
		keyboardInputThread.start();

		HandleMessages handleMessagesThread = new HandleMessages(this);
		handleMessagesThread.start();
	}

	//TODO:theres something wrong with the message passing protocol,
	//not sure what's happening but when a site receives a prepare
	//from a site that isn't itself, ,it doesn't respond with promise
	
	//Select a proposal number and send a prepare request to majority of acceptors
	//Wait for acceptor response with a promise not to accept any proposals numbered
	//less than n and with the highest-number proposal it has completed
	public void prepare(Integer number) {
		Message msg = new Message(_p._id, number);
		for (int i = 0; i < _p._hosts.length; i++) {
			_p._hosts[i].sendToHost(msg);
		}
	}

	public void promise() {
		Message msg = new Message(_p._id, Message.MsgType.PROMISE, 
			_p._accNumber, _p._accValue);

		for (int i = 0; i < _p._hosts.length; i++) {
			_p._hosts[i].sendToHost(msg);
		}
	}
	
	//Upon reciept of a response for the 'prepare' message from the majority of acceptors,
	//send an accept request to majority of acceptors for a proposal n with value v
	public void propose() {
		prepare(0);
		//TODO:	we don't actually have a propose message, should probably
		//remove this
	}
}