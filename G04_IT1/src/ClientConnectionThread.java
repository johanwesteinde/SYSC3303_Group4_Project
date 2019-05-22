import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class ClientConnectionThread extends Thread {
	byte[] data = new byte[100];
	byte[] response = new byte[4];
	
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendSocketAndRecieve;

	// types of requests we can receive
	public static enum Request {
		READ, WRITE, ERROR
	};

	// responses for valid requests
	public static final byte[] dataResp = { 0, 3, 0, 1 };
	public static final byte[] ackResp = { 0, 4, 0, 0 };
	
	Request req; // READ, WRITE or ERROR
	String filename, mode;
	int len, j = 0, k = 0;

	public ClientConnectionThread(DatagramPacket packet) {
		receivePacket = packet;
		try {
			// Construct a new datagram socket and bind it to any port
			// on the local host machine. This socket will be used to
			// send UDP Datagram packets.
			sendSocketAndRecieve = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void run() {
		//thread closes once done running this method
		
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
			response = dataResp;
		} else if (req == Request.WRITE) { // for Write it's 0400
			response = ackResp;
		} else { // it was invalid, close socket on port 69 (so things work properly next time)
					// and quit
			sendSocketAndRecieve.close();
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
			sendSocketAndRecieve.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Server: packet sent using port " + sendSocketAndRecieve.getLocalPort());
		System.out.println();

		// We're finished with this socket, so close it.
		sendSocketAndRecieve.close();
	}
}
