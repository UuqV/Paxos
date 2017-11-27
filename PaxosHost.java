import java.net.*;

public class PaxosHost {
	Integer _id;
	String _name;
	Integer _port;
	InetAddress _address;
	Socket _socket;

	public PaxosHost(Integer id, String name, Integer port, InetAddress address) {
		_id = id;
		_name = name;
		_port = port;
		_address = address;
	}

	public void printPaxosHost() {
		System.out.print("PaxosHost:\n\t_id = "
				+ _id + "\n\t_name = " + _name + "\n\t_port = "
				+ _port + "\n\t_address = " + _address + 
				"\n\t_socket = " + _socket + "\n");
	}

	public void connectToHost() {
		//System.out.print("Called connect to\n");
		//System.out.print("\thostname: " + _address.getHostName() + 
		//	"\n\tport: " + _port + "\n");

		try {
			_socket = new Socket(_address.getHostName(), _port);

			_socket.connect(new InetSocketAddress(_address, _port), 1000);
			System.out.println("Connected to " + _address + "\n\ton port " + _port);
		//	System.out.println("Tried to connect!");
		} catch (Exception e) {
			System.out.print("ERROR: ");
			System.out.println(e.getMessage());
		}
	}
}