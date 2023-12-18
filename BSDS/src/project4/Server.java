package project4;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server extends UnicastRemoteObject implements IProposer, IAcceptor, ILearner, IMap {

	private static final long serialVersionUID = -8674360608560961289L;

	// Create a hashmap to store the key value.
	private ConcurrentHashMap<String, String> keyValueMap;

	// Store a list of acceptors and learners.
	private IAcceptor[] acceptors;
	private ILearner[] learners;

	// ID associate with each server.
	private int serverId;

	// ID for proposals and promise.
	private int proposalId;
	private int promisedId;

	// Simulating failuers.
	private static final double FAILURE_RATE = 0.2;
	private Random random = new Random();

	private static final int CONST_INCREASE = 5;
	private static final int MAJORITY = 3;

	// Initialize 2 types of lock. Write and Read reentrant lock - provides
	// synchronization to methods while accessing shared resources
	ReadWriteLock lock;

	// If no threads are reading or writing, only one thread can acquire the write
	// lock
	Lock writeLock;

	// If no thread acquired the write lock or requested for it, multiple threads
	// can acquire the read lock
	Lock readLock;

	FileWriter fileWriter;

	/**
	 * Constructor to create a Server instance.
	 * 
	 * @param serverId   The unique ID of this server.
	 * @param proposalId The starting proposal ID of this server.
	 * @param fileWriter
	 * @throws RemoteException for exceptions that may occur during the execution of
	 *                         a remote method call.
	 */
	protected Server(int serverId, int proposalId, FileWriter fileWriter) throws RemoteException {
		this.keyValueMap = new ConcurrentHashMap<>();
		this.serverId = serverId;
		this.proposalId = CONST_INCREASE + proposalId;
		this.promisedId = 0;
		this.lock = new ReentrantReadWriteLock();
		this.writeLock = lock.writeLock();
		this.readLock = lock.readLock();
		this.fileWriter = fileWriter;
	}

	/**
	 * Set the acceptors for this server.
	 * 
	 * @param acceptors Array of acceptors.
	 */
	public void setAcceptors(IAcceptor[] acceptors) {
		this.acceptors = acceptors;
	}

	/**
	 * Set the learners for this server.
	 * 
	 * @param learners Array of learners.
	 */
	public void setLearners(ILearner[] learners) {
		this.learners = learners;
	}

	/**** PROPOSER OPERATIONS ****/

	/**
	 * Propose an operation to be applied.
	 * 
	 * @param operation The operation to be proposed.
	 * @throws RemoteException If a remote error occurs.
	 */
	private void proposeOperation(Operation operation) throws RemoteException, IOException {
		int proposalId = generateProposalId();
		propose(proposalId, operation);
	}

	@Override
	public synchronized void propose(int proposalId, Operation proposalValue) throws RemoteException, IOException {
		printToConsoleLog("Server ID: "+this.serverId + " proposing with proposal ID "+ proposalId);
		printToServerLog("Server ID: "+this.serverId + " proposing with proposal ID "+ proposalId);
		
		// check for promises from other servers.
		boolean promiseResponses = sendPromise(proposalId);

		// if got majority promises.
		if (promiseResponses) {

			printToConsoleLog("Recieved majority promises");
			printToServerLog("Recieved majority promises");

			boolean acceptResponses = sendAccept(proposalId, proposalValue);

			if (acceptResponses) {

				// apply operation for self
				applyOperation(proposalValue);

				printToConsoleLog("Recieved majority accepts");
				printToServerLog("Recieved majority accepts");

				for (ILearner learner : this.learners) {
					if (learner != null) {
						try {
							// learn from all the server.
							learn(proposalId, proposalValue);

						} catch (RemoteException e) {
							printToConsoleLog("Failed to learn: server ID: " + this.serverId);
							printToServerLog("Failed to learn: server ID: " + this.serverId);
						}
					}
				}
			} else {
				printToConsoleLog("Did not recieve majority accepts - retry with higher proposal ID");
				printToServerLog("Did not recieve majority accepts - retry with higher proposal ID");
				reTryPrepare(proposalValue);
			}
		} else {
			printToConsoleLog("Did not recieve majority promises - retry with higher proposal ID");
			printToServerLog("Did not recieve majority promises - retry with higher proposal ID");
			reTryPrepare(proposalValue);
		}
	}

	/**
	 * private method to send promises to all other servers and receive response.
	 * 
	 * @param proposalId proposal ID of the proposer server.
	 * @return true if majority of server promise, false otherwise.
	 */
	private boolean sendPromise(int proposalId) {

		// check promise from all the servers in parallel.
		List<Future<Boolean>> futures = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(acceptors.length);

		for (IAcceptor acceptor : this.acceptors) {

			if (acceptor != null) {

				Callable<Boolean> acceptTask = () -> {
					try {
						return acceptor.prepare(proposalId);
					} catch (RemoteException e) {

						printToConsoleLog("Failed to prepare/promise: server ID: " + this.serverId);
						printToServerLog("Failed to prepare/promise: server ID: " + this.serverId);
						return false;
					}
				};
				Future<Boolean> future = executor.submit(acceptTask);
				futures.add(future);
			}
		}

		// Set timeout for 0.5 seconds
		long timeout = 500;

		List<Boolean> promiseResponses = new ArrayList<>();
		try {
			for (Future<Boolean> future : futures) {
				promiseResponses.add(future.get(timeout, TimeUnit.MILLISECONDS));
			}
		} catch (InterruptedException | ExecutionException e) {
			printToConsoleLog("Thread execution interrupted or failed: " + e.getMessage());
		} catch (TimeoutException e) {
			printToConsoleLog("Timeout reached while waiting for threads: " + e.getMessage());
		} finally {
			executor.shutdownNow();
		}

		// if majority of response promised, return true, else false
		long respPos = promiseResponses.stream().filter(s -> s == true).count();
		if (respPos >= MAJORITY) {
			return true;
		}
		return false;
	}

	/**
	 * private method to retry propose with a higher proposal value.
	 * 
	 * @param proposalValue proposal value of the proposer server.
	 * @throws IOException If unable to write to server log.
	 */
	private void reTryPrepare(Operation proposalValue) throws IOException {
		try {
			// wait and propose with a higher proposal value.
			Thread.sleep(100);
			propose(generateProposalId(), proposalValue);

		} catch (InterruptedException e) {

			printToConsoleLog("Interrupted Exception during retry propose " + e.getMessage());
			printToServerLog("Interrupted Exception during retry propose " + e.getMessage());

		} catch (RemoteException e) {

			printToConsoleLog("Remote Exception during retry propose " + e.getMessage());
			printToServerLog("Remote Exception during retry propose " + e.getMessage());
		}
	}

	/**
	 * private method to send accept message to all servers.
	 * 
	 * @param proposalId    proposal ID of the server.
	 * @param proposalValue operation that is proposed.
	 * @return true if majority of servers, accepted, false otherwise.
	 */
	private boolean sendAccept(int proposalId, Operation proposalValue) {

		// check accept from all the servers in parallel.
		List<Future<Boolean>> futures = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(acceptors.length);

		for (IAcceptor acceptor : this.acceptors) {

			if (acceptor != null) {

				Callable<Boolean> acceptTask = () -> {
					try {
						// Check for acceptors.
						return acceptor.accept(proposalId, proposalValue);

					} catch (RemoteException e) {

						printToConsoleLog("Accept phase failed - Consensus Failed!");
						printToServerLog("Accept phase failed - Consensus Failed!");
						return false;
					}
				};

				Future<Boolean> future = executor.submit(acceptTask);
				futures.add(future);
			}
		}

		// Set timeout for 0.5 seconds.
		long timeout = 500;
		List<Boolean> acceptResponsePairs = new ArrayList<>();
		try {
			for (Future<Boolean> future : futures) {
				acceptResponsePairs.add(future.get(timeout, TimeUnit.MILLISECONDS));
			}
		} catch (InterruptedException | ExecutionException e) {
			printToConsoleLog("Thread execution interrupted or failed");
		} catch (TimeoutException e) {
			printToConsoleLog("Timeout reached while waiting for threads");
		} finally {
			executor.shutdownNow();
		}

		// if majority of response accepted, return true, else false
		long respAcc = acceptResponsePairs.stream().filter(s -> true).count();
		if (respAcc >= MAJORITY) {
			return true;
		}
		return false;
	}

	/**** ACCEPTOR OPERATIONS ****/
	@Override
	public synchronized boolean prepare(int proposalId) throws RemoteException, IOException {
		// Simulate failure during prepare
		if (random.nextDouble() < FAILURE_RATE) {

			printToConsoleLog("Simulated failure during prepare: server ID:" + this.serverId);
			printToServerLog("Simulated failure during prepare: server ID:" + this.serverId);

			throw new RemoteException("Simulated failure during prepare");
		}
		if (this.promisedId < proposalId) {
			// promise!
			this.promisedId = proposalId;
			return true;
		} else {
			// don't promise!
			return false;
		}
	}

	@Override
	public synchronized boolean accept(int proposalId, Operation proposalValue) throws RemoteException, IOException {
		// Simulate failure in accept.
		if (random.nextDouble() < FAILURE_RATE) {

			printToConsoleLog("Simulated failure during accept: server ID:" + this.serverId);
			printToServerLog("Simulated failure during accept: server ID:" + this.serverId);

			throw new RemoteException("Simulated failure during accept");
		}
		// Accept
		if (this.promisedId <= proposalId) {
			applyOperation(proposalValue);

			printToConsoleLog(
					"ACCEPT: " + "operation : " + proposalValue.getOperationType() + "\t key: " + proposalValue.getKey()
							+ "\t value:" + proposalValue.getValue());

			printToServerLog(
					"ACCEPT: " + "operation : " + proposalValue.getOperationType() + "\t key: " + proposalValue.getKey()
							+ "\t value:" + proposalValue.getValue());

			return true;
		}
		return false;
	}

	/****
	 * LEARNER OPERATIONS
	 * 
	 * @throws RIOException
	 ****/
	@Override
	public synchronized void learn(int proposalId, Operation acceptedValue) throws RemoteException, IOException {
		this.promisedId = proposalId;

		applyOperation(acceptedValue);

		printToConsoleLog("LEARN: " + "operation : " + acceptedValue.getOperationType() + "\t key: "
				+ acceptedValue.getKey() + "\t value:" + acceptedValue.getValue());

		printToServerLog("LEARN: " + "operation : " + acceptedValue.getOperationType() + "\t key: "
				+ acceptedValue.getKey() + "\t value:" + acceptedValue.getValue());
	}

	/**
	 * Generates a unique proposal ID.
	 * 
	 * @return A unique proposal ID.
	 */
	private int generateProposalId() {
		this.proposalId = CONST_INCREASE + this.proposalId;
		return this.proposalId;
	}

	/**** MAP OPERATIONS ****/

	/**
	 * Apply the given operation to the key-value store.
	 * 
	 * @param operation The operation to apply.
	 */
	private String applyOperation(Operation operation) {
		if (operation == null)
			return "";

		switch (operation.getOperationType()) {
		case "PUT":
			return executePut(operation.getKey(), operation.getValue());
		case "GET":
			return executeGet(operation.getKey());
		case "DELETE":
			return executeDelete(operation.getKey());
		default:
			return "Received unsolicited response acknowledging unknown PUT/GET/DELETE with an invalid KEY";
		}
	}

	@Override
	public String put(String key, String value) throws RemoteException, IOException {
		proposeOperation(new Operation("PUT", key, value));
		return "Key " + key + " value " + value + " inserted";
	}

	@Override
	public String get(String key) throws RemoteException, IOException {
		return applyOperation(new Operation("GET", key, null));
	}

	@Override
	public String delete(String key) throws RemoteException, IOException {
		proposeOperation(new Operation("DELETE", key, null));
		return "DELETE operation successful - please check using GET";
	}

	@Override
	public int getMapSize() throws RemoteException {
		return keyValueMap.size();
	}

	/**
	 * Private method to insert key and value into the map.
	 * 
	 * @param key   key to be inserted.
	 * @param value of the key to be inserted.
	 * @return response from the server.
	 */
	private String executePut(String key, String value) {
		writeLock.lock();

		StringBuffer sb = new StringBuffer();
		try {

			keyValueMap.put(key, value);
			sb.append("Key " + key + " value " + value + " inserted");

		} catch (ArrayIndexOutOfBoundsException ae) {
			sb.append("Invalid PUT command. It must be in the format PUT-Key-Value");
		} finally {
			writeLock.unlock();
		}
		return sb.toString();
	}

	/**
	 * Private method to get the value for the key from the hash map, if exists.
	 * 
	 * @param key key to be inserted.
	 * @return response from the server.
	 */
	private String executeGet(String key) {
		readLock.lock();

		StringBuffer sb = new StringBuffer();
		try {

			if (keyValueMap != null && !keyValueMap.isEmpty() && keyValueMap.containsKey(key)) {
				sb.append("Value for key " + key + " is :" + keyValueMap.get(key));
			} else {
				sb.append("Key-value store does not contain the given key");
			}
		} catch (ArrayIndexOutOfBoundsException ae) {
			sb.append("Invalid GET command. It must be in the format GET-Key");
		} finally {
			readLock.unlock();
		}
		return sb.toString();
	}

	/**
	 * Private method to delete the key and value from the hash map, if exists.
	 * 
	 * @param key key to be inserted.
	 * @return response from the server.
	 */
	private String executeDelete(String key) {

		writeLock.lock();

		StringBuffer sb = new StringBuffer();
		try {

			if (keyValueMap != null && !keyValueMap.isEmpty() && keyValueMap.containsKey(key)) {

				keyValueMap.remove(key);
				sb.append("Deleted key : " + key);

			} else {
				sb.append("Unnable to delete. Key-value store does not contain the given key");
			}
		} catch (ArrayIndexOutOfBoundsException ae) {
			sb.append("Invalid DELETE command. It must be in the format DELETE-Key");
		} finally {
			writeLock.unlock();
		}
		return sb.toString();
	}

	/**
	 * Private method to print message to server log.
	 * 
	 * @param message message to be printed.
	 * @throws IOException for exceptions that may occur during writing to server
	 *                     log.
	 */
	private void printToServerLog(String message) throws IOException {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String fullMsg = timestamp + ": " +message;
		fileWriter.write(fullMsg + "\n");
		fileWriter.flush();
	}

	/**
	 * Private method to print message to console log.
	 * 
	 * @param message message to be printed.
	 */
	private void printToConsoleLog(String message) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String fullMsg = timestamp +": " +message + "\n";
		System.out.print(fullMsg);
	}

}
