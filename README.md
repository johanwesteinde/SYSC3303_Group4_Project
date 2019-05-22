# SYSC3303_Group4_Project

Adam Labelle 101038735
Johan Westeinde 101055222
Dominic Kocjan 100980801
Lyndon Lo 101030526

Iteration 1: Due May 21st, 2019

Client class: this is where the user will choose what data to read/write. The user will be prompted to enter what they want to do (read, write, or quit). If they choose read or write, they will be prompted to enter a file name, then the data transfer will take place. If the user chooses quit, the client will shut down.

ErrorSimulator class: this class currently just passes on information, either from the client to the server or vice-versa.

Server class: this class waits for requests to come into port 69 and if/when they do, it creates new ClientConnectionThread objects to handle the requests.

ClientConnectionThread class: this class handles the requests that come into the server on port 69


To run this code, first launch the Server class, then the ErrorSimulator class, then the Client class. The client class will be prompted to enter either 'read', 'write', or 'quit'. If read or write is chosen, the user will be prompted to enter the name of the file they want to read/write. From here, the data transfer occurs.


Requirements: Shutdown - Client and Server
             Pass Threads through Error Simulator
Latest Push: May 21st

Server Operator must start the server, for client to run
  - Server: Command Line: Start
  - Client: Command Line: Send, test.txt

 Client can quit
 - Client: Command Line: quit //Closes Scanner

 Server Operator can quit
 - Server: Command Line: quit //will check if threads are finished, using system.exit(0) after.

Johan: Created ClientConnectionThread class, the sendAndReceive() method in the Client class, created the class diagram
Dominic: Worked on the server handling & client connection threads, use cases.
Adam: Handled the shut down
Lyndon: Created the scanners to prompt the user to enter the action they want to perform (read, write, quit) and the file name they wish to read/write to/from. Added multithreaded server.
