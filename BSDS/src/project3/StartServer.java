package project3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StartServer {

	public static void main(String[] args) throws IOException {

		FileWriter fileWriter = null;

		try {
			// Step 1 : Create new file or use the existing server log file.
			fileWriter = createFileWriter();

			// Step 2 : Get the 5 port numbers from user and add it to a list.
			if (args.length < 5) {
				System.out.println("Please enter 5 port numbers");
			}

			// Step 3 : List all the port numbers. If its same as 9000, request new.
			List<Integer> serverPortList = new ArrayList<>();
			for (int i = 0; i < args.length; i++) {

				if (args[i] != null && args[i].matches("[0-9]+")) {

					int port = Integer.parseInt(args[i]);

					if (port == 9000) {
						System.out.println("You have chosen a participant port which is also a coordinator. "
								+ "Please chose any port other than 9000");
						System.exit(0);
					} else {
						serverPortList.add(port);
					}
				}
			}

			// Step 4 : Create the participants and bind them to a RMI registry.
			List<Participant> participantsList = new ArrayList<>();
			for (int i = 0; i < args.length; i++) {

				ParticipantImpl participant = new ParticipantImpl(fileWriter);

				// Create an RMI registry on each participant port given by the user.
				Registry participantRegistry = LocateRegistry.createRegistry(serverPortList.get(i));
				// Bind the participants.
				participantRegistry.bind("MyKeyValueMap", participant);

				participantsList.add(participant);
			}

			// Step 5 : Send the list of participants/servers to the coordinator.
			CoordinatorImpl coordinator = new CoordinatorImpl(
					Arrays.asList("localhost", "localhost", "localhost", "localhost", "localhost"), serverPortList);

			// Step 6 : Create an RMI registry on coordinator port(default port).
			Registry coordinatorRegistry = LocateRegistry.createRegistry(9000);

			// Step 7 : Bind the coordinator.
			coordinatorRegistry.bind("Coordinator", coordinator);

			// Step 8 : For each of participant, set the created coordinator, on a specific port.
			for (int i = 0; i < participantsList.size(); i++) {
				participantsList.get(i).setCoordinator("localhost", 9000);
			}

			System.out.println(participantsList.size() + " participants/servers and 1 coordinator is ready!");

		} catch (Exception e) {
			System.err.println(
					"Error while starting the partcipants/servers or coordinator. Please try again. " + e.getMessage());
		}
	}

	/**
	 * A utility method to create log file and provide the ability to write into it.
	 * Created new file if not present, uses existing file if present.
	 * 
	 * @return file write of client log file.
	 */
	private static FileWriter createFileWriter() {
		FileWriter fileWriter = null;
		try {
			// Get the current directory where the Java file is located
			String currentDirectory = System.getProperty("user.dir");

			// Define the file name and content to write
			String fileName = currentDirectory + "\\2PCServerLog.txt";
			File serverLogFile = new File(fileName);
			if (!serverLogFile.exists()) {
				serverLogFile.createNewFile();
				System.out.println("2PC server log file created: " + fileName);
			} else {
				System.out.println("Using 2PC Server log file already exists at : " + fileName);
			}
			fileWriter = new FileWriter(serverLogFile, true);

		} catch (IOException e) {
			System.err.println("Error creating 2PC server log file!");
		}
		return fileWriter;
	}
}
