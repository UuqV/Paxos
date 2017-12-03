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
	
	//Request ordering number (take a ticket)
	//_propNumber stores the highest propNumber either it has used or received from anotehr site
	Integer _propNumber;
	//_sentPropNumber stores the most recent propNumber sent by THIS site, so that when we send
	//accept messages we know what propNumber to use
	Integer _sentPropNumber;

	//To keep track of how many acceptors have granted in response to a prep request
	ArrayList<Message> _promises;

	//eventrecord.tostring mapped to number of learns of that event this site has received.
	HashMap<String, Integer> _learns;

	//Events this instance has created, with local keyboard commands.  Events are stored here until they
	//are successfully added to the log.
	Queue<EventRecord> _qMyEvents = new LinkedList<EventRecord>();

	//Messages this instance has received, to be processed by the
	//HandleMessages thread, started by PaxosClient
	Queue<Message> _qMessages = new LinkedList<Message>();
	
	ArrayList<EventRecord> log = new ArrayList<EventRecord>();
	
	public Paxos() {
		_maxPrepare = 0;
		_accNumber = -1;
		_accValue = new EventRecord();
		
		_propNumber = 0;
		_sentPropNumber = null;

		_promises = new ArrayList<Message>();
		_learns = new HashMap<String, Integer>();

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

	//Send a promise back to the host it came from, FOR THE MESSAGE THAT IT REQUESTED.
	public void promise(Integer id, Integer logIndex) {
		Message msg = new Message(_id, Message.MsgType.PROMISE, 
			_accNumber, _accValue, logIndex);
			_hosts[id].sendToHost(msg);
	}

	public void pleaseAccept(Integer logIndex) {
		synchronized(this) {
			//If all promises are null, use my proposal value
			//Otherwise, send an accept with the other's largest proposal value (accVal)
			//No matter what, use OWN proposal number
			EventRecord msgValue = new EventRecord();
			Integer msgNumber = -1;
			for (int i = 0; i < _promises.size(); i++) {
				if (_promises.get(i)._value.operation != EventRecord.Operation.NONE
					&& _promises.get(i)._number > msgNumber) {
					msgValue = _promises.get(i)._value;
				}
			}

			Message msg = null;

			//TODO: make sure to clear the accNumber and accValue after learning a value and completing synod

			//if received a value back in a promise:
			if (msgValue.operation != EventRecord.Operation.NONE) {
				msg = new Message(_id, Message.MsgType.ACCEPT, msgNumber,
					msgValue, logIndex);
			} else { //if all promises are null:
				if (_qMyEvents.isEmpty()) {
					System.out.println("ERROR: THIS INSTANCE SENT PREPARE WITHOUT SAVING A TWEET TO ITS QUEUE");
				}
				msg = new Message(_id, Message.MsgType.ACCEPT, _sentPropNumber,
					_qMyEvents.remove(), logIndex);
			}

			for (int i = 0; i < _hosts.length; i++) {
				_hosts[i].sendToHost(msg);
			}
			//Clear the promises whether accepted or not
			_promises = new ArrayList<Message>();
		}
	}
	
	public void learn(Integer logIndex) {
		Message msg = new Message(_id, Message.MsgType.LEARN, 
			_accNumber, _accValue, logIndex);
			
		for (int i = 0; i < _hosts.length; i++) {
			_hosts[i].sendToHost(msg);
		}
	}

	public void commit(Message m) {
		Message msg = new Message(_id, Message.MsgType.COMMIT,
			m._number, m._value, m._logIndex);

		for (int i = 0; i < _hosts.length; i++) {
			_hosts[i].sendToHost(msg);
		}
	}

	public synchronized void addToLog(Message m) {
		System.out.println("\n\tADDING TO LOG AT INDEX " + m._logIndex + "\n");
		if ((log.size() == m._logIndex)) {
			log.add(m._value);
		}

		//reset state variables, potentially begin new propose 
		_promises.clear();
		_learns.clear();

		_accNumber = -1;
		_accValue = new EventRecord();
		_maxPrepare = 0;

		view();
	}

	public void view() {
		System.out.println("Number of events in log: " + log.size());
		for (int i = 0; i < log.size(); i++) {
			log.get(i).printEventRecord();
		}
	}

	public Integer nextHighestPropNum(Integer n) {
		int propNum = n + (_hosts.length - (n % _hosts.length));
		propNum += 1 + _id;
		return propNum;
	}

	public static void main(String args[]) {
		Paxos paxos = new Paxos();
		
		//TODO: Leader Election
		//First proposal initiated by "leader," can issue proposal number 0
		//All acceptors have an implicit promise to proposal 0
		//Leader can skip propose/promise & go directly to accept
		PaxosServer ps = new PaxosServer(paxos);
		ps.start();

		PaxosClient pc = new PaxosClient(paxos, 1000);
		pc.start();
	}
}