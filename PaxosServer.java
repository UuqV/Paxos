import java.net.*;
import java.io.*;

public class PaxosServer extends Thread {

	Paxos _p;
	ServerSocket _ss;

	public PaxosServer(Paxos p) {
		_p = p;
	}

	public void run() {
		try {
			_ss = new ServerSocket(_p._hosts[_p._id]._port);
			while (true) {
				System.out.println("Waiting to accept ...");
				Socket clientSocket = _ss.accept();
				System.out.println("Accepted client!  Spawning thread: ");
				RecvMessages recvMessages = new RecvMessages(clientSocket, this);
				recvMessages.start();
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		System.out.println("Closing Server");
	}
}