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
			System.out.println("Waiting to accept ...");
			_ss.accept();
			System.out.println("Accepted client WOW SUCCESS");

			//TODO: LOOP THIS ACCEPT PROCESS
			System.out.println("Waiting to accept 2 ...");
			_ss.accept();
			System.out.println("Accepted second client!  Closing server");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}