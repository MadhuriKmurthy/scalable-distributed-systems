package project1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.Scanner;

public class UDPClient {

	public static void main(String[] args) throws IOException, InterruptedException {

		FileWriter fileWriter = null;
		try {
			// Step 1 : Create new file or use the existing client log file.
			fileWriter = createFileWriter();

			// Step 2 : Get the server IP and port from the command line arguments.
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

			// Create scanner to get user input.
			Scanner in = new Scanner(System.in);

			// Step 3 : Create the datagram socket object for carrying the data.
			DatagramSocket datagramSocket = new DatagramSocket();

			// Step 4 : Create InetAddress for the given IP address.
			InetAddress ip = InetAddress.getByName(inputIPAddress);

			// Step 5: Pre populate data
			String prepopulatedInput = prepopulate();
			Scanner scanner = new Scanner(prepopulatedInput);
			while (scanner.hasNextLine()) {
				sendDataToServer(scanner.nextLine(), ip, serverPort, datagramSocket, fileWriter);
			}
			scanner.close();

			// Step 5 : Get the input commands from the user, until "quit" or "q".
			while (true) {

				System.out.println("Enter the command :");
				String input = in.nextLine();

				// break the loop if user enters "quit" or "q"
				if ("quit".equalsIgnoreCase(input) || "q".equalsIgnoreCase(input)) {
					System.out.println("Quitting from client!");
					break;
				}

				sendDataToServer(input, ip, serverPort, datagramSocket, fileWriter);
			}

			// Step 17: Close the socket in client.
			datagramSocket.close();

		} catch (SocketException e1) {

			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String timeoutError = timestamp + ": Socket expection occured!";
			System.err.println(timeoutError);

		} finally {
			// Close the file writer.
			fileWriter.close();
		}
	}

	/**
	 * A utility function to create packet and send the data to the server and
	 * getting the data back from server.
	 * 
	 * @param input          user input from console.
	 * @param ip             IP address.
	 * @param serverPort     port.
	 * @param datagramSocket datagram socket.
	 * @param fileWriter     file writer
	 * @throws IOException if unable to send the packet in socket.
	 */
	private static void sendDataToServer(String input, InetAddress ip, int serverPort, DatagramSocket datagramSocket,
			FileWriter fileWriter) throws IOException {

		byte buffer[] = null;
		byte[] receive = new byte[65535];
		DatagramPacket DpReceive = null;

		// convert the String input into the byte array.
		buffer = input.getBytes();

		// Step 6 : Create the datagramPacket for sending the data.
		DatagramPacket DpSend = new DatagramPacket(buffer, buffer.length, ip, serverPort);

		// Step 7 : Invoke the send call to actually send the data.
		datagramSocket.send(DpSend);

		// Step 8 : Set the timeout in milliseconds.
		datagramSocket.setSoTimeout(1000);

		try {
			// Step 10 : create a DatgramPacket to receive the data.
			DpReceive = new DatagramPacket(receive, receive.length);

			// Step 11 : Receive the data in byte buffer.
			datagramSocket.receive(DpReceive);

			// Step 12 : Convert data in byte buffer to string
			String userInput = byteDataToString(receive);

			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String responseFromServer = timestamp + ": Recieved from " + DpReceive.getAddress() + ", port: "
					+ DpReceive.getPort() + ": " + userInput;

			// Step 13 : Write the server response to client log file.
			fileWriter.write(responseFromServer + "\n");
			fileWriter.flush();

			// Step 14 : Also print the server response.
			System.out.println(responseFromServer);

		} catch (SocketTimeoutException e) {

			// Step 15 : Also capture any server timeout error.
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String timeoutError = timestamp + ": Server timeout! Please try again";

			fileWriter.write(timeoutError + "\n");
			fileWriter.flush();

			System.err.println(timeoutError);
		}

		// Step 16 : Clear the buffer after every message.
		receive = new byte[65535];
		fileWriter.flush();
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
			String fileName = currentDirectory + "\\UDPClientLog.txt";
			File clientLogFile = new File(fileName);
			if (!clientLogFile.exists()) {
				clientLogFile.createNewFile();
				System.out.println("Client log file created: " + fileName);
			} else {
				System.out.println("Client log file already exists at : " + fileName);
			}

			fileWriter = new FileWriter(clientLogFile, true);

		} catch (IOException e) {
			System.err.println("Error creating client log file!");
		}
		return fileWriter;
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
