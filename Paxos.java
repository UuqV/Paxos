import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;

public class Paxos {

	Integer _id = -1;
	PaxosHost[] _hosts;

	public Paxos() {

		parseConfig("Paxos.config");

		PaxosServer ps = new PaxosServer(this);
		ps.start();

		PaxosClient pc = new PaxosClient(this);
		pc.start();
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

				//check if this line corresponds to this instance
				if (address.equals(InetAddress.getLocalHost())) {
					_id = lineId;
					System.out.println("This instance's ID: " + _id);
				}

				//construct a client for this instance
				_hosts[lineId] = new PaxosHost(lineId, name, port, address);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void main(String args[]) {
		Paxos paxos = new Paxos();
	}
}