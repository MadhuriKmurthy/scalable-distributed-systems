package project2;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface class that declares a set of methods that may be invoked from a
 * remote client. Contains methods to put, get and delete from the key value
 * map.
 */
public interface RMIInterface extends Remote {

	/**
	 * Method to insert key and value into the hash map.
	 * 
	 * @param key   key to be inserted.
	 * @param value value of the key to be inserted.
	 * @return response from the server.
	 * @throws RemoteException for exceptions that may occur during the execution of
	 *                         a remote method call
	 */
	public String put(String key, String value) throws RemoteException;

	/**
	 * Method to get the value for the key from the hash map, if exists.
	 * 
	 * @param key key to be inserted.
	 * @return response from the server.
	 * @throws RemoteException for exceptions that may occur during the execution of
	 *                         a remote method call
	 */
	public String get(String key) throws RemoteException;

	/**
	 * Method to delete the key and value from the hash map, if exists.
	 * 
	 * @param key key to be inserted.
	 * @return response from the server.
	 * @throws RemoteException for exceptions that may occur during the execution of
	 *                         a remote method call
	 */
	public String delete(String key) throws RemoteException;

	/**
	 * Method to get the size of the hash map.
	 * 
	 * @return hash map size.
	 * @throws RemoteException for exceptions that may occur during the execution of
	 *                         a remote method call
	 */
	public int getMapSize() throws RemoteException;

}
