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
				//Wait for another computer to connect
				Socket clientSocket = _ss.accept();
				//Connection received!  process that computer's info
				InetAddress clientInetAddress = clientSocket.getInetAddress();
				System.out.println("Received connection from: " + clientInetAddress);

				//Spawn a thread to receive messages from the client that just connected
				RecvMessages recvMessages = new RecvMessages(clientSocket, this);
				recvMessages.start();

				//Attempt to reciprocally connect to the client that just connected,
				//So that this computer can send messages to it as well as recv them
				PaxosHost connectedClient = _p.findHost(clientInetAddress);
				connectedClient.connectToHost();
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		System.out.println("Closing Server");
	}
}