package project2;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RemoteObject extends UnicastRemoteObject implements RMIInterface {

	// Step 1: Initialize 2 types of lock. Write and Read reentrant lock - provides
	// synchronization to methods while accessing shared resources
	ReadWriteLock lock;

	// If no threads are reading or writing, only one thread can acquire the write
	// lock
	Lock writeLock;

	// If no thread acquired the write lock or requested for it, multiple threads
	// can acquire the read lock
	Lock readLock;

	// Step 2 : Create a hashmap to store the key value.
	ConcurrentHashMap<String, String> keyValueMap;

	protected RemoteObject() throws RemoteException {
		super();
		this.keyValueMap = new ConcurrentHashMap<>();

		this.lock = new ReentrantReadWriteLock();
		this.writeLock = lock.writeLock();
		this.readLock = lock.readLock();
	}

	@Override
	public String put(String key, String value) throws RemoteException {

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

	@Override
	public String get(String key) throws RemoteException {

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

	@Override
	public String delete(String key) throws RemoteException {

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

	@Override
	public int getMapSize() {
		return keyValueMap.size();
	}
}
