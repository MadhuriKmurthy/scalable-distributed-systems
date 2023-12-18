package project3;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface that represents operations on a participant/server.
 */
public interface Participant extends Remote {

	/**
	 * Function that represents the logic for voting phase for the participant.
	 * 
	 * @param command user input command.
	 * @param key     user input key.
	 * @param value   user input value.
	 * @return true if there are no issues, false if aborting.
	 * @throws RemoteException for exceptions that may occur during the execution of
	 *                         a remote method call.
	 * @throws IOException     for exceptions that may occur during writing to
	 *                         server log.
	 */
	boolean vote(String command, String key, String value) throws RemoteException, IOException;

	/**
	 * Function that represents the logic for commit phase. Involves operations on
	 * the key-value map.
	 * 
	 * @return response message from the server for each operation.
	 * @throws RemoteException for exceptions that may occur during the execution of
	 *                         a remote method call.
	 * @throws IOException     for exceptions that may occur during writing to
	 *                         server log.
	 */
	String commit() throws RemoteException, IOException;

	/**
	 * Function that supports linking the servers to the coordinator.
	 * 
	 * @param coordinatorHost hostname of the coordinator.
	 * @param coordinatorPort port of the coordinator.
	 * @throws RemoteException for exceptions that may occur during the execution of
	 *                         a remote method call.
	 */
	void setCoordinator(String coordinatorHost, int coordinatorPort) throws RemoteException;

}
