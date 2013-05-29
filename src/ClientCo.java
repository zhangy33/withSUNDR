import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClientCo {
	
	// ***** should read from the config.txt
	// define client numbers
	public static int minClients = 0;		// minimum number of clients
	public static int maxClients = 0;		// maximum number of clients
	public static int incClients = 0;		// increment of clients
				
	// define operation numbers
	public static int writesNum = 0;		// number of write updates
	public static int readsNum = 0;		// number of read updates
	public static int runsNum = 0;		// number of runs
	
	public static ArrayList<String> protocols = new ArrayList<String>();	//which protocols to run
	
	// the current protocol name
	public static String curProtocolName = "null";
	// ***** should read from the config.txt			
	
	
	// current operation
	public static String cur_o = "w";
	
	// current client number
	public static int curClientNum = 0;
	
	// keys
	public static int KEYLEN = 1024;
	public static List<PublicKey> pubKeys; // public keys
	public static List<PrivateKey> privKeys; // private keys
	
	// define this client
	private static String hostCo = "localhost";
	private static int portCo = 4700;
	
	// test info collection
	// time info
	public static long client_total_writeTime_withSocket;
	public static long client_total_writeTime_withoutSocket;
	public static long client_active_writeTime_withSocket;
	public static long client_active_writeTime_withoutSocket;
	public static long client_total_readTime_withSocket;
	public static long client_total_readTime_withoutSocket;
	public static long client_active_readTime_withSocket;
	public static long client_active_readTime_withoutSocket;
	public static long server_total_writeTime_withSocket;
	public static long server_total_writeTime_withoutSocket;
	public static long server_active_writeTime_withSocket;
	public static long server_active_writeTime_withoutSocket;
	public static long server_total_readTime_withSocket;
	public static long server_total_readTime_withoutSocket;
	public static long server_active_readTime_withSocket;
	public static long server_active_readTime_withoutSocket;
	// size info
	public static long client_total_send_msgSize;
	public static long client_total_received_msgSize;
	public static long client_total_msgSize;
	// buffer
	public static class testInfoEachRunClientStruct {
		public String client_protocolName = "null";
		public int client_clientNum = 0;
		
		public long client_total_writeTime_withSocket[][];
		public long client_total_writeTime_withoutSocket[][];
		public long client_active_writeTime_withSocket[][];
		public long client_active_writeTime_withoutSocket[][];
		public long client_total_readTime_withSocket[][];
		public long client_total_readTime_withoutSocket[][];
		public long client_active_readTime_withSocket[][];
		public long client_active_readTime_withoutSocket[][];
		
		public long server_total_writeTime_withSocket[][];
		public long server_total_writeTime_withoutSocket[][];
		public long server_active_writeTime_withSocket[][];
		public long server_active_writeTime_withoutSocket[][];
		public long server_total_readTime_withSocket[][];
		public long server_total_readTime_withoutSocket[][];
		public long server_active_readTime_withSocket[][];
		public long server_active_readTime_withoutSocket[][];
		
		public long client_total_writeSend_msgSize[][];
		public long client_total_writeReceived_msgSize[][];
		public long client_total_writeMsgSize[][];
		public long client_total_readSend_msgSize[][];
		public long client_total_readReceived_msgSize[][];
		public long client_total_readMsgSize[][];
		
		public testInfoEachRunClientStruct(int in_clientNum, int in_writeNum, int in_readNum) {
			this.client_total_writeTime_withSocket = new long[in_clientNum][in_writeNum];
			this.client_total_writeTime_withoutSocket = new long[in_clientNum][in_writeNum];
			this.client_active_writeTime_withSocket = new long[in_clientNum][in_writeNum];
			this.client_active_writeTime_withoutSocket = new long[in_clientNum][in_writeNum];
			this.client_total_readTime_withSocket = new long[in_clientNum][in_readNum];
			this.client_total_readTime_withoutSocket = new long[in_clientNum][in_readNum];
			this.client_active_readTime_withSocket = new long[in_clientNum][in_readNum];
			this.client_active_readTime_withoutSocket = new long[in_clientNum][in_readNum];
			
			this.server_total_writeTime_withSocket = new long[in_clientNum][in_writeNum];
			this.server_total_writeTime_withoutSocket = new long[in_clientNum][in_writeNum];
			this.server_active_writeTime_withSocket = new long[in_clientNum][in_writeNum];
			this.server_active_writeTime_withoutSocket = new long[in_clientNum][in_writeNum];
			this.server_total_readTime_withSocket = new long[in_clientNum][in_readNum];
			this.server_total_readTime_withoutSocket = new long[in_clientNum][in_readNum];
			this.server_active_readTime_withSocket = new long[in_clientNum][in_readNum];
			this.server_active_readTime_withoutSocket = new long[in_clientNum][in_readNum];
			
			this.client_total_writeSend_msgSize = new long[in_clientNum][in_writeNum];
			this.client_total_writeReceived_msgSize = new long[in_clientNum][in_writeNum];
			this.client_total_writeMsgSize = new long[in_clientNum][in_writeNum];
			this.client_total_readSend_msgSize = new long[in_clientNum][in_readNum];
			this.client_total_readReceived_msgSize = new long[in_clientNum][in_readNum];
			this.client_total_readMsgSize = new long[in_clientNum][in_readNum];

		}
	}
	public static ArrayList<testInfoEachRunClientStruct> testInfoEachRunClient;
	
	

	
	public static void main(String[] args) throws IOException {

		//String configFilePathName = "E:\\DownloadsE\\eclipse\\workspace\\yuanyuan\\config.txt"; // input the filePath
		//String outputFileRoot = "E:\\DownloadsE\\eclipse\\workspace\\yuanyuan\\";
		String configFilePathName = args[0];
		String outputFileRoot = args[1];
		
		readConfigFile(configFilePathName);
		
		try {

			privKeys = new ArrayList<PrivateKey>(maxClients);
			pubKeys = new ArrayList<PublicKey>(maxClients);

			for (int i = 0; i < maxClients; ++i) {

				// generate the key pair for current client
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
				keyGen.initialize(KEYLEN);
				KeyPair key = keyGen.generateKeyPair();
				PrivateKey priv = key.getPrivate();
				PublicKey pub = key.getPublic();

				privKeys.add(priv);
				pubKeys.add(pub);

				System.out.println("Preparing client #" + i);
				System.out.println("Current public key length: "
						+ pub.toString().length());
			}

			// communicate with ServerCo 
			Socket socketCo = new Socket(hostCo, portCo);
			DataOutputStream outCo = new DataOutputStream(
					socketCo.getOutputStream());
			
			
			// 1st: send the test parameters
			outCo.writeInt(minClients);
			outCo.flush();
			outCo.writeInt(maxClients);
			outCo.flush();
			outCo.writeInt(incClients); 
			outCo.flush();
			outCo.writeInt(writesNum); 
			outCo.flush();
			outCo.writeInt(readsNum);
			outCo.flush();
			outCo.writeInt(runsNum);
			outCo.flush();
			outCo.writeInt(protocols.size());
			outCo.flush();
			for (int i = 0; i<protocols.size(); ++i) {
				outCo.writeUTF(protocols.get(i)); // send out the protocol name
				outCo.flush();
				System.out.println("Protocol name: " + protocols.get(i));
			}
			
			
			// 2nd: communicate with ServerCo to send the public keys
			byte[] pubKeys_byte = SDK.serialize(pubKeys);
			outCo.write(pubKeys_byte);
			outCo.flush();

			
			// 3rd: CLOSE THE SOCKET
			socketCo.shutdownOutput();
			socketCo.close();
			outCo.close();

			
			int runNumTT = protocols.size() * ((maxClients-minClients) / incClients + 1);
			testInfoEachRunClient = new ArrayList<testInfoEachRunClientStruct>(runNumTT);
			
			for (int l = 0; l < protocols.size(); ++l) 
			{
				curProtocolName = protocols.get(l); // get the current protocal name
				
				
				for (int k = minClients; k <= maxClients; k = k + incClients) // client
																				// #
																				// loop
				{
					curClientNum = k;
					for (int j = 0; j < runsNum; ++j) // run # loop (server
														// needs to be reseted
														// after this)
					{
						// test info
						testInfoEachRunClientStruct temp_testInfoEachRunStruct = new testInfoEachRunClientStruct(k, writesNum, readsNum);
						temp_testInfoEachRunStruct.client_clientNum = k;
						temp_testInfoEachRunStruct.client_protocolName = curProtocolName;
						
						// init multiple clients and public keys
						List<Client> clients = new ArrayList<Client>(
								curClientNum);
						for (int i = 0; i < maxClients; ++i) {
							Client client = new Client(i, 1, privKeys.get(i));
							clients.add(client);
						}

						// write # loop
						for (int i = 0; i < writesNum; ++i) {
							Random randomGenerator = new Random();
							for (int ii = 0; ii < k; ++ii) { // for each client

								// update the current operation type
								cur_o = "w";

								// start the communication
								clients.get(ii).cv = randomGenerator
										.nextInt(100); // the value to be
														// written
								clients.get(ii).chat();
								
								// test info
								temp_testInfoEachRunStruct.client_active_writeTime_withoutSocket[ii][i] = client_active_writeTime_withoutSocket;
								temp_testInfoEachRunStruct.client_active_writeTime_withSocket[ii][i] = client_active_writeTime_withSocket;
								temp_testInfoEachRunStruct.client_total_writeTime_withoutSocket[ii][i] = client_total_writeTime_withoutSocket;
								temp_testInfoEachRunStruct.client_total_writeTime_withSocket[ii][i] = client_total_writeTime_withSocket;
								temp_testInfoEachRunStruct.server_active_writeTime_withoutSocket[ii][i] = server_active_writeTime_withoutSocket;
								temp_testInfoEachRunStruct.server_active_writeTime_withSocket[ii][i] = server_active_writeTime_withSocket;
								temp_testInfoEachRunStruct.server_total_writeTime_withoutSocket[ii][i] = server_total_writeTime_withoutSocket;
								temp_testInfoEachRunStruct.server_total_writeTime_withSocket[ii][i] = server_total_writeTime_withSocket;
								temp_testInfoEachRunStruct.client_total_writeSend_msgSize[ii][i] = client_total_send_msgSize;
								temp_testInfoEachRunStruct.client_total_writeReceived_msgSize[ii][i] = client_total_received_msgSize;
								temp_testInfoEachRunStruct.client_total_writeMsgSize[ii][i] = client_total_msgSize;
								
								// // testing message
								//System.out.println("test:");
								//System.out.println(temp_testInfoEachRunStruct.client_active_writeTime_withoutSocket[ii][i]);
								//System.out.println(temp_testInfoEachRunStruct.server_active_writeTime_withSocket[ii][i]);
								//System.out.println("Press Any Key To Continue...");
						        //new java.util.Scanner(System.in).nextLine();
								
								
							}
						} // end of write # loop

						
						
						// read # loop
						for (int i = 0; i < readsNum; ++i) {
							for (int ii = 0; ii < k; ++ii) { // for each client

								// update the current operation type
								cur_o = "r";

								// start the communication
								clients.get(ii).chat();
								
								// test info
								temp_testInfoEachRunStruct.client_active_readTime_withoutSocket[ii][i] = client_active_readTime_withoutSocket;
								temp_testInfoEachRunStruct.client_active_readTime_withSocket[ii][i] = client_active_readTime_withSocket;
								temp_testInfoEachRunStruct.client_total_readTime_withoutSocket[ii][i] = client_total_readTime_withoutSocket;
								temp_testInfoEachRunStruct.client_total_readTime_withSocket[ii][i] = client_total_readTime_withSocket;
								temp_testInfoEachRunStruct.server_active_readTime_withoutSocket[ii][i] = server_active_readTime_withoutSocket;
								temp_testInfoEachRunStruct.server_active_readTime_withSocket[ii][i] = server_active_readTime_withSocket;
								temp_testInfoEachRunStruct.server_total_readTime_withoutSocket[ii][i] = server_total_readTime_withoutSocket;
								temp_testInfoEachRunStruct.server_total_readTime_withSocket[ii][i] = server_total_readTime_withSocket;
								temp_testInfoEachRunStruct.client_total_readSend_msgSize[ii][i] = client_total_send_msgSize;
								temp_testInfoEachRunStruct.client_total_readReceived_msgSize[ii][i] = client_total_received_msgSize;
								temp_testInfoEachRunStruct.client_total_readMsgSize[ii][i] = client_total_msgSize;
							}
						} // end of read # loop
	
						testInfoEachRunClient.add(temp_testInfoEachRunStruct);
						
					} // end of run # loop
				} // end of client # loop

			}// end of protocol # loop
			
			
			// write to the file
			writeTestResults(testInfoEachRunClient, outputFileRoot);
			
			
			
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		
	} // end of main()
	
	
	
	// in: testing results structure, output file root
	public static void writeTestResults(ArrayList<testInfoEachRunClientStruct> testInfo, String root) {
		// write the testing result into files
		
		try {
			System.out.println("Start writing the rslt to files...");
			File dir;
			File file;
			FileWriter fw;
			for (int l = 0; l < protocols.size(); ++l) {
				
				String dirProtocolName = protocols.get(l) + "\\";
				String root_dirProtocolName = root + dirProtocolName;
				dir = new File(root_dirProtocolName);
				dir.mkdirs(); // create protocol folder

				
				// the 16 time test info folders + 6 size test info folders:
				String root_dirProtocolName_timeType;
				
				root_dirProtocolName_timeType = root_dirProtocolName
						+ "client_total_writeTime_withSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).client_total_writeTime_withSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "client_total_writeTime_withoutSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).client_total_writeTime_withoutSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "client_active_writeTime_withSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).client_active_writeTime_withSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "client_active_writeTime_withoutSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).client_active_writeTime_withoutSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "client_total_readTime_withSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).client_total_readTime_withSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "client_total_readTime_withoutSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).client_total_readTime_withoutSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "client_active_readTime_withSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).client_active_readTime_withSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "client_active_readTime_withoutSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).client_active_readTime_withoutSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "server_total_writeTime_withSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).server_total_writeTime_withSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "server_total_writeTime_withoutSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).server_total_writeTime_withoutSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "server_active_writeTime_withSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).server_active_writeTime_withSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "server_active_writeTime_withoutSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).server_active_writeTime_withoutSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "server_total_readTime_withSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).server_total_readTime_withSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "server_total_readTime_withoutSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).server_total_readTime_withoutSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "server_active_readTime_withSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).server_active_readTime_withSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient

				root_dirProtocolName_timeType = root_dirProtocolName
						+ "server_active_readTime_withoutSocket\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).server_active_readTime_withoutSocket[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient
				
				
				root_dirProtocolName_timeType = root_dirProtocolName
						+ "client_total_writeSend_msgSize\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).client_total_writeSend_msgSize[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient
				
				root_dirProtocolName_timeType = root_dirProtocolName
						+ "client_total_writeReceived_msgSize\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).client_total_writeReceived_msgSize[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient
				
				root_dirProtocolName_timeType = root_dirProtocolName
						+ "client_total_writeMsgSize\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).client_total_writeMsgSize[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient
				
				root_dirProtocolName_timeType = root_dirProtocolName
						+ "client_total_readSend_msgSize\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).client_total_readSend_msgSize[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient
				
				root_dirProtocolName_timeType = root_dirProtocolName
						+ "client_total_readReceived_msgSize\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).client_total_readReceived_msgSize[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient
				
				root_dirProtocolName_timeType = root_dirProtocolName
						+ "client_total_readMsgSize\\";
				dir = new File(root_dirProtocolName_timeType);
				dir.mkdirs();
				for (int k = minClients; k <= maxClients; k = k + incClients) {
					String root_dirProtocolName_timeType_cNum = root_dirProtocolName_timeType
							+ "clientNum" + String.valueOf(k) + "\\";
					dir = new File(root_dirProtocolName_timeType_cNum);
					dir.mkdirs();

					file = new File(dir, "rslt.csv"); // create the file

					fw = new FileWriter(file); // open the file
					
					int cnt = 0;
					for (int j = 0; j<testInfo.size(); ++j)
					{
						if ((testInfo.get(j).client_protocolName==protocols.get(l)) && (testInfo.get(j).client_clientNum==k)) {
							++cnt;
							fw.write("run#" + String.valueOf(cnt) + ":,");
							for (int i = 0; i<k; ++i)
							{
								for (int ii=0; ii<writesNum; ++ii)
								{
									fw.write(String.valueOf(testInfo.get(j).client_total_readMsgSize[i][ii]) + ",");
								} // end of for writeNum
							} // end of for clientNum k
							fw.write(System.getProperty("line.separator")); // new line
						} // end of if
					} // end of testInfo itor
					
					fw.close();
				} // end of minClient : maxClient
				

				System.out.println("File writing done.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void readConfigFile(String configFilePathName) {
		// Read in configuration file data here
		// Check if file exists
		// Try to read in all data
		
		// locate the file
		//String configFilePathName = "E:\\DownloadsE\\eclipse\\workspace\\Simulator\\src\\config.txt"; // input the filePath
		File file = new File(configFilePathName);
		try {
			
			// open file
			if ( file.isFile() ) {
				
				System.out.println( "Config file open succeed." );
				
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				
				String line = null;
				// read the config values
				line = br.readLine(); // locate the pos
				//this.serverAdd = line.substring(line.indexOf(": ") + 2, line.length()); // read the value
				line = br.readLine();
				minClients = Integer.valueOf(line.substring(line.indexOf(": ") + 2,
						line.length()));
				line = br.readLine();
				maxClients = Integer.valueOf(line.substring(line.indexOf(": ") + 2,
						line.length()));
				line = br.readLine();
				incClients = Integer.valueOf(line.substring(line.indexOf(": ") + 2,
						line.length()));
				line = br.readLine();
				writesNum = Integer.valueOf(line.substring(line.indexOf(": ") + 2,
						line.length()));
				line = br.readLine();
				readsNum = Integer.valueOf(line.substring(line.indexOf(": ") + 2,
						line.length()));
				line = br.readLine();
				runsNum = Integer.valueOf(line.substring(line.indexOf(": ") + 2,
						line.length()));
				line = br.readLine(); // multiple protocols may apply
				String protocols_temp = line.substring(line.indexOf(": ") + 2, line.length());
				while ( protocols_temp.indexOf(",") > -1 ) {
					protocols.add( protocols_temp.substring(0, protocols_temp.indexOf(",")) ) ;
					protocols_temp = protocols_temp.substring(protocols_temp.indexOf(",") + 1,
							protocols_temp.length());
				}
				protocols.add( protocols_temp.substring(0, protocols_temp.length()) ) ;
				
				/*
				// testing: output to console
				System.out.println(serverAdd + "\n" + minClients + "\n"
						+ maxClients + "\n" + incClients + "\n" + writesNum + "\n"
						+ readsNum + "\n" + runsNum + "\n" + protocols);
				*/
				
				// close file
				br.close();
				fr.close();
			}
			else {
				System.out.println( "Config file open failed." );
			} // end of "if ( file.isFile() )"
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}

	}
	

	/* ***** The followings are the static functions get from the protocol. They will be called by Client. ***** */
	
	// init the server data base
	public static byte[] initClientDataBase(int client_n) {
		// load the protocol at runtime
		Class<?> cls_initializeClientDataBas;
		try {
			// load the protocol at run time
			cls_initializeClientDataBas = Class.forName(curProtocolName);
			Object obj_initializeClientDataBas = cls_initializeClientDataBas
					.newInstance();

			// get the initializeServerDataBas method at runtime
			Method method = cls_initializeClientDataBas.getDeclaredMethod(
					"initializeClientDataBase", int.class, int.class);

			// call the clientDataBas method
			byte[] clientDataBase = (byte[]) method.invoke(
					obj_initializeClientDataBas, client_n, curClientNum);

			return clientDataBase;

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
	
	
	public static byte[] getRequestPackage() {
		
		try {
			// load the protocol at runtime
			Class<?> cls_makeRequest = Class.forName(curProtocolName);
			Object obj_makeRequest = cls_makeRequest.newInstance();
			
			// define the input parameters: int c, String o in the protocol
			@SuppressWarnings("rawtypes")
			Class[] param_makeRequest = {int.class,String.class,int.class};
			
			// get the makeRequest method at runtime
			Method method = cls_makeRequest.getDeclaredMethod("makeRequest", param_makeRequest );
			
			// call the makeRequest method
			byte[] RequestPackage = (byte[]) method.invoke(obj_makeRequest, 1, "w", 6);
			
			return RequestPackage;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}
	
	public static byte[] makeUpdatePackage(int c, int cv, String o, byte[] clientDataBase, byte[] serverDataBase, PrivateKey priKey) {
		
		try {
			// load the protocol at runtime
			Class<?> cls_makeUpdate = Class.forName(curProtocolName);
			Object obj_makeUpdate = cls_makeUpdate.newInstance();
			
			// define the input parameters: int c, int cv, byte[] serverDataBase
			@SuppressWarnings("rawtypes")
			Class[] param_makeUpdate = {int.class,int.class,String.class,byte[].class,byte[].class,PrivateKey.class,List.class};
			
			// get the makeUpdate method at runtime
			Method method = cls_makeUpdate.getDeclaredMethod("makeUpdate", param_makeUpdate );
			
			// call the makeUpdate method
			byte[] UpdatePackage = (byte[]) method.invoke(obj_makeUpdate, c, cv, o, clientDataBase, serverDataBase, priKey, pubKeys);
			
			return UpdatePackage;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}
	

}
