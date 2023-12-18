package project2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Timestamp;


public class RMIServer {

	public static void main(String[] args) throws IOException {

		FileWriter fileWriter = null;

		try {
			// Step 1 : Create new file or use the existing server log file.
			fileWriter = createFileWriter();

			// Step 2 : Get the port number from user.
			if (args.length < 1) {
				System.out.println("Please enter the port number");
			}
			String inputPort = args[0];
			int serverPort = 0;
			if (inputPort != null) {
				serverPort = Integer.parseInt(inputPort);
			}

			// Step 3: Create an instance of the remote object.
			RMIInterface remoteObject = new RemoteObject();
			
			// Step 4: Create an RMI registry on user entered port.
            Registry registry = LocateRegistry.createRegistry(serverPort);
            
            // Step 5: Bind the remote object to the RMI registry
            registry.rebind("MyKeyValueMap", remoteObject);

			System.out.println("Server is ready!");
			
		} catch (Exception e) {

			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String error = timestamp + ": Error in TCP Server : " + e.getMessage();
			fileWriter.write(error + "\n");
			fileWriter.flush();
			
		} finally {
			// Close the file writer.
			fileWriter.close();
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
			String fileName = currentDirectory + "\\RMIServerLog.txt";
			File serverLogFile = new File(fileName);
			if (!serverLogFile.exists()) {
				serverLogFile.createNewFile();
				System.out.println("RMI server log file created: " + fileName);
			} else {
				System.out.println("Using RMI Server log file already exists at : " + fileName);
			}
			fileWriter = new FileWriter(serverLogFile, true);

		} catch (IOException e) {
			System.err.println("Error creating RMI server log file!");
		}
		return fileWriter;
	}

}
