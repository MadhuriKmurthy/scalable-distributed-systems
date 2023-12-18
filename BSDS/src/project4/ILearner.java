package project4;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The LearnerInterface represents a remote interface that defines the learning
 * process in the Paxos consensus algorithm. It contains the learning method to
 * acknowledge an accepted proposal.
 */
public interface ILearner extends Remote {

	/**
	 * The learn method is used to inform the Learner of an accepted proposal.
	 *
	 * @param proposalId    The unique identifier for the proposal.
	 * @param acceptedValue The value that has been accepted.
	 * @throws RemoteException If a remote invocation error occurs.
	 * @throws IOException     If unable to write to server log.
	 */
	void learn(int proposalId, Operation acceptedValue) throws RemoteException, IOException;
}
