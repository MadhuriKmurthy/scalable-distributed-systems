package project1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class TCPServer {

	public static void main(String[] args) throws IOException {

		FileWriter fileWriter = null;
		try {
			// Step 1 : Create new file or use the existing client log file.
			fileWriter = createFileWriter();

			// Step 2 : Create a hashmap to store the key value.
			Map<String, String> keyValueMap = new HashMap<>();

			// Step 3 : Get the port number from user.
			if (args.length < 1) {
				System.out.println("Please enter the port number");
			}
			String inputPort = args[0];
			int serverPort = 0;
			if (inputPort != null) {
				serverPort = Integer.parseInt(inputPort);
			}

			// Step 3 : Create new socket for the port and accept the connection.
			ServerSocket socket = new ServerSocket(serverPort);
			Socket clientSocket = socket.accept();

			// Step 4 : Create new print writer for the socket output stream
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			while (true) {

				String userInput = in.readLine();

				// Step 5 : Print and log the client request.
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String requestFromClient = timestamp + ": Recieved from " + clientSocket.getInetAddress() + ", port: "
						+ socket.getLocalPort() + ": " + userInput;
				fileWriter.write(requestFromClient + "\n");
				fileWriter.flush();

				// Step 6 : Update the key value store based on the user command.
				String sendString = updateKeyValueMap(keyValueMap, userInput);

				// Step 7 : Send the response to client.
				out.println(sendString);

				// Step 8 : Print and log the response.
				String responseToClient = timestamp + ": Sending to client at port: " + socket.getLocalPort() + ": "
						+ sendString;
				fileWriter.write(responseToClient + "\n");
				fileWriter.flush();

			}

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
	 * A utility method to perform various operation on the key-value store.
	 * 
	 * @param keyValueMap hashmap representation of the key-value store.
	 * @param userInput   input commands from the user.
	 * @return server response for the user input.
	 */
	private static String updateKeyValueMap(Map<String, String> keyValueMap, String userInput) {

		String[] commands = userInput.split("-");

		if ("PUT".equalsIgnoreCase(commands[0])) {
			try {

				keyValueMap.put(commands[1], commands[2]);
				return "Key " + commands[1] + " value " + commands[2] + " inserted";

			} catch (ArrayIndexOutOfBoundsException ae) {
				return "Invalid PUT command. It must be in the format PUT-Key-Value";
			}

		}
		if ("GET".equalsIgnoreCase(commands[0])) {
			try {
				if (keyValueMap != null && !keyValueMap.isEmpty() && keyValueMap.containsKey(commands[1])) {
					return "Value for key " + commands[1] + " is :" + keyValueMap.get(commands[1]);
				} else {
					return "Key-value store does not contain the given key";
				}
			} catch (ArrayIndexOutOfBoundsException ae) {
				return "Invalid GET command. It must be in the format GET-Key";
			}
		}
		if ("DELETE".equalsIgnoreCase(commands[0])) {
			try {
				if (keyValueMap != null && !keyValueMap.isEmpty() && keyValueMap.containsKey(commands[1])) {

					keyValueMap.remove(commands[1]);
					return "Deleted key : " + commands[1];

				} else {
					return "Unnable to delete. Key-value store does not contain the given key";
				}

			} catch (ArrayIndexOutOfBoundsException ae) {
				return "Invalid DELETE command. It must be in the format DELETE-Key";
			}
		}

		return "Received unsolicited response acknowledging unknown PUT/GET/DELETE with an invalid KEY";
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
			String fileName = currentDirectory + "\\TCPServerLog.txt";
			File serverLogFile = new File(fileName);
			if (!serverLogFile.exists()) {
				serverLogFile.createNewFile();
				System.out.println("TCP server log file created: " + fileName);
			} else {
				System.out.println("Using TCP Server log file already exists at : " + fileName);
			}
			fileWriter = new FileWriter(serverLogFile, true);

		} catch (IOException e) {
			System.err.println("Error creating TCP server log file!");
		}
		return fileWriter;
	}

}
