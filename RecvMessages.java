import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.concurrent.*;

public class RecvMessages extends Thread {
	Socket _client;
	PaxosServer _ps;
	PaxosHost _phClient;

	public RecvMessages(Socket client, PaxosServer ps) {
		_client = client;
		_ps = ps;
		_phClient = _ps._p.findHost(_client.getInetAddress());
	}

	public void run() {
		System.out.println("Started new recvmessages thread");
		for (int i = 0; i < _ps._p._hosts.length; i++) {
			if (_ps._p._hosts[i] != null) {
				System.out.println(_ps._p._hosts[i]); 
			}
		}
		try {
			BufferedReader dis = new BufferedReader(
				new InputStreamReader(_client.getInputStream()));
			String line = "";
			while(true) {
				//Receive a message
				if ((line = dis.readLine()) != null) {
					//Message received - Add it to the queue in Paxos,
					//to be handled by the HandleMessages thread
					Message m = Message.fromString(line);
					System.out.print("Recv'd from " + _phClient._name
						+ ": " + m.toString());
					
					synchronized(_ps._p) {
						_ps._p._qMessages.add(m);
					}
				}
				else if (_ps._p._timeout >= 1000){
					System.out.println("Timeout: " + _ps._p._timeout);
					_ps._p._timeout = 0;
					System.out.println("Hosts length: " + _ps._p._hosts.length );
					_ps._p.clearHost(_client.getInetAddress());
					
					for (int i = 0; i < _ps._p._hosts.length; i++) {
						if (_ps._p._hosts[i] != null) {
							System.out.println(_ps._p._hosts[i]);
						}
						else {
							System.out.println("null");
						}
					}
					break;
				}
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					System.out.println("Interrupted");
				}
				_ps._p._timeout += 1;
			}
		} catch (Exception e) {
			System.out.println("Exception in recvmessages");
			System.out.println(e.getMessage());
		}

		System.out.println("RecvMessages from " + _client
			+ " terminated");
	}
}