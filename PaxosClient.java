public class PaxosClient extends Thread {

	Paxos _p;

	public PaxosClient(Paxos p) {
		_p = p;
	}

	public void run() {
		for (int i = 0; i < _p._hosts.length; i++) {
			if (i != _p._id) {
				System.out.println(_p._id + " Trying to connect to instance " + i);
				_p._hosts[i].connectToHost();
			}
		}
	}
}