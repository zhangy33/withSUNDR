import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;


public class ServerCo {
	
	// the current protocol name
	public static String curProtocolName = "null";
	// define client numbers
	public static int minClients = 0;		// minimum number of clients
	public static int maxClients = 0;		// maximum number of clients
	public static int incClients = 0;		// increment of clients
				
	// define operation numbers
	public static int writesNum = 0;		// number of write updates
	public static int readsNum = 0;		// number of read updates
	public static int runsNum = 0;		// number of runs
	public static int ProtocolsNum = 0;		// number of protocols
	
	public static ArrayList<String> protocols = new ArrayList<String>();	//which protocols to run
	
	// communicate with the ClientCo
	private static int portCo = 4700;
	
	// current client number
	public static int curClientNum = 0;
	
	// the current run is the last one?
	public static int LAST_RUN = 0;
	
	
	public static List<PublicKey> pubKey;
	

	public static void main(String[] args) {

		// conduct the Server
		Server server = new Server();
		try {
			server.startService();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/* ***** The followings are the static functions get from the protocol. They will be called by Server. ***** */
	
	// update the protocol name
	@SuppressWarnings("unchecked")
	public static void getClientNumProtocolNameAndPubKey() {
		
		
		try {
			// communicate with the Client
			ServerSocket serverSocketCo = new ServerSocket(portCo);
			Socket socketCo = serverSocketCo.accept();
			DataInputStream inCo = new DataInputStream(
					socketCo.getInputStream());
			
			// 1st: read the test parameters
			minClients = inCo.readInt();
			maxClients = inCo.readInt();
			incClients = inCo.readInt();
			writesNum = inCo.readInt();
			readsNum = inCo.readInt();
			runsNum = inCo.readInt();
			ProtocolsNum = inCo.readInt();
			for (int i = 0; i<ProtocolsNum; ++i) {
				String temp = inCo.readUTF(); // get the protocol name form the ClientCo
				protocols.add(temp);
				System.out.println("The protocolName got from the ClientCo is: "
						+ temp);
			}
			curProtocolName = protocols.get(0);

			
			// 2nd (part 1): receive the public keys
			byte[] pubKey_byte = new byte[0];
			byte[] buff = new byte[SDK.INBUFF];
			int k = -1;
		    while((k = inCo.read(buff, 0, buff.length)) > -1) {
		        byte[] tbuff = new byte[pubKey_byte.length + k]; // temp buffer size = bytes already read + bytes last read
		        System.arraycopy(pubKey_byte, 0, tbuff, 0, pubKey_byte.length); // copy previous bytes
		        System.arraycopy(buff, 0, tbuff, pubKey_byte.length, k);  // copy current lot
		        pubKey_byte = tbuff; // call the temp buffer as the result buff
		    }
		    
		    
		    // 3rd: SHUT DOWN SOCKET inPUT
		    socketCo.shutdownInput();
			socketCo.close();
			serverSocketCo.close();
			
			
			// 2nd (part 2): deserialize the public key
			pubKey = (List<PublicKey>) SDK.deserialize(pubKey_byte);
			//System.out.println("Receive the pubKey with length: "
			//		+ pubKey.get(0).toString().length() + " " + pubKey.get(1).toString().length());



		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	
	// init the server data base
	public static byte[] initServerDataBase() {
		// load the protocol at runtime
		Class<?> cls_initializeServerDataBas;
		try {
			// load the protocol at run time
			cls_initializeServerDataBas = Class.forName(curProtocolName);
			Object obj_initializeServerDataBas = cls_initializeServerDataBas
					.newInstance();

			// get the initializeServerDataBas method at runtime
			Method method = cls_initializeServerDataBas.getDeclaredMethod(
					"initializeServerDataBase", int.class);

			// call the initializeServerDataBas method
			byte[] serverDataBase = (byte[]) method.invoke(
					obj_initializeServerDataBas, curClientNum);

			return serverDataBase;

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;

	}

	public static byte[] replyRequest(byte[] packReceived_byte, byte[] serverDataBase) {

		// load the protocol at runtime
		Class<?> cls_processRequest;
		try {
			// load the protocol at run time
			cls_processRequest = Class.forName(curProtocolName);
			Object obj_processRequest = cls_processRequest.newInstance();

			// define the input parameters: byte[], SDK.ServerDataBase in the protocol
			@SuppressWarnings("rawtypes")
			Class[] param_processRequest = { byte[].class, byte[].class };

			// get the processRequest method at runtime
			Method method = cls_processRequest.getDeclaredMethod(
					"processRequest", param_processRequest);

			// call the processRequest method
			byte[] packReply_byte = (byte[]) method.invoke(obj_processRequest, packReceived_byte, serverDataBase);
			
		
			
			
			
			return packReply_byte;
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public static byte[] updateServerDataBase(String o, byte[] packUpdateServer, byte[] serverDataBase) {

		// load the protocol at runtime
		Class<?> cls_updateServerDataBase;
		try {
			// load the protocol at run time
			cls_updateServerDataBase = Class.forName(curProtocolName);
			Object obj_updateServerDataBase = cls_updateServerDataBase.newInstance();

			// define the input parameters: byte[], SDK.ServerDataBase in the protocol
			@SuppressWarnings("rawtypes")
			Class[] param_updateServerDataBase = { String.class, byte[].class, byte[].class, List.class };

			// get the processRequest method at runtime
			Method method = cls_updateServerDataBase.getDeclaredMethod(
					"updateServer", param_updateServerDataBase);

			// call the processRequest method
			byte[] newServerDataBase = (byte[]) method.invoke(obj_updateServerDataBase, o, packUpdateServer, serverDataBase, pubKey);

			
			return newServerDataBase;
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}


}
