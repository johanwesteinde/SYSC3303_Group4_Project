import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class ClientConnectionThread extends Thread {
	private byte[] packetData = new byte[512];
	private byte[] respPacket = new byte[512];//make 512 bytes for read responses (508 bytes data max)
	
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendSocketAndRecieve;

	// types of requests we can receive
	public static enum Request {
		READ, WRITE, ERROR
	};
//	
	//types of responses we can have
	public static enum Response {
		ACKRESPONSE, DATARESPONSE
	};

	// responses for valid requests
	//public static final byte[] dataResp = { 0, 3, 0, 1 };//WRONG, needs to be 512 bytes to attach data
//	public static final byte[] ackResp = { 0, 4, 0, 0 };
	
	Request req; // READ, WRITE or ERROR
	Response resp;
	String filename, mode;
	int len, j = 0, k = 0;
	private int blockNum = 0;

	public ClientConnectionThread(DatagramPacket packet) {
		// only WRQ and RRQ packets are passed through the constructor
		//use the constructor to set the Request type
		receivePacket = packet;
		packetData = receivePacket.getData();
		printReceivePacketInformation();
		//set the Request type and response type
		handleWRQorRRQ(packetData);
		
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

	public void handleWRQorRRQ(byte[] data) {
		//this method takes in the very first packet (RRQ or WRQ) and sets the Request type
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
			resp = Response.DATARESPONSE;
		} else if (req == Request.WRITE) { // for Write it's 0400
			resp = Response.ACKRESPONSE;
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
	}
	
	public void setNewPacket(DatagramPacket packet) {
		//this method is called when the server thread gets a packet that isn't a WRQ or RRQ
		//so the WRQ or RRQ is passed in the constructor, then the remainder of DATA or ACK
		//packets are sent through here
		receivePacket = packet;
		run();
	}
	
	public void printReceivePacketInformation() {
		// Process the received datagram.
		System.out.println("Server: Packet received:");
		System.out.println("From host: " + receivePacket.getAddress());
		System.out.println("Host port: " + receivePacket.getPort());
		len = receivePacket.getLength();
		System.out.println("Length: " + len);
		System.out.println("Containing: ");

		// print the bytes
		System.out.println(Arrays.toString(packetData));
		
		// Form a String from the byte array.
		String received = new String(packetData, 0, len);
		System.out.println(received);
	}
			
	public void sendResponsePacket() {//should we pass in an integer? If =0, send first block, if =1
		//if the received packet is WRQ , send back an ACK
		if(receivePacket.getData()[0] == 0 && receivePacket.getData()[1] == 2) {
			respPacket = new byte[] { 0, 4, 0, 0 };
			
		} 
		//if the received packet is RRQ, send back data block 1
		else if (receivePacket.getData()[0] == 0 && receivePacket.getData()[1] == 1) {
			respPacket = new byte[512];
			//first four bytes should be 0301 (DATA = 03, block # = 01)
			respPacket[0] = 0;
			respPacket[1] = 3;
			respPacket[2] = 0;
			respPacket[3] = 1;
			//TODO we need to get the data from the file name requested
		}
		//
//		else if()
			
			
			
			
			sendPacket = new DatagramPacket(respPacket, respPacket.length, receivePacket.getAddress(),
					receivePacket.getPort());
	
			System.out.println("Server: Sending packet:");
			System.out.println("To host: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			len = sendPacket.getLength();
			System.out.println("Length: " + len);
			System.out.println("Containing: ");
			for (j = 0; j < len; j++) {
				System.out.println("byte " + j + " " + respPacket[j]);
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
		}



	public void run() {
		//thread should stay open until it gets a data packet under 512 bytes (if write)
		//or sends a data packet under 512 bytes (if read)
		packetData = receivePacket.getData();
		byte[] dataCopy = new byte[packetData.length];
		
		//if we're handling a RRQ
		if(resp == Response.DATARESPONSE) {
			//if its a RRQ, create the data packet to send back and 
			//close thread if the data packet is under 512 bytes
			//otherwise continue scanning for new packets
		}
		else if(resp == Response.ACKRESPONSE) {
			if(packetData.length < 512) {
//				handlePacket();
				//SEND MEETHOD HERE
				// We're finished with this socket, so close it.
				sendSocketAndRecieve.close();
				
			}
			else {
				//SEND METHOD HERE
//				handlePacket();
				
			}
		}
	}
}
