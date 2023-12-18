package project4;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class PaxosServerStart {

	public static void main(String[] args) throws IOException {

		FileWriter fileWriter = null;

		try {
			// Step 1 : Create new file or use the existing server log file.
			fileWriter = createFileWriter();

			// Step 2 : Get the 5 port numbers from user and add it to a list.
			if (args.length < 5) {
				System.out.println("Please enter 5 port numbers");
			}

			// Step 3 : List all the port numbers.
			List<Integer> serverPortList = new ArrayList<>();
			for (int i = 0; i < args.length; i++) {

				if (args[i] != null && args[i].matches("[0-9]+")) {
					int port = Integer.parseInt(args[i]);
					serverPortList.add(port);
				}
			}

			// Step 4: Create instances of the servers.
			int numServers = args.length;
			Server[] servers = new Server[numServers];

			// Step 5: For each server, bind with the registry.
			for (int serverId = 0; serverId < numServers; serverId++) {

				// Create server instance
				servers[serverId] = new Server(serverId, serverId, fileWriter);

				// Create an RMI registry on each participant port given by the user.
				int port = serverPortList.get(serverId);
				Registry registry = LocateRegistry.createRegistry(port);

				// Bind the servers.
				registry.rebind("MyKeyValueMap" + serverPortList.get(serverId), servers[serverId]);

				System.out.println("Server " + serverId + " is ready at port " + port);
			}

			// Step 6: Set acceptors and learners for each server, other than its own.
			for (int serverId = 0; serverId < numServers; serverId++) {
				
				IAcceptor[] acceptors = new IAcceptor[numServers];
				ILearner[] learners = new ILearner[numServers];
				
				for (int i = 0; i < numServers; i++) {
					if (i != serverId) {
						acceptors[i] = servers[i];
						learners[i] = servers[i];
					}
				}
				servers[serverId].setAcceptors(acceptors);
				servers[serverId].setLearners(learners);
			}
			
			System.out.println("Servers are up!");

		} catch (Exception e) {
			System.err.println("Error while starting the paxos servers. Please try again. " + e.getMessage());
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
			String fileName = currentDirectory + "\\PaxosServerLog.txt";
			File serverLogFile = new File(fileName);
			if (!serverLogFile.exists()) {
				serverLogFile.createNewFile();
				System.out.println("Paxos server log file created: " + fileName);
			} else {
				System.out.println("Using Paxos Server log file already exists at : " + fileName);
			}
			fileWriter = new FileWriter(serverLogFile, true);

		} catch (IOException e) {
			System.err.println("Error creating Paxos server log file!");
		}
		return fileWriter;
	}

}
