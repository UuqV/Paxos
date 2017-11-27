import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.concurrent.*;

public class RecvMessages extends Thread {
	Socket _client;
	PaxosServer _ps;

	public RecvMessages(Socket client, PaxosServer ps) {
		_client = client;
		_ps = ps;
	}

	public void run() {
		System.out.println("RUNNING RECVMESSAGESTHREAD");
		try {
			BufferedReader dis = new BufferedReader(new InputStreamReader(_client.getInputStream()));
			String line = "";
			while(true) {
				if ((line = dis.readLine()) != null) {
					Message m = Message.fromString(line);
					System.out.println("Received message: " + m.toString());
					//todo: do
				}
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}