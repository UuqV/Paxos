import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.util.concurrent.*;

public class PaxosHost {
	Integer _id;
	String _name;
	Integer _port;
	InetAddress _address;
	Socket _socket;
	PrintWriter _printWriter;

	public PaxosHost(Integer id, String name, Integer port, InetAddress address) {
		_id = id;
		_name = name;
		_port = port;
		_address = address;
	}

	public void printPaxosHost() {
		System.out.println("PaxosHost:\n\t_id = "+ _id);
		System.out.println("\t_name = " + _name);
		System.out.println("\t_port = " + _port);
		System.out.println("\t_address = " + _address);
		System.out.println("\t_socket = " + _socket);
		System.out.println("\t_printwriter = " + _printWriter);
	}

	public void connectToHost() {
		try {
			_socket = new Socket(_address.getHostName(), _port);

			//_socket.connect(new InetSocketAddress(_address, _port), 1000);

			if (_socket != null) {
				System.out.println("Got here 1\n");
				_printWriter = new PrintWriter(new OutputStreamWriter(_socket.getOutputStream()));
				System.out.println("Created PrintWriter\n");
			}

			System.out.println("Connected to " + _address + "\n\ton port " + _port);
		//	System.out.println("Tried to connect!");
		} catch (Exception e) {
			System.out.print("ERROR: ");
			System.out.println(e.getMessage());
		}

		System.out.println("Finished call to ConnectToHost");
		printPaxosHost();
	}

	public void sendToHost(Message msg) {
		System.out.println("Called sendToHost");
		if (_socket != null && _printWriter != null) {
			_printWriter.print(msg.toString());
			_printWriter.flush();
			System.out.println("Sent msg: " + msg.toString());
		}
	}
}