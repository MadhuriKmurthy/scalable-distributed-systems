package project1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class UDPServer {

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

			// Step 4 : Create a datagram socket to listen at port
			DatagramSocket datagramSocket = new DatagramSocket(serverPort);

			byte[] receive = new byte[65535];
			DatagramPacket DpReceive = null;

			while (true) {

				// Step 5 : create a Datagram Packet to receive the data.
				DpReceive = new DatagramPacket(receive, receive.length);

				String userInput = null;
				try {
					// Step 3 : revieve the data in byte buffer.
					datagramSocket.receive(DpReceive);

					// Step 4 : Convert data in byte buffer to string
					userInput = byteDataToString(receive);

				} catch (IllegalArgumentException e) {
					// Handle malformed packet exception
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					String malformedDg = timestamp + ": Malformed datagram packet received from "
							+ DpReceive.getAddress() + ", port: " + DpReceive.getPort() + ": " + userInput;
					fileWriter.write(malformedDg + "\n");
					fileWriter.flush();
				}

				// Step 5 : Print and log the client request.
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String requestFromClient = timestamp + ": Recieved from " + DpReceive.getAddress() + ", port: "
						+ DpReceive.getPort() + ": " + userInput;
				fileWriter.write(requestFromClient + "\n");
				fileWriter.flush();

				// Step 6 : Update the key value store based on the user command.
				String sendString = updateKeyValueMap(keyValueMap, userInput);

				// Step 7 : Send the response to client.
				byte[] sendData = sendString.getBytes("UTF-8");

				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, DpReceive.getAddress(),
						DpReceive.getPort());

				datagramSocket.send(sendPacket);

				// Step 8 : Print and log the response.
				String responseToClient = timestamp + ": Sending to " + sendPacket.getAddress() + ", port: "
						+ sendPacket.getPort() + ": " + sendString;
				fileWriter.write(responseToClient + "\n");
				fileWriter.flush();

				// Step 9 : Clear the buffer after every message.
				receive = new byte[65535];
			}

		} catch (Exception e) {
			
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String error = timestamp + ": Error in UDP Server : " + e.getMessage();
			fileWriter.write(error + "\n");
			fileWriter.flush();		
		}  finally {
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
			String fileName = currentDirectory + "\\UDPServerLog.txt";
			File serverLogFile = new File(fileName);
			if (!serverLogFile.exists()) {
				serverLogFile.createNewFile();
				System.out.println("UDP Server log file created: " + fileName);
			} else {
				System.out.println("Using UDP Server log file already exists at : " + fileName);
			}
			fileWriter = new FileWriter(serverLogFile, true);
		} catch (IOException e) {
			System.err.println("Error creating UDP server log file!");
		}
		return fileWriter;
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
				return "Key " + commands[1] + " value " + commands[2] + " inserted!";

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
	 * A utility method to convert the byte array data into a string representation.
	 * 
	 * @param array byte array.
	 * @return string representation of the byte array.
	 */
	public static String byteDataToString(byte[] array) {
		if (array == null)
			return null;

		StringBuilder returnString = new StringBuilder();
		int i = 0;
		while (array[i] != 0) {
			returnString.append((char) array[i++]);
		}

		return returnString.toString();
	}

}
