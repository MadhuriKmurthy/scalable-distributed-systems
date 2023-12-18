package project3;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a coordinator.
 */
public class CoordinatorImpl extends UnicastRemoteObject implements Coordinator {

	private static final long serialVersionUID = 3916385668266522359L;

	// Step 1. Coordinator consists of a list of all the participants.
	private List<Participant> participants;

	public CoordinatorImpl(List<String> participantHosts, List<Integer> participantPorts) throws RemoteException {
		super();
		participants = new ArrayList<>();

		for (int i = 0; i < participantHosts.size(); i++) {
			try {

				// Step 2: Get the RMI registry of all the participants and add to the list of participants.
				Registry registry = LocateRegistry.getRegistry(participantHosts.get(i), participantPorts.get(i));
				Participant participant = (Participant) registry.lookup("MyKeyValueMap");

				participants.add(participant);

			} catch (Exception e) {
				throw new RemoteException("Unable to connect to participant", e);
			}
		}

	}

	@Override
	public String prepareTransaction(String command, String key, String value) throws IOException {

		// Step 3: For each participant check if is its okay/ready to commit. If not, abort!
		for (Participant participant : participants) {
			if (!participant.vote(command, key, value)) {
				return "Aborting! - either key is already present for put, or key is absent for delete";
			}
		}
		// Step 4: If okay, then commit to all the participants.
		List<String> commitMessages = new ArrayList<>();
		for (Participant participant : participants) {
			commitMessages.add(participant.commit());
		}
		// Step 5:If all the participants gave a response then all were committed.
		if (commitMessages.size() == participants.size()) {
			return commitMessages.get(0);
		}

		return "Unable to commit to one or more servers! Try again!";

	}

}
