import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;

public class Paxos {

	Integer _id = -1;
	PaxosHost[] _hosts;

	//State variables for Synod execution
	Integer _maxPrepare;
	Integer _accNumber;
	EventRecord _accValue;

	ArrayList<Message> _promises;

	//Events this instance has created, with local keyboard commands
	Queue<EventRecord> _qMyEvents = new LinkedList<EventRecord>();

	//Messages this instance has received, to be processed by the
	//HandleMessages thread, started by PaxosClient
	Queue<Message> _qMessages = new LinkedList<Message>();

	public Paxos() {
		_maxPrepare = -1;
		_accNumber = -1;
		_accValue = new EventRecord();

		_promises = new ArrayList<Message>();

		parseConfig("Paxos.config");
	}

	//effects: creates the hosts array
	public void parseConfig(String filename) {

		//open config file
		try (BufferedReader br = new BufferedReader(new FileReader("Paxos.config"))) {
			
			//config format: line 1 = number of instances
			String line = br.readLine();
			Integer numInstances = Integer.valueOf(line);

			_hosts = new PaxosHost[numInstances];
			
			//each successive line corresponds to one instance
			//format: id, name, port, address
			while ((line = br.readLine()) != null) {
				//tokenize the instance line
				String[] tokens = line.split(" ");
				Integer lineId = Integer.valueOf(tokens[0]);
				String name = tokens[1];
				Integer port = Integer.valueOf(tokens[2]);
				InetAddress address = InetAddress.getByName(tokens[3]);

				//construct a host for this instance
				_hosts[lineId] = new PaxosHost(lineId, name, port, address);

				//check if this host corresponds to this instance
				if (address.equals(InetAddress.getLocalHost())) {
					_id = lineId;
					System.out.print("Starting ");
					_hosts[_id].printPaxosHost();
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public PaxosHost findHost(InetAddress clientInetAddress) {
		for (int i = 0; i < _hosts.length; i++) {
			if (_hosts[i]._address.equals(clientInetAddress)) {
				return _hosts[i]; 
			}
		}
		System.out.println("FATAL ERROR: Connection made from machine not in config file");
		System.out.println("\tInetAddress: " + clientInetAddress);
		System.exit(1);
		return null; //unreachable, system will terminate
	}

	public static void main(String args[]) {
		Paxos paxos = new Paxos();

		PaxosServer ps = new PaxosServer(paxos);
		ps.start();

		PaxosClient pc = new PaxosClient(paxos);
		pc.start();
	}
}