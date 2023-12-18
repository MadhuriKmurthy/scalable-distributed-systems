package project1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.Scanner;

public class TCPClient {
	public static void main(String[] args) throws IOException {

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

			// Step 3 : Create new socket for the input IP address and port
			Socket socket = new Socket(inputIPAddress, serverPort);

			// Step 4 : Create new print writer for the socket output stream
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

			// Step 5 : Set the timeout in milliseconds.
			socket.setSoTimeout(1000);

			// Step 6: Pre populate data
			String prepopulatedInput = prepopulate();
			Scanner scanner = new Scanner(prepopulatedInput);
			while (scanner.hasNextLine()) {
				sendDataToServer(out, scanner.nextLine(), in, socket, fileWriter);
			}
			scanner.close();

			// Step 6 : Get the input commands from the user, until "quit" or "q".
			while (true) {

				System.out.println("Enter the command :");
				String userInput = stdIn.readLine();

				// break the loop if user enters "quit" or "q"
				if ("quit".equalsIgnoreCase(userInput) || "q".equalsIgnoreCase(userInput)) {
					System.out.println("Quitting from client!");
					break;
				}

				sendDataToServer(out, userInput, in, socket, fileWriter);

			}

		} catch (SocketException e1) {

			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String timeoutError = timestamp + ": Socket expection occured!";
			fileWriter.write(timeoutError + "\n");
			fileWriter.flush();

			System.err.println(timeoutError);

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
			String fileName = currentDirectory + "\\TCPClientLog.txt";

			File clientLogFile = new File(fileName);

			if (!clientLogFile.exists()) {
				clientLogFile.createNewFile();
				System.out.println("Client log file created: " + fileName);
			} else {
				System.out.println("Client log file already exists at : " + fileName);
			}

			fileWriter = new FileWriter(clientLogFile, true);

		} catch (IOException e) {
			System.err.println("Error creating server log file!");
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
	private static void sendDataToServer(PrintWriter out, String userInput, BufferedReader in, Socket socket,
			FileWriter fileWriter) throws IOException {
		try {

			out.println(userInput);

			// Step 7 : Get the response from server.
			String serverResponse = in.readLine();

			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String responseFromServer = timestamp + ": Recieved from " + socket.getInetAddress() + ", port: "
					+ socket.getPort() + ": " + serverResponse;

			// Step 8 : Write the server response to client log file.
			fileWriter.write(responseFromServer + "\n");
			fileWriter.flush();

			// Step 9 : Also print the server response.
			System.out.println(responseFromServer);

		} catch (SocketTimeoutException e) {

			// Step 15 : Also capture any server timeout error.
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
