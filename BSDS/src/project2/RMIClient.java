package project2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Scanner;

public class RMIClient {

	private static RMIInterface rmiInterface;

	public static void main(String[] args) throws NotBoundException, IOException {

		FileWriter fileWriter = null;
		try {
			// Step 1 : Create new file or use the existing client log file.
			fileWriter = createFileWriter();
			
			// Step 2 : Get the server IP and port from the program arguments.
			if (args.length < 2) {
				System.out.println("Please enter IP address of server followed by port number");
			}
			String inputIPAddress = args[0];
			String inputPort = args[1];

			int serverPort = 0;
			if (inputPort != null && inputPort.matches("[0-9]+")) {
				serverPort = Integer.parseInt(inputPort);
			} else {
				System.out.println("Invalid port. Please enter valid port number.");
				System.exit(0);
			}

			
			rmiInterface = (RMIInterface) Naming.lookup("rmi://"+inputIPAddress+":"+serverPort+"/MyKeyValueMap");

			// Step 4: Pre populate data
			String prepopulatedInput = prepopulate();
			Scanner scanner = new Scanner(prepopulatedInput);
			while (scanner.hasNextLine()) {
				sendDataToServer(scanner.nextLine(), fileWriter);
			}
			scanner.close();

			Scanner in = new Scanner(System.in);

			// Step 5 : Get the input commands from the user, until "quit" or "q".
			while (true) {
				System.out.println("Enter the command :");
				String userInput = in.nextLine();

				// break the loop if user enters "quit" or "q"
				if ("quit".equalsIgnoreCase(userInput) || "q".equalsIgnoreCase(userInput)) {
					System.out.println("Quitting from client!");
					break;
				}

				sendDataToServer(userInput, fileWriter);
			}
		} catch (ConnectException e1) {

			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String connection = timestamp + ": Connection refused to host expection occured!";
			fileWriter.write(connection + "\n");
			fileWriter.flush();

			System.err.println(connection);

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
			String fileName = currentDirectory + "\\RMIClientLog.txt";

			File clientLogFile = new File(fileName);

			if (!clientLogFile.exists()) {
				clientLogFile.createNewFile();
				System.out.println("RMI client log file created: " + fileName);
			} else {
				System.out.println("RMI client log file already exists at : " + fileName);
			}

			fileWriter = new FileWriter(clientLogFile, true);

		} catch (IOException e) {
			System.err.println("Error creating RMI client log file!");
		}
		return fileWriter;
	}

	/**
	 * A utility function to create packet and send the data to the server and
	 * getting the data back from server.
	 * 
	 * @param out        output stream.
	 * @param userInput  user input.
	 * @param in         buffered reader.
	 * @param socket     socket
	 * @param fileWriter file writer
	 * @throws IOException if unable to send the packet in socket.
	 */
	private static void sendDataToServer(String userInput, FileWriter fileWriter) throws IOException {
		try {

			String[] commands = userInput.split("-");
			
			String serverResponse = null;
			
			if ("PUT".equalsIgnoreCase(commands[0])) {

				serverResponse = rmiInterface.put(commands[1], commands[2]);
				
			} else if ("GET".equalsIgnoreCase(commands[0])) {
				
				serverResponse = rmiInterface.get(commands[1]);

			} else if ("DELETE".equalsIgnoreCase(commands[0])) {
				
				serverResponse = rmiInterface.delete(commands[1]);

			} else {
				serverResponse = "Received unsolicited response acknowledging unknown PUT/GET/DELETE with an invalid KEY";
			}

			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String responseFromServer = timestamp + ": Recieved from server : " + serverResponse;

			// Step 7 : Write the server response to client log file.
			fileWriter.write(responseFromServer + "\n");
			fileWriter.flush();

			// Step 8 : Also print the server response.
			System.out.println(responseFromServer);

		} catch (Exception e) {

			// Step 9 : Also capture any server timeout error.
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String timeoutError = timestamp + ": Server timeout! Please try again";

			fileWriter.write(timeoutError + "\n");
			fileWriter.flush();

			System.err.println(timeoutError);
		}
	}

	/**
	 * A utility function to execute put, get and delete
	 * 
	 * @return String of all input commands.
	 */
	private static String prepopulate() {
		StringBuilder sb = new StringBuilder();
		sb.append("PUT-A-Apple\n");
		sb.append("PUT-B-Ball\n");
		sb.append("PUT-C-Cat\n");
		sb.append("Put-D-Dog\n");
		sb.append("PUT-E-Elephant\n");
		sb.append("PUT-F-Fish\n");
		sb.append("put-G-Goat\n");
		sb.append("PUT-H-Hippo\n");
		sb.append("PUT-I-Impala\n");
		sb.append("GET-D\n");
		sb.append("get-B\n");
		sb.append("GET-A\n");
		sb.append("GET-C\n");
		sb.append("GET-F\n");
		sb.append("GET-g\n");
		sb.append("get-E\n");
		sb.append("GET-h\n");
		sb.append("DELETE-C\n");
		sb.append("DELETE-A\n");
		sb.append("DELETE-E\n");
		sb.append("DELETE-G\n");
		sb.append("DELETE-B\n");
		sb.append("DELETE-D\n");
		sb.append("DELETE-F\n");
		sb.append("DELETE-H\n");
		sb.append("DELETE-I\n");
		return sb.toString();
	}
}
