
// TFTPClient.java
// This class is the client side for a very simple assignment based on TFTP on
// UDP/IP. The client uses one port and sends a read or write request and gets 
// the appropriate response from the server.  No actual file transfer takes place.   

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {

	private DatagramPacket sendPacket, receivePacket, sendDataPacket;
	private DatagramSocket sendReceiveSocket;
	//start with fis = null, set it later
	private FileInputStream fis = null;
	private byte readWriteCode = 00;
	private int receivePort;
	
	// we can run in normal (send directly to server) or test
	// (send to simulator) mode
	public static enum Mode {
		NORMAL, TEST
	};

	public Client() {
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send and receive UDP Datagram packets.
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) { // Can't create the socket.
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void sendAndReceive(byte RWCode) {
		readWriteCode = RWCode;
		
		byte[] msg, // message we send
				fn, // filename as an array of bytes
				md, // mode as an array of bytes
				data; // reply as array of bytes
		String filename, mode; // filename and mode as Strings
		int j, len, sendPort;
		
		// test vs. normal will be entered by the user.
		Mode run = Mode.TEST; // change to NORMAL to send directly to server

		if (run == Mode.NORMAL)
			sendPort = 69;
		else
			sendPort = 23;

		// Get User Input for filename
		Scanner inputFilename = new Scanner(System.in);
		System.out.print("Enter Filename: ");
		String filename1 = inputFilename.nextLine();

		//set up the currentPath string 
		Path currRelativePath = Paths.get("");
		String currPath = currRelativePath.toAbsolutePath().toString();
		//set up writeFilePath
		Path writeFilePath = Paths.get(currPath + "\\client", filename1);

		for (int i = 1; i <= 10; i++) {

			System.out.println("Client: creating packet " + i + ".");

			//get length of request
			int serverRequestLength = 4 + filename1.length() + mode.length();
			msg = new byte[serverRequestLength];
			
			// convert filename to bytes
			fn = filename1.getBytes();
			
			// and copy into the msg
			System.arraycopy(fn, 0, msg, 2, fn.length);
			// format is: source array, source index, dest array,
			// dest index, # array elements to copy
			// i.e. copy fn from 0 to fn.length to msg, starting at
			// index 2

			// now add a 0 byte
			msg[fn.length + 2] = 0;

			// now add "octet" (or "netascii")
			mode = "octet";
			// convert to bytes
			md = mode.getBytes();

			// and copy into the msg
			System.arraycopy(md, 0, msg, fn.length + 3, md.length);

			len = fn.length + md.length + 4; // length of the message
			// length of filename + length of mode + opcode (2) + two 0s (2)
			// second 0 to be added next:

			// end with another 0 byte
			msg[len - 1] = 0;

			// Construct a datagram packet that is to be sent to a specified port
			// on a specified host.
			// The arguments are:
			// msg - the message contained in the packet (the byte array)
			// the length we care about - k+1
			// InetAddress.getLocalHost() - the Internet address of the
			// destination host.
			// In this example, we want the destination to be the same as
			// the source (i.e., we want to run the client and server on the
			// same computer). InetAddress.getLocalHost() returns the Internet
			// address of the local host.
			// 69 - the destination port number on the destination host.
			try {
				sendPacket = new DatagramPacket(msg, len, InetAddress.getLocalHost(), sendPort);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(1);
			}

			System.out.println("Client: sending packet.");
			System.out.println("To host: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			len = sendPacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			for (j = 0; j < len; j++) {
				System.out.println("byte " + j + " " + msg[j]);
			}

			// Form a String from the byte array, and print the string.
			String sending = new String(msg, 0, len);
			System.out.println(sending);

			// Send the datagram packet to the server via the send/receive socket.

			try {
				sendReceiveSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			System.out.println("Client: Packet sent.");
			
			
			
			//if its a read request, write out the file 
			if(readWriteCode == 1) {
				//TODO implement this
				
			}
			//if its a write request
			if(readWriteCode == 2) {
			try {
				// Block until a datagram is received via sendReceiveSocket.
				sendReceiveSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			} else {
				//if the readWriteCode doesn't equal 1 or 2, quit (invalid)
				System.out.println("INVALID FILE REQUEST (not write or read)");
			}

			// Process the received datagram.
			System.out.println("Client: Packet received:");
			System.out.println("From host: " + receivePacket.getAddress());
			System.out.println("Host port: " + receivePacket.getPort());
			len = receivePacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			for (j = 0; j < len; j++) {
				System.out.println("byte " + j + " " + data[j]);
			}

			System.out.println();

		} // end of loop

		// We're finished, so close the socket.
		sendReceiveSocket.close();
	}

	public static void printOptions()
	{
		System.out.println("	Send: Send a file to Server.");
		System.out.println("	Quit: Stops the client.");
	}
	
	public static void main(String args[]) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Welcome Client!");
		
		while (true) {
			// Get User Input for filename
			System.out.print("Enter Command ('read', 'write', or 'quit'): ");
			String command = scanner.nextLine();
			
			if (command.equalsIgnoreCase("read")) {
				//pass in 1 as the Read/Write code
				Client c = new Client();
				c.sendAndReceive((byte) 1);
			}else if (command.equalsIgnoreCase("write")) {
				//pass in 2 as the Read/Write code
				Client c = new Client();
				c.sendAndReceive((byte) 2);
			}
			else if (command.equalsIgnoreCase("quit")) {
				System.out.println("Client shutting down...");
				scanner.close();
				return;
			} else {
				System.out.println("Invalid command. Please try again.");
				printOptions();
			}
		}
	 }
}
