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
	HashMap<String, HashSet<String>> blocklist = new HashMap<String, HashSet<String>>();
	
	//Offline Storage for Log and State
	String _logFile;
	String _stateFile;
	String _promisesFile;
	String _learnsFile;

	public Paxos() {
		_maxPrepare = 0;
		_accNumber = -1;
		_accValue = new EventRecord();
		
		_propNumber = 0;
		_sentPropNumber = null;

		_promises = new ArrayList<Message>();
		_learns = new HashMap<String, Integer>();

		_logFile = "Log.txt";
		_stateFile = "State.txt";
		_promisesFile = "Promises.txt";
		_learnsFile = "Learns.txt";

		parseConfig("Paxos.config");
		parseLogAndState();
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

	public void parseLogAndState() {
		String line;
		try {
			if (new File(_logFile).exists()) {
				BufferedReader brLog = new BufferedReader(new FileReader(_logFile));
				while ((line = brLog.readLine()) != null) {
					log.add(EventRecord.fromString(line));
				}
				brLog.close();
			}
			
			if (new File(_stateFile).exists()) {
				BufferedReader brState = new BufferedReader(new FileReader(_logFile));
				line = brState.readLine();
				_maxPrepare = Integer.parseInt(line);
				line = brState.readLine();
				_accNumber = Integer.parseInt(line);
				line = brState.readLine();
				_accValue = EventRecord.fromString(line);
				brState.close();
			}

			if (new File(_promisesFile).exists()) {
				BufferedReader brPromises = new BufferedReader(new FileReader(_logFile));
				while ((line = brPromises.readLine()) != null) {
					_promises.add(Message.fromString(line));
				}
				brPromises.close();
			}

			if (new File(_learnsFile).exists()) {
				BufferedReader brLearns = new BufferedReader(new FileReader(_logFile));
				while ((line = brLearns.readLine()) != null) {
					String learnHash = (Message.fromString(line)).toString();
					if (_learns.get(learnHash) == null) {
						_learns.put(learnHash, 1);
					} else {
						_learns.put(learnHash, _learns.get(learnHash) + 1);
					}				}
				brLearns.close();
			}
		} catch (Exception e) {
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

	//Select a proposal number and send a prepare request to all acceptors
	//Wait for acceptor response with a promise not to accept any proposals numbered
	//less than n and with the highest-number proposal it has completed
	public void prepare() {
		//Increment proposal number (get a new ticket)
		_propNumber = nextHighestPropNum(_propNumber);
		//Request a log entry be added at the first index we have available
		Message msg = new Message(_id, _propNumber, log.size());
		for (int i = 0; i < _hosts.length; i++) {
			_hosts[i].sendToHost(msg);
		}
		_sentPropNumber = _propNumber;
	}

	//Send a promise back to the host it came from, FOR THE MESSAGE THAT IT REQUESTED.
	public void promise(Integer id, Message prepare) {
		Message msg = null;

		//if we are operating on a logindex that has already been committed at this site,
		//send the committed value back with a higher propnum
		if (prepare._logIndex < log.size()) {
			msg = new Message(_id, Message.MsgType.PROMISE,
				nextHighestPropNum(prepare._number), log.get(prepare._logIndex), prepare._logIndex);

		} else {
			msg = new Message(_id, Message.MsgType.PROMISE, 
				_accNumber, _accValue, prepare._logIndex);
		}
		
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
					msgNumber = _promises.get(i)._number;
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
					_qMyEvents.peek(), logIndex); //TODO: CHANGED REMOVE() TO PEEK(), WHICH DOES NOT REMOVE QUEUE ELEMENT.
													//FIGURE OUT WHERE TO REMOVE THE ELEMENT.
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

	public void block(EventRecord er) {
		if (blocklist.get(er.username) == null) {
			blocklist.put(er.username, new HashSet<String>(Arrays.asList(er.content)));
		} else {
			blocklist.get(er.username).add(er.content);
		}
	}

	public void unblock(EventRecord er) {
		if (blocklist.get(er.username) != null) {
			blocklist.get(er.username).remove(er.content);
		}
	}

	public synchronized void addToLog(Message m) {
		if ((log.size() == m._logIndex)) {
			if (!_qMyEvents.isEmpty()) {
				System.out.println("\n\t_qMyEvents.peek(): " + _qMyEvents.peek().toString());
				System.out.println("\tm._value.toString(): " + m._value.toString());
				if (m._value.toString().equals(_qMyEvents.peek().toString())) {
					System.out.println("\tTHESE VALuES ARE EQUAL, REMOVING FROM QUEUE\n");
					_qMyEvents.remove();
					System.out.println("QuEUE CONTENTS: " + _qMyEvents);
				}
			}
			log.add(m._value);

			if (m._value.operation == EventRecord.Operation.BLOCK) {
				block(m._value);
			} else if (m._value.operation == EventRecord.Operation.UNBLOCK) {
				unblock(m._value);
			}
		}

		//reset state variables, potentially begin new propose 
		_promises.clear();
		_learns.clear();

		_accNumber = -1;
		_accValue = new EventRecord();
		_maxPrepare = 0;

		//TODO: WHEN WE COMMIT SOMETHING TO THE LOG - THROW THAT 
		//GOOD BOY INTO STABLE STORAGE

		if (!_qMyEvents.isEmpty()) {
			prepare();
		}

		//write to stable storage
		writeLogToStableStorage();
		writeStateVariablesToStableStorage();
	}

	public synchronized void writeLogToStableStorage() {
		try {
			PrintWriter logWriter = new PrintWriter("Log.txt", "UTF-8");
			for (int i = 0; i < log.size(); i++) {
				logWriter.println(log.get(i).toString());
			}
			logWriter.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}

	public synchronized void writeStateVariablesToStableStorage() {
		try {
			PrintWriter svWriter = new PrintWriter("State.txt", "UTF-8");
			svWriter.println(_maxPrepare);
			svWriter.println(_accNumber);
			svWriter.println(_accValue);
			svWriter.close();

			PrintWriter promiseWriter = new PrintWriter("Promises.txt", "UTF-8");
			for (int i = 0; i < _promises.size(); i++) {
				promiseWriter.println(_promises.get(i).toString());
			}
			promiseWriter.close();

			PrintWriter learnWriter = new PrintWriter("Learns.txt", "UTF-8");
			for (int i = 0; i < _learns.size(); i++) {
				learnWriter.println(_learns.get(i).toString());
			}
			learnWriter.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}

	public void view() {
		System.out.println("Number of events in log: " + log.size() + "\n");
		for (int i = 0; i < log.size(); i++) {
			EventRecord er = log.get(i);
			if (er.operation == EventRecord.Operation.TWEET) {
				if (blocklist.get(_hosts[_id]._name) != null) {
					if (!blocklist.get(_hosts[_id]._name).contains(er.username)) {
						er.printTweet();
						System.out.println("\n");
					}
				} else {
					er.printTweet();
					System.out.println("\n");
				}
			}
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