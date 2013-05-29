import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Server{
	

	// communication field
	private int port = 7822;
    private ServerSocket serverSocket;
    private ExecutorService executorService; // thread pool
	private Lock lock = new ReentrantLock(); // to prevent deadlock
	
	// data base field
	private byte[] serverDataBase;
	
	public static long server_total_writeTime_withSocket;
	public static long server_total_writeTime_withoutSocket;
	public static long server_active_writeTime_withSocket;
	public static long server_active_writeTime_withoutSocket;
	public static long server_total_readTime_withSocket;
	public static long server_total_readTime_withoutSocket;
	public static long server_active_readTime_withSocket;
	public static long server_active_readTime_withoutSocket;
	

	// constructor
	public Server() {}

	
	// start the service
	public void startService() throws IOException {
		
		int connectCount = 0;

		this.serverSocket = new ServerSocket(port);
		executorService = Executors.newSingleThreadExecutor();
		System.out.println("Server started up.");

		
		// Let ServerCo update the client number, protocol name,and pubKey
		ServerCo.getClientNumProtocolNameAndPubKey();

		for (int l = 0; l < ServerCo.ProtocolsNum; ++l) 
		{
			ServerCo.curProtocolName = ServerCo.protocols.get(l);
			for (int k = ServerCo.minClients; k <= ServerCo.maxClients; k = k + ServerCo.incClients) // client # loop
			{
				ServerCo.curClientNum = k;
				for (int j = 0; j < ServerCo.runsNum; ++j) // run # loop
				{
					// initialize the server
					serverDataBase = ServerCo.initServerDataBase();

					// operation # = client # * ( write # + read # )
					int ops = ServerCo.curClientNum
							* (ServerCo.writesNum + ServerCo.readsNum);

					while (ops-- > 0) {

						try {

							// .accept() will be activated when a client trying
							// to
							// connect this server
							Socket socket = this.serverSocket.accept();

							connectCount++; // connection counter
							System.out.println(connectCount
									+ " of connections.");

							// 1. connect the current client to a thread(input
							// socket
							// and i)
							// 2. input the server's data base
							// 3. output the new server's data base for updating
							Future<byte[]> futureServerDataBase = executorService
									.submit(new Handler(socket, serverDataBase));
							byte[] newServerDataBase = (byte[]) futureServerDataBase
									.get();

							// update the server data base
							serverDataBase = newServerDataBase;
							
							if (!socket.isClosed()) {
								socket.close();
							}
							
							
							// send out the test info
							Socket socket_testInfo = this.serverSocket.accept();
							DataOutputStream out_testInfo = new DataOutputStream(socket_testInfo.getOutputStream());
							out_testInfo.flush();
							
							out_testInfo.writeLong(server_total_writeTime_withSocket);
							out_testInfo.flush();
							out_testInfo.writeLong(server_total_writeTime_withoutSocket);
							out_testInfo.flush();
							out_testInfo.writeLong(server_active_writeTime_withSocket);
							out_testInfo.flush();
							out_testInfo.writeLong(server_active_writeTime_withoutSocket);
							out_testInfo.flush();
							out_testInfo.writeLong(server_total_readTime_withSocket);
							out_testInfo.flush();
							out_testInfo.writeLong(server_total_readTime_withoutSocket);
							out_testInfo.flush();
							out_testInfo.writeLong(server_active_readTime_withSocket);
							out_testInfo.flush();
							out_testInfo.writeLong(server_active_readTime_withoutSocket);
							out_testInfo.flush();
							
							socket_testInfo.shutdownOutput();
							out_testInfo.close();
							
							if (!socket_testInfo.isClosed()) {
								socket_testInfo.close();
							}

							System.out.println();

						} catch (IOException | InterruptedException
								| ExecutionException e) {
							e.printStackTrace();
						}

					} // end of while
				} // end of run # loop
			} // end of client # loop
		} // end of protocol # loop


		
		// close the server
		executorService.shutdownNow();
		this.serverSocket.close();

	}

	
	
	// substitute the server data base
	
	
	// implement the Callable interface
	public class Handler implements Callable<byte[]> {
		
		// define the thread fields
		// client info
		private Socket socket; // socket
		// local server data base copy
		private byte[] serverDataBase_ThreadCopy;
		
		public String o;
		
		
		// constructor of the current thread
		public Handler(Socket socket, byte[] serverDataBase) {
			
			// copy client info to local
			this.socket = socket;
			
			// copy server data base to local
			serverDataBase_ThreadCopy = serverDataBase;
			
		}

		// override the call() method
		@Override
		public byte[] call() {
			lock.lock();
			try {
				
				
				// time info		
				server_total_readTime_withSocket = System.nanoTime();
				server_active_readTime_withSocket = System.nanoTime();
				// ***** initialize the in the out sockets *****
				// get input and output streams
				DataOutputStream out = new DataOutputStream(
						socket.getOutputStream());
				out.flush(); // to ensure that the header is sent
				DataInputStream in = new DataInputStream(
						socket.getInputStream());
				
				
				server_total_readTime_withoutSocket = System.nanoTime();
				server_active_readTime_withoutSocket = System.nanoTime();
				// ***** reply the request *****
				this.o = in.readUTF(); // read the current operation
				
				// receive the package
				int sizePackReq = in.readInt(); // receive request package size
				byte[] packReceived_byte = new byte[sizePackReq];
				in.read(packReceived_byte); // get the request package
				
				// process and reply the request
				byte[] packReply_byte = ServerCo.replyRequest(packReceived_byte, serverDataBase_ThreadCopy);
				out.write(packReply_byte);
				out.flush(); // force any buffered byte to be written out the the stream
				server_active_readTime_withSocket = server_active_readTime_withSocket - System.nanoTime();
				server_active_readTime_withoutSocket = server_active_readTime_withoutSocket - System.nanoTime();
				//System.out.println("1. Server sent reply: " + packReply_byte.length + " byte");
				
				// SHUT DOWN SOCKET OUTPUT !!!KEY WORD TO AVOID DEAD LOCK!!!
				// otherwise, the client will keep waiting for the reply message
				socket.shutdownOutput();
				
				
				server_total_writeTime_withSocket = System.nanoTime();
				server_total_writeTime_withoutSocket = System.nanoTime();
				// ***** receive the updating package and update server *****
				// receive the update package
				byte[] packUpd_byte = new byte[0];
				byte[] buff = new byte[SDK.INBUFF];
				int k = -1;
				int flag = 1;
			    while((k = in.read(buff, 0, buff.length)) > -1) {
			    	if (flag == 1) {
			    		server_total_readTime_withSocket = server_total_readTime_withSocket - System.nanoTime();
			    		server_total_readTime_withoutSocket = server_total_readTime_withoutSocket - System.nanoTime();
			    		server_active_writeTime_withSocket = System.nanoTime();
			    		server_active_writeTime_withoutSocket = System.nanoTime();
			    		flag = 0;
			    	}
			        byte[] tbuff = new byte[packUpd_byte.length + k]; // temp buffer size = bytes already read + bytes last read
			        System.arraycopy(packUpd_byte, 0, tbuff, 0, packUpd_byte.length); // copy previous bytes
			        System.arraycopy(buff, 0, tbuff, packUpd_byte.length, k);  // copy current lot
			        packUpd_byte = tbuff; // call the temp buffer as the result buff
			    }
			    //System.out.println("4. Server read updates: " + packUpd_byte.length + " byte");
			    
			    // // SHUT DOWN SOCKET inPUT
			    socket.shutdownInput();
			    
				// update the server data base
				byte[] newServerDataBase = ServerCo.updateServerDataBase(this.o, packUpd_byte, serverDataBase_ThreadCopy);
				 
				
				
				// ***** done *****
				System.out.println();

				
				if (!socket.isClosed()) {
					server_total_writeTime_withoutSocket = System.nanoTime() - server_total_writeTime_withoutSocket;
					server_active_writeTime_withoutSocket = System.nanoTime() - server_active_writeTime_withoutSocket;
					in.close();
					out.close();
					socket.close();
				}
				server_total_writeTime_withSocket = System.nanoTime() - server_total_writeTime_withSocket;
				server_active_writeTime_withSocket = System.nanoTime() - server_active_writeTime_withSocket;
				
		
				
				return newServerDataBase;

			} catch (IOException e) {
				e.printStackTrace();
			}
			lock.unlock();
			
			return null;
		}
		
	}
	 
} // end of this class file