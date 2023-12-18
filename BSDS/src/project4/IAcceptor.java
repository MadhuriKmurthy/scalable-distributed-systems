package project4;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The AcceptorInterface defines the remote methods to be implemented by the
 * acceptors in the Paxos consensus algorithm. It includes methods for preparing
 * and accepting proposals.
 */
public interface IAcceptor extends Remote {

	/**
	 * 
	 * Prepares the acceptor to receive a proposal with a given proposal ID.
	 *
	 * @param proposalId The unique ID of the proposal.
	 * @return An integer response indicating the status or decision related to the
	 *         proposal.
	 * @throws RemoteException If a remote communication error occurs.
	 * @throws IOException     If unable to write to server log.
	 */
	boolean prepare(int proposalId) throws RemoteException, IOException;

	/**
	 * Accepts or rejects a proposal with the given proposal ID and value.
	 *
	 * @param proposalId    The unique ID of the proposal.
	 * @param proposalValue The value of the proposal.
	 * @return A boolean indicating whether the proposal was accepted (true) or
	 *         rejected (false).
	 * @throws RemoteException If a remote communication error occurs.
	 * @throws IOException     If unable to write to server log.
	 */
	boolean accept(int proposalId, Operation proposalValue) throws RemoteException, IOException;
}
