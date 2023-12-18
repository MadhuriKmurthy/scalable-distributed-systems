package project3;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface that represents operations on a server that acts a coordinator.
 */
public interface Coordinator extends Remote {

	/**
	 * Method that coordinator uses to implements the commit handling in two phases.
	 * It first sends the prepare request to each of the participants. Once it
	 * receives a successful response from all the participants, the coordinator
	 * marks the transaction as prepared to complete. Then it sends the commit
	 * request to all the participants.
	 * 
	 * @param command operation on the key-value map requested by the user.
	 * @param key     key to be inserted.
	 * @param value   value to be inserted.
	 * @return response from all the participants.
	 * @throws RemoteException for exceptions that may occur during the execution of
	 *                         a remote method call.
	 * @throws IOException     for exceptions that may occur during writing to
	 *                         server log.
	 */
	String prepareTransaction(String command, String key, String value) throws RemoteException, IOException;
}
