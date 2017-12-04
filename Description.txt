Design Patterns
User Interface
	A single thread handles the user input
	A single thread handles accepting connections
	There is a thread for each InputStream opened by a connection
	There is a thread for each OutputStream opened by a connection
	The log is stored as an array of EventRecord objects, consisting of an operation (NONE, TWEET, BLOCK, UNBLOCK), a username, some content, the wall clock time, and the user ID.
	
Log Entries and Full Synod
	The index of this EventRecord in the array is used to ensure Synod operates solely on that EventRecord. Each message is tagged with the index of the EventRecord in the array, and consensus on different records can operate concurrently.
	
Algorithm
	The HandleMessage threads, started for each OutputStream created by a connection, can processes messages sent over its corresponding connection. The thread expects PREPARE, PROMISE, ACCEPT, LEARN, and COMMIT messages.
	The RecvMessage threads, started for each InputStream created by a connection, sends messages of these types to any instance in the config file it's connected to. When the AcceptKeyboardInput thread receives the "tweet" command, the thread tells its instance to select a proposal number and send a prepare request to all acceptors. This tweet is to be added to the end of the log, so log.size() is used to choose the single entry for Synod to operate on for a new entry.
	When the rest of the instances receive this PREPARE request, provided that they have yet to promise to accept an entry with a higher proposal value, will send a PROMISE back to the proposer. This PROMISE ensures that the instance will not accept any requests with a higher proposal number until this entry is committed.

Commit Failure
	When the proposer receives a response back from the majority of these acceptors, it will send an ACCEPT message. This requests that the rest of instances learn the value. Handled by acceptor, accepts proposal unless it has already received a LEARN request having number greater than n. It will abandon proposal if some proposer has received a LEARN for a higher-numbered log entry.
	Finally, if a LEARN is accepted by the majority, a COMMIT is sent out to every instance by the last instance to accept the LEARN. This COMMIT triggers the actual addition of the entry for all instances.

Block/Unblock

Fifth Site

Log Recovery on Crash
Stable storage
Behavior of other instances on crash
	The log entries are stored in stable storage, along with maxPrepare, accNumber, and accValue, in a separate file. These log entries are parsed back into memory upon recovery. To fill holes in this log from entries it might have missed, the recovered instance sends a request on the indicies that are missing with no content. The other instances will respond to this empty PREPARE is full Synod fashion, complete with a COMMIT message at the end with the value they have in that log entry. When the recovered instance receives this commit, it will have the value for the log entry it missed. Since the rest of the instances already have this same value, their receipt of this commit has no effect on them.
	Without attention, sending and waiting for input from a crashed instance can significantly slow down the rest of the processes, when it doesn't crash them entirely. Thus when an instance crashes, the other instances must somehow detect it and avoid sending messages to it. To do this, we place a timeout on the OutputStream that is attached to each instance. If this OutputStream.getLine() returns null for a long time, we know that this instance is non-responsive, and we close and remove the socket from our list. When the crashed instance recovers, then, it can safely re-create its connection to each instance.