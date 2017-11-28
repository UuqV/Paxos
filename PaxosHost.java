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
		_socket = null;
		_printWriter = null;
	}

	public void printPaxosHost() {
		System.out.println("PaxosHost:\n\t_id = "+ _id);
		System.out.println("\t_name = " + _name);
		System.out.println("\t_port = " + _port);
		System.out.println("\t_address = " + _address);
		System.out.println("\t_socket = " + _socket);
		System.out.println("\t_printwriter = " + _printWriter);
	}

	//Connect to the address stored in data.
	//Only connects if _socket is null, to prevent exceptions
	public void connectToHost() {
		try {
			//open the socket connection:
			if (_socket == null) {
				_socket = new Socket(_address.getHostName(), _port);
				//if connection was successful, display results
				if (_socket != null) {
					System.out.println("Connected to " + _address + "\n\ton port " + _port);
				}
			}

			//if socket was successfully created, open a printwriter to it:
			if (_socket != null) {
				_printWriter = new PrintWriter(new OutputStreamWriter(_socket.getOutputStream()));
			}
		} catch (Exception e) {
			System.out.println("Refused connection to " + _address + 
				".\n\tServer hasn't started or has crashed.");
		}
	}

	public void sendToHost(Message msg) {
		if (_socket != null && _printWriter != null) {
			_printWriter.print(msg.toString());
			_printWriter.flush();
			System.out.print("Sent to " + _name + ": " + msg.toString());
		}
	}
}