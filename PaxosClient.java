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

		_p.recover();
	}
}