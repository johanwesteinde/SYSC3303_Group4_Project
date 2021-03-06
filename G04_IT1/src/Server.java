
// TFTPServer.java
// This class is the server side of a simple TFTP server based on
// UDP/IP. The server receives a read or write packet from a client and
// sends back the appropriate response without any actual file transfer.
// One socket (69) is used to receive (it stays open) and another for each response. 

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

	// types of requests we can receive
	public static enum Request {
		READ, WRITE, ERROR
	};

	// responses for valid requests
	public static final byte[] readResp = { 0, 3, 0, 1 };
	public static final byte[] writeResp = { 0, 4, 0, 0 };

	// Variable used for stopping the server
	public static int threadCount;

	// UDP datagram packets and sockets used to send / receive
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket receiveSocket, sendSocket;

	public Server() {
		try {
			// Construct a datagram socket and bind it to port 69
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets.
			receiveSocket = new DatagramSocket(69);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	private class SendReceiveThread extends Thread {
		DatagramPacket receivePacket;
		byte[] data = new byte[100];
		byte[] response = new byte[4];
		Request req; // READ, WRITE or ERROR
		String filename, mode;
		int len, j = 0, k = 0;

		public SendReceiveThread(DatagramPacket packet) {
			receivePacket = packet;
		}

		public void run() {
			data = receivePacket.getData();

			// Process the received datagram.
			System.out.println("Server: Packet received:");
			System.out.println("From host: " + receivePacket.getAddress());
			System.out.println("Host port: " + receivePacket.getPort());
			len = receivePacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");

			// print the bytes
			System.out.println(Arrays.toString(data));
			/*
			 * for (j = 0; j < len; j++) { System.out.println("byte " + j + " " + data[j]);
			 * }
			 */
			// Form a String from the byte array.
			String received = new String(data, 0, len);
			System.out.println(received);

			// If it's a read, send back DATA (03) block 1
			// If it's a write, send back ACK (04) block 0
			// Otherwise, ignore it
			if (data[0] != 0)
				req = Request.ERROR; // bad
			else if (data[1] == 1)
				req = Request.READ; // could be read
			else if (data[1] == 2)
				req = Request.WRITE; // could be write
			else
				req = Request.ERROR; // bad

			if (req != Request.ERROR) { // check for filename
				// search for next all 0 byte
				for (j = 2; j < len; j++) {
					if (data[j] == 0)
						break;
				}
				if (j == len)
					req = Request.ERROR; // didn't find a 0 byte
				if (j == 2)
					req = Request.ERROR; // filename is 0 bytes long
				// otherwise, extract filename
				filename = new String(data, 2, j - 2);
			}

			if (req != Request.ERROR) { // check for mode
				// search for next all 0 byte
				for (k = j + 1; k < len; k++) {
					if (data[k] == 0)
						break;
				}
				if (k == len)
					req = Request.ERROR; // didn't find a 0 byte
				if (k == j + 1)
					req = Request.ERROR; // mode is 0 bytes long
				mode = new String(data, j, k - j - 1);
			}

			if (k != len - 1)
				req = Request.ERROR; // other stuff at end of packet

			// Create a response.
			if (req == Request.READ) { // for Read it's 0301
				response = readResp;
			} else if (req == Request.WRITE) { // for Write it's 0400
				response = writeResp;
			} else { // it was invalid, close socket on port 69 (so things work properly next time)
						// and quit
				receiveSocket.close();
				try {
					throw new Exception("Not yet implemented");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			sendPacket = new DatagramPacket(response, response.length, receivePacket.getAddress(),
					receivePacket.getPort());

			System.out.println("Server: Sending packet:");
			System.out.println("To host: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			len = sendPacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			for (j = 0; j < len; j++) {
				System.out.println("byte " + j + " " + response[j]);
			}

			// Send the datagram packet to the client via a new socket.

			try {
				// Construct a new datagram socket and bind it to any port
				// on the local host machine. This socket will be used to
				// send UDP Datagram packets.
				sendSocket = new DatagramSocket();
			} catch (SocketException se) {
				se.printStackTrace();
				System.exit(1);
			}

			try {
				sendSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			System.out.println("Server: packet sent using port " + sendSocket.getLocalPort());
			System.out.println();

			// We're finished with this socket, so close it.
			decrementThreadCount();
			sendSocket.close();
			}
	}

	public void receiveAndSendTFTP() throws Exception {
		byte[] data, response = new byte[4];

		Request req; // READ, WRITE or ERROR

		String filename, mode;
		int len, j = 0, k = 0;

		for (;;) { // loop forever
			// Construct a DatagramPacket for receiving packets up
			// to 100 bytes long (the length of the byte array).

			data = new byte[100];
			receivePacket = new DatagramPacket(data, data.length);

			System.out.println("Server: Waiting for packet.");
			// Block until a datagram packet is received from receiveSocket.
			try {
				receiveSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			SendReceiveThread newThread = new SendReceiveThread(receivePacket);
			newThread.start();
			incrementThreadCount();
			
			/*
			 * // Process the received datagram.
			 * System.out.println("Server: Packet received:");
			 * System.out.println("From host: " + receivePacket.getAddress());
			 * System.out.println("Host port: " + receivePacket.getPort()); len =
			 * receivePacket.getLength(); System.out.println("Length: " + len);
			 * System.out.println("Containing: ");
			 * 
			 * // print the bytes for (j = 0; j < len; j++) { System.out.println("byte " + j
			 * + " " + data[j]); }
			 * 
			 * // Form a String from the byte array. String received = new String(data, 0,
			 * len); System.out.println(received);
			 * 
			 * // If it's a read, send back DATA (03) block 1 // If it's a write, send back
			 * ACK (04) block 0 // Otherwise, ignore it if (data[0] != 0) req =
			 * Request.ERROR; // bad else if (data[1] == 1) req = Request.READ; // could be
			 * read else if (data[1] == 2) req = Request.WRITE; // could be write else req =
			 * Request.ERROR; // bad
			 * 
			 * if (req != Request.ERROR) { // check for filename // search for next all 0
			 * byte for (j = 2; j < len; j++) { if (data[j] == 0) break; } if (j == len) req
			 * = Request.ERROR; // didn't find a 0 byte if (j == 2) req = Request.ERROR; //
			 * filename is 0 bytes long // otherwise, extract filename filename = new
			 * String(data, 2, j - 2); }
			 * 
			 * if (req != Request.ERROR) { // check for mode // search for next all 0 byte
			 * for (k = j + 1; k < len; k++) { if (data[k] == 0) break; } if (k == len) req
			 * = Request.ERROR; // didn't find a 0 byte if (k == j + 1) req = Request.ERROR;
			 * // mode is 0 bytes long mode = new String(data, j, k - j - 1); }
			 * 
			 * if (k != len - 1) req = Request.ERROR; // other stuff at end of packet
			 * 
			 * // Create a response. if (req == Request.READ) { // for Read it's 0301
			 * response = readResp; } else if (req == Request.WRITE) { // for Write it's
			 * 0400 response = writeResp; } else { // it was invalid, close socket on port
			 * 69 (so things work properly next time) // and quit receiveSocket.close();
			 * throw new Exception("Not yet implemented"); }
			 * 
			 */

			// Construct a datagram packet that is to be sent to a specified port
			// on a specified host.
			// The arguments are:
			// data - the packet data (a byte array). This is the response.
			// receivePacket.getLength() - the length of the packet data.
			// This is the length of the msg we just created.
			// receivePacket.getAddress() - the Internet address of the
			// destination host. Since we want to send a packet back to the
			// client, we extract the address of the machine where the
			// client is running from the datagram that was sent to us by
			// the client.
			// receivePacket.getPort() - the destination port number on the
			// destination host where the client is running. The client
			// sends and receives datagrams through the same socket/port,
			// so we extract the port that the client used to send us the
			// datagram, and use that as the destination port for the TFTP
			// packet.

			/*
			 * 
			 * sendPacket = new DatagramPacket(response, response.length,
			 * receivePacket.getAddress(), receivePacket.getPort());
			 * 
			 * System.out.println("Server: Sending packet:"); System.out.println("To host: "
			 * + sendPacket.getAddress()); System.out.println("Destination host port: " +
			 * sendPacket.getPort()); len = sendPacket.getLength();
			 * System.out.println("Length: " + len); System.out.println("Containing: "); for
			 * (j = 0; j < len; j++) { System.out.println("byte " + j + " " + response[j]);
			 * }
			 * 
			 * // Send the datagram packet to the client via a new socket.
			 * 
			 * try { // Construct a new datagram socket and bind it to any port // on the
			 * local host machine. This socket will be used to // send UDP Datagram packets.
			 * sendSocket = new DatagramSocket(); } catch (SocketException se) {
			 * se.printStackTrace(); System.exit(1); }
			 * 
			 * try { sendSocket.send(sendPacket); } catch (IOException e) {
			 * e.printStackTrace(); System.exit(1); }
			 * 
			 * System.out.println("Server: packet sent using port " +
			 * sendSocket.getLocalPort()); System.out.println();
			 * 
			 * // We're finished with this socket, so close it. sendSocket.close();
			 * 
			 */
		} // end of loop
		
	}

	synchronized public void incrementThreadCount() {
		threadCount++;
	}

	synchronized public void decrementThreadCount() {
		threadCount--;
		if (threadCount <= 0) {
			notifyAll();
		}
	}

	synchronized public static int getThreadCount() {
		return threadCount;
	}

	public void shutdownServer() {
		System.out.println("Server is Stopping: Waiting for threads to finish");
		while (getThreadCount() > 0) {
			// Wait for threads to finish
			try {
				wait();
			} catch (InterruptedException e1) {
				System.out.println("Stopping was interrupted. Failed to stop properly.");
				System.exit(1);
			}
		}
		System.out.println("Finished.");
		System.exit(0);
	}

	public static void printOptions() {
		System.out.println("	Start: Start up the Server.");
		System.out.println("	Quit: Stops the Server.");
	}

	public static void main(String args[]) throws Exception {
		Server s = new Server();
		//s.receiveAndSendTFTP();
		
		Scanner scanner = new Scanner(System.in);
		
		while (true) {
			// Get User Input for filename
			System.out.println("Welcome Server Operator!");
			System.out.print("Enter Command: ");
			String command = scanner.nextLine();
			
			if (command.equalsIgnoreCase("Start")) {
				s.receiveAndSendTFTP();
			}else if (command.equalsIgnoreCase("quit")) {
				s.shutdownServer();
				scanner.close();
				return;
			} else {
				System.out.println("Invalid command. Please try again.");
				printOptions();
			}
		}
	 }
}

