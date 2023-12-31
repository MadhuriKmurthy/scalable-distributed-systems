package project4;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The ProposerInterface provides a remote method to initiate a proposal in the
 * Paxos consensus algorithm. It is part of the Paxos distributed consensus
 * protocol, representing the proposing role.
 */
public interface IProposer extends Remote {

	/**
	 * Initiates a proposal with the given proposal ID and value.
	 *
	 * @param proposalId    The unique identifier for the proposal.
	 * @param proposalValue The value being proposed.
	 * @throws RemoteException If a remote invocation error occurs.
	 * @throws IOException     If unable to write to server log.
	 */
	void propose(int proposalId, Operation proposalValue) throws RemoteException, IOException;
}
