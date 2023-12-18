package project3;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class that represents a participant/server. 
 */
public class ParticipantImpl extends UnicastRemoteObject implements Participant, RMIInterface2 {

	private static final long serialVersionUID = -5823802982940282436L;
	// ID to identify different participants/servers.
	private UUID id;

	// Step 1 : Create a hashmap to store the key value.
	ConcurrentHashMap<String, String> keyValueMap;

	// Step 2 : Each participant created has access to the coordinator.
	private Coordinator coordinator;

	// Step 3: Assign the command and keys
	String command;
	String key;
	String value;

	// Step 4: Initialize 2 types of lock. Write and Read reentrant lock - provides
	// synchronization to methods while accessing shared resources
	ReadWriteLock lock;

	// If no threads are reading or writing, only one thread can acquire the write
	// lock
	Lock writeLock;

	// If no thread acquired the write lock or requested for it, multiple threads
	// can acquire the read lock
	Lock readLock;

	FileWriter fileWriter;

	public ParticipantImpl(FileWriter fileWriter) throws RemoteException {
		super();
		this.id = UUID.randomUUID();
		this.keyValueMap = new ConcurrentHashMap<>();
		this.lock = new ReentrantReadWriteLock();
		this.writeLock = lock.writeLock();
		this.readLock = lock.readLock();
		this.fileWriter = fileWriter;
	}

	@Override
	public void setCoordinator(String coordinatorHost, int coordinatorPort) throws RemoteException {

		try {
			//1. Get the RMI registry of the coordinator.
			Registry registry = LocateRegistry.getRegistry(coordinatorHost, coordinatorPort);
			coordinator = (Coordinator) registry.lookup("Coordinator");

		} catch (Exception e) {
			throw new RemoteException("Unable to connect to coordinator", e);
		}

	}
	
	@Override
	public boolean vote(String command, String key, String value) throws IOException {

		this.command = command;
		this.key = key;
		this.value = value;

		printToServerLog(": Voting Phase - Participant " + this.id);

		// 1. If key for PUT already, exists abort
		if ("PUT".equalsIgnoreCase(command)) {
			if (keyValueMap.containsKey(key)) {
				printToServerLog(": Aborting! Key-value store already contains the given key : " + key);
				return false;
			}
		}
		// 2. If key for DELETE does NOT exists, abort
		if ("DELETE".equalsIgnoreCase(command)) {
			if (!keyValueMap.containsKey(key)) {
				printToServerLog(": Aborting! Key-value store does NOT contain the given key : " + key);
				return false;
			}
		}
		// 3. All ok.
		return true;
	}

	@Override
	public String commit() throws IOException {

		printToServerLog(": Commit Phase - Participant " + this.id + " Command : " + this.command);

		if ("PUT".equalsIgnoreCase(this.command)) {
			return executePut(this.key, this.value);
		}
		if ("GET".equalsIgnoreCase(this.command)) {
			return executeGet(this.key);
		}
		if ("DELETE".equalsIgnoreCase(this.command)) {
			return executeDelete(this.key);
		}
		return null;
	}

	@Override
	public String put(String key, String value) throws IOException {
		return coordinator.prepareTransaction("PUT", key, value);
	}

	@Override
	public String get(String key) throws IOException {
		return coordinator.prepareTransaction("GET", key, null);
	}

	@Override
	public String delete(String key) throws IOException {
		return coordinator.prepareTransaction("DELETE", key, null);
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
		String voting = timestamp + message;
		fileWriter.write(voting + "\n");
		fileWriter.flush();
	}

}
