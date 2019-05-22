// TFTPClient.java
// This class is the client side for a very simple assignment based on TFTP on
// UDP/IP. The client uses one port and sends a read or write request and gets 
// the appropriate response from the server.  No actual file transfer takes place.   

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {

   private DatagramPacket sendPacket, receivePacket;
   private DatagramSocket sendReceiveSocket;
   private byte readWriteCode = 00;
   private FileInputStream fis = null;
   
   // we can run in normal (send directly to server) or test
   // (send to simulator) mode
   public static enum Mode { NORMAL, TEST};

   public Client()
   {
      try {
         // Construct a datagram socket and bind it to any available
         // port on the local host machine. This socket will be used to
         // send and receive UDP Datagram packets.
         sendReceiveSocket = new DatagramSocket();
      } catch (SocketException se) {   // Can't create the socket.
         se.printStackTrace();
         System.exit(1);
      }
   }

   public void sendAndReceive(byte RWCode)
   {
	  readWriteCode = RWCode;
      byte[] msg = new byte[100], // message we send
             fn, // filename as an array of bytes
             md, // mode as an array of bytes
             data; // reply as array of bytes
      String filename, mode; // filename and mode as Strings
      int j, len, sendPort;
      
      // test vs. normal will be entered by the user.
      Mode run = Mode.TEST; // change to NORMAL to send directly to server
      
      //TODO make sure to change these back to port 23 and 69
      if (run==Mode.NORMAL) 
         sendPort = 69;
      else
         sendPort = 23;
      
      Scanner inputFilename = new Scanner(System.in);
      System.out.print("Enter Filename:");
      String filename1 = inputFilename.nextLine();

      // Prepare a DatagramPacket and send it via sendReceiveSocket
      // to sendPort on the destination host (also on this machine).
      msg[0] = 0;
      msg[1] = readWriteCode;
      // convert to bytes and copy into the msg
      fn = filename1.getBytes();
      System.arraycopy(fn,0,msg,2,fn.length);
      // format is: source array, source index, dest array,dest index, # array elements to copy
      // i.e. copy fn from 0 to fn.length to msg, starting at index 2
      
      // now add a 0 byte
      msg[fn.length+2] = 0;
      
      // now add "octet" (or "netascii")
      mode = "octet";
      // convert to bytes
      md = mode.getBytes();
      
      // and copy into the msg
      System.arraycopy(md,0,msg,fn.length+3,md.length);
      
      len = fn.length+md.length+4; // length of the message
      // length of filename + length of mode + opcode (2) + two 0s (2)
      // second 0 to be added next:

      // end with another 0 byte 
      msg[len-1] = 0;
      
      // Construct a datagram packet that is to be sent to a specified port on a specified host.
      // The arguments are:
      //  msg - the message contained in the packet (the byte array) 
      //  the length we care about - k+1
      //  InetAddress.getLocalHost() - the Internet address of the destination host.
      //     In this example, we want the destination to be the same as
      //     the source (i.e., we want to run the client and server on the
      //     same computer). InetAddress.getLocalHost() returns the Internet
      //     address of the local host.
      //  69 - the destination port number on the destination host.
      try {
         sendPacket = new DatagramPacket(msg, len,
                             InetAddress.getLocalHost(), sendPort);
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
      for (j=0;j<len;j++) {
          System.out.println("byte " + j + " " + msg[j]);
      }
      
      // Form a String from the byte array, and print the string.
      //Format: fileNameMode
      String sending = new String(msg,0,len);
      System.out.println(sending);

      // Send the datagram packet to the server via the send/receive socket.
      try {
         sendReceiveSocket.send(sendPacket);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }

      System.out.println("Client: Packet sent.");
      
      // Construct a DatagramPacket for receiving packets up
      // to 512 bytes long (the length of the byte array).
      data = new byte[512];
      receivePacket = new DatagramPacket(data, data.length);
      System.out.println("Client: Waiting for packet." + receivePacket);
      
      try {
          // Block until a datagram is received via sendReceiveSocket.
          sendReceiveSocket.receive(receivePacket);
       } catch(IOException e) {
          e.printStackTrace();
          System.exit(1);
       }
      
      // Process the received datagram.
      System.out.println("Client: Packet received:");
      System.out.println("From host: " + receivePacket.getAddress());
      System.out.println("Host port: " + receivePacket.getPort());
      len = receivePacket.getLength();
      System.out.println("Length: " + len);
      System.out.println("Containing: ");
      for (j=0;j<len;j++) {
          System.out.println("byte " + j + " " + data[j]);
      }
      
      System.out.println();
      
	  Path currRelativePath = Paths.get("");
	  String currPath = currRelativePath.toAbsolutePath().toString();
	  
      if(readWriteCode == 1) {
    	  //TODO implement the READ
    	  //create a file input stream in the server or client connection thread to pass data in
    	  
    	  System.out.print("RECEIVED DATA PACKET: " + receivePacket.toString());
    	  
      } else if (readWriteCode == 2) {
    	  //TODO later - verify we get the correct ACK back (0400)
//    	  Path filePath = Paths.get(currPath + "/client", filename1);
//    	  System.out.println("File path is: " + filePath.toString());
//    	  if(!Files.isReadable(filePath)) {
//    		  System.out.println("File is not readable");
//    		  System.exit(0);
//    	  }
			
    	  //set up file input stream using and ensure the file exists
    	  File file = new File(currPath + "/client/" + filename1);
          
    	  System.out.println("File is: " + file);
    	  try {
    		  BufferedReader br = new BufferedReader(new FileReader(file));
        	  String st;
        	  try {
				while((st = br.readLine()) != null){
					  System.out.println(st);
				  }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("IOException occurs here");
				e.printStackTrace();
			}
//				fis = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				System.out.println( filename1 + " not found at path " + file);
				System.exit(0);
			}
    	  System.out.println("ReceivePacket Data is: " + receivePacket.getData());
    	  
    	  
      } else {
    	  System.out.println("INVALID REQUEST (readWriteCode should be 1 or 2");
    	  System.exit(0);
      }
      
      // We're finished, so close the socket.
      sendReceiveSocket.close();
   }

   public static void printOptions()
	{
		System.out.println("	Read: Read file from the server");
		System.out.println("    Write: Write a file to the server");
		System.out.println("	Quit: Stops the client.");
	}
   
   public static void main(String args[])
   {
		Client c = new Client();
		Scanner scanner = new Scanner(System.in);
		System.out.println("Welcome Client!");
		
		while (true) {
			// Get User Input for filename
			System.out.print("Enter Command: ");
			String command = scanner.nextLine();
			
			if (command.toLowerCase().equalsIgnoreCase("read")) {
				c.sendAndReceive((byte)1);
			}else if (command.toLowerCase().equalsIgnoreCase("write")) {
				c.sendAndReceive((byte)2);
			}else if (command.toLowerCase().equalsIgnoreCase("quit")) {
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


