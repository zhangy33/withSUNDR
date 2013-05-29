import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.PrivateKey;

public class Client{

	// define this client
	private String host = "localhost";
	private int port = 7822;
	

	public int c;
	public int cv;
	public byte[] clientDataBase;
	public PrivateKey priv;
	public String o;

	
	// constructor
	public Client(int in_c, int in_cv, PrivateKey in_priv) {
		// will use the default host and port
		
		c = in_c;
		cv = in_cv;

		this.priv = in_priv;
		
		// init the client data base
		clientDataBase = ClientCo.initClientDataBase(in_c);

	}


	public void chat() {
		
		try {
		
		
			ClientCo.client_total_writeTime_withSocket = System.nanoTime();
			ClientCo.client_active_writeTime_withSocket = System.nanoTime();
			// ***** initialize the in the out sockets *****
			Socket socket = new Socket(host, port); // initialize the socket
			DataOutputStream out = new DataOutputStream(
					socket.getOutputStream());
			out.flush(); // to ensure that the header is sent
			DataInputStream in = new DataInputStream(socket.getInputStream());

			
			
			ClientCo.client_total_writeTime_withoutSocket = System.nanoTime();
			ClientCo.client_active_writeTime_withoutSocket = System.nanoTime();
			// ***** submit the request *****
			this.o = ClientCo.cur_o;
			out.writeUTF(this.o); // send the operation type
			out.flush(); // force any buffered byte to be written out the the stream
			
			// get the request package
			byte[] packSub_byte = ClientCo.getRequestPackage();

			// send the package
			out.writeInt(packSub_byte.length);
			out.flush();
			out.write(packSub_byte);
			out.flush();
			
			
			ClientCo.client_active_writeTime_withSocket = System.nanoTime() - ClientCo.client_active_writeTime_withSocket;
			ClientCo.client_active_writeTime_withoutSocket = System.nanoTime() - ClientCo.client_active_writeTime_withoutSocket;
			ClientCo.client_total_readTime_withSocket = System.nanoTime();
			ClientCo.client_total_readTime_withoutSocket = System.nanoTime();
			// ***** receive the reply package and make update*****
			// receive the reply (processed) package
			byte[] packRep_byte = new byte[0];
			byte[] buff = new byte[SDK.INBUFF];
			int k = -1;
			int flag = 1;
		    while((k = in.read(buff, 0, buff.length)) > -1) {
		    	if (flag == 1) {
		    		ClientCo.client_total_writeTime_withSocket = System.nanoTime() - ClientCo.client_total_writeTime_withSocket;
		    		ClientCo.client_total_writeTime_withoutSocket = System.nanoTime() - ClientCo.client_total_writeTime_withoutSocket;
		    		ClientCo.client_active_readTime_withSocket = System.nanoTime(); 
		    		ClientCo.client_active_readTime_withoutSocket = System.nanoTime();
		    		flag = 0;
		    	}
		        byte[] tbuff = new byte[packRep_byte.length + k]; // temp buffer size = bytes already read + bytes last read
		        System.arraycopy(packRep_byte, 0, tbuff, 0, packRep_byte.length); // copy previous bytes
		        System.arraycopy(buff, 0, tbuff, packRep_byte.length, k);  // copy current lot
		        packRep_byte = tbuff; // call the temp buffer as the result buff
		    }
		    //System.out.println("2. Client read reply: " + packRep_byte.length + " byte");
		    
		    // SHUT DOWN SOCKET INPUT
		    socket.shutdownInput();
		     
		    
			// make update
			byte[] packUpd_byte = ClientCo.makeUpdatePackage(c, cv, this.o,
					clientDataBase, packRep_byte, this.priv);
			// update client
			clientDataBase = packUpd_byte;
			// send the updates to server
			out.write(packUpd_byte);
			out.flush();
			
			
			//System.out.println("3. Client sent updates: " + packUpd_byte.length + " byte");
			
			
			// SHUT DOWN SOCKET OUTPUT
			socket.shutdownOutput();
			
			
			// ***** done *****
			//System.out.println();

			if (!socket.isClosed()) {
				ClientCo.client_total_readTime_withoutSocket = System.nanoTime() - ClientCo.client_total_readTime_withoutSocket;
				ClientCo.client_active_readTime_withoutSocket = System.nanoTime() - ClientCo.client_active_readTime_withoutSocket;
				in.close();
				out.close();
				socket.close();
			}
			
			ClientCo.client_total_readTime_withSocket = System.nanoTime() - ClientCo.client_total_readTime_withSocket;
			ClientCo.client_active_readTime_withSocket = System.nanoTime() - ClientCo.client_active_readTime_withSocket;
			

			
			// ***** receive info *****
			ClientCo.client_total_send_msgSize = packSub_byte.length + packUpd_byte.length;
			ClientCo.client_total_received_msgSize = packRep_byte.length;
			ClientCo.client_total_msgSize = packSub_byte.length + packRep_byte.length + packUpd_byte.length;
			
			
			Socket socket_testInfo = new Socket(host, port); // initialize the socket
			DataInputStream in_testInfo = new DataInputStream(socket_testInfo.getInputStream());
			
			ClientCo.server_total_writeTime_withSocket = in_testInfo.readLong();
			ClientCo.server_total_writeTime_withoutSocket = in_testInfo.readLong();
			ClientCo.server_active_writeTime_withSocket = in_testInfo.readLong();
			ClientCo.server_active_writeTime_withoutSocket = in_testInfo.readLong();
			ClientCo.server_total_readTime_withSocket = in_testInfo.readLong();
			ClientCo.server_total_readTime_withoutSocket = in_testInfo.readLong();
			ClientCo.server_active_readTime_withSocket = in_testInfo.readLong();
			ClientCo.server_active_readTime_withoutSocket = in_testInfo.readLong();
			
			socket_testInfo.shutdownInput();
			in_testInfo.close();
			
			if(!socket_testInfo.isClosed()) {
				socket_testInfo.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	} // end of chat()

} // end of current class