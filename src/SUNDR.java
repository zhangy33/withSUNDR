import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

// Serialize the object to byte array or vice versa
import java.io.IOException;
import java.io.Serializable;


public class SUNDR implements Serializable, NewProtocol  {

	private static final long serialVersionUID = -4078257594186693768L;


	// define the class for ClientDataBase
	public class ClientDataBase implements Serializable {

		private static final long serialVersionUID = 8720439442926157434L;
		public int c; // client #
		public int cv; // value
		public byte[] VS; // VS
		public byte[] Sig; // Sig

		public ClientDataBase() {
		}
		
		public ClientDataBase(int in_c, int in_cv, byte[] in_VS, byte[] in_Sig) {
			this.c = in_c;
			this.cv = in_cv;
			this.VS = in_VS;
			this.Sig = in_Sig;
		}
	}

	// define the class for ServerDataBase
	public class ServerDataBase implements Serializable {

		private static final long serialVersionUID = -3574385112881198245L;
		public int cur_server_value; // current server value
		public List<byte[]> VSL; // VS list
		public List<byte[]> signedVSL; // sig list

		public ServerDataBase() {
		}
		
		public ServerDataBase(int in_cur_server_value, List<byte[]> in_VSL, List<byte[]> in_signedVSL) {
			this.cur_server_value = in_cur_server_value;
			this.VSL = in_VSL;
			this.signedVSL = in_signedVSL;
		}
	}
	
	// define the class for VS
	public class VSstruct implements Serializable{


		
		private static final long serialVersionUID = 1208091541999970479L;
		String type = "VRS"; // type is usually VRS
		byte[] cvArr; // hash value of cv
		int c; // client number
		int[] pairs; // version pairs
		
		 // in_c: incoming client #
		 // in_cv: incoming value
		 // in_CLIENT_TTNUM: incoming total client number		
		public VSstruct (int in_c, int in_cv, int in_CLIENT_TTNUM) {
			try {
				// cvByte
				byte[] cvByte = new byte[4]; 
				for (int i = 0; i < 4; ++i) {
					cvByte[i] = (byte) (in_cv >>> (i * 8));
				}
				MessageDigest md = MessageDigest.getInstance("MD5");
				this.cvArr = md.digest(cvByte);
				// c
				this.c = in_c;
				// pairs
				pairs = new int[2*in_CLIENT_TTNUM];
				for (int i = 0; i < in_CLIENT_TTNUM; ++i) {
					pairs[2*i] = i; // client #
					pairs[2*i + 1] = 0; // corresponding version #
				}
			} catch (NoSuchAlgorithmException e) {
				System.out.println("VSstruct constructor error!");
				e.printStackTrace();
			}
		}
		
	}
	

	// define the class for packSub
	public class PackSubClass implements Serializable {

		private static final long serialVersionUID = 3776778181626364672L;
		private int c; // client #
		private String o; // operation w or r
		int cv; // current value

		// constructor and functions to return the fields
		public PackSubClass(int in_c, String in_o, int in_cv) {
			this.c = in_c;
			this.o = in_o;
			this.cv = in_cv;
		}
	}
	
	
	
	@Override
	public byte[] initializeClientDataBase(int client_num, int CLIENT_TTNUM) {

		ClientDataBase clientDataBase = new ClientDataBase();

		// init data base:
		// client #
		clientDataBase.c = client_num;
		// value
		clientDataBase.cv = 0;
		// VS
		
		VSstruct localVSstruc = new VSstruct(client_num, 0, CLIENT_TTNUM);
		byte[] localVSstrucByte = SDK.serialize(localVSstruc);
		
		// // testing
		//String test = "testing";
		//byte[] localVSstrucByte = test.getBytes();
		clientDataBase.VS = localVSstrucByte;
		// sig
		clientDataBase.Sig = localVSstrucByte;// !!!!!!! to be continue
		
												 
		return SDK.serialize(clientDataBase);
		
	}

	@Override
	public byte[] initializeServerDataBase(int CLIENT_TTNUM){
		
		ServerDataBase serverDataBase = new ServerDataBase();
		
		// allocate memory for the data base
		serverDataBase.cur_server_value = 0;
		serverDataBase.VSL = new ArrayList<byte[]>(CLIENT_TTNUM);
		serverDataBase.signedVSL = new ArrayList<byte[]>(CLIENT_TTNUM);
		
		
		// the initial values are all zeros
		for (int i = 0; i < CLIENT_TTNUM; ++i) {
			
			
			VSstruct localVSstruc = new VSstruct(i, 0, CLIENT_TTNUM);
			byte[] localVSstrucByte = SDK.serialize(localVSstruc);
			
			// // testing
			// String test = "testing";
			// byte[] localVSstrucByte = test.getBytes();

			serverDataBase.VSL.add(i, localVSstrucByte);
			serverDataBase.signedVSL.add(i, localVSstrucByte); // !!!!!!! to be continue
		}
				
	
		return SDK.serialize(serverDataBase);
		
	}
	
	
	@Override
	public byte[] makeRequest(int c, String o, int cv) {

		PackSubClass packSubClass = new PackSubClass(c, o, cv);
		
		System.out.println("Protocal log: Request was made.");
		return SDK.serialize(packSubClass);

	}

	@Override
	public byte[] processRequest(byte[] Req, byte[] serverDataBase){
		
		try {
			// de-serialize the request
			PackSubClass packSubClass = (PackSubClass) SDK.deserialize( Req );
			
			// return the server data base
			System.out.println("Protocal log: Request was processed. Server data base size: " + serverDataBase.length);
			return serverDataBase;
			
			
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		
		return null;
			
	}

	@SuppressWarnings("resource")
	@Override
	public byte[] makeUpdate(int c, int cv, String o, byte[] clientDataBase, byte[] serverDataBase, PrivateKey priKey, List<PublicKey> pubKeys) {
			
		
		try {
			// de-serialize the client data base
			ClientDataBase clientDataBase_local;
			clientDataBase_local = (ClientDataBase) SDK.deserialize(clientDataBase);
			
			
			// de-serialize the server data base
			ServerDataBase serverDataBase_local;
			serverDataBase_local = (ServerDataBase) SDK.deserialize(serverDataBase);
			
			
			// VS from client == VS from server?
			
			VSstruct clientVSstruc_local = (VSstruct) SDK.deserialize(clientDataBase_local.VS);
			VSstruct serverVSstruc_local_curClient = (VSstruct) SDK.deserialize(serverDataBase_local.VSL.get(c));
			int badPairs =0;
			for(int i=0;i<20;++i)
			{
				if(clientVSstruc_local.pairs[i] != serverVSstruc_local_curClient.pairs[i])
					badPairs = 1;
			}
			if ( clientVSstruc_local.c != serverVSstruc_local_curClient.c ||
					badPairs == 1 ||
					!clientVSstruc_local.type.equals(serverVSstruc_local_curClient.type)) {
				System.out.println("ERROR in makeUpdate: VS from client != VS from server");
				return null;
			}
			
			
			// current client's data base is empty?
			boolean notEmpty = true;
			if ( (clientVSstruc_local.pairs[2*c + 1] == 0) ) {
				notEmpty = false;
				if (o=="r") {
					System.out.println("ERROR in makeUpdate: cannot read from empty database");
					return null;
				}
			}

			
			// if not empty: verify the sig and order
			int maxVersionNum = 0;
			if( notEmpty == true ){
				
				Signature sig = Signature.getInstance("SHA1WithRSA");
				
				// iterate all the clients
				for (int i = 0; i<serverDataBase_local.VSL.size() ; ++i) {
					sig.initVerify(pubKeys.get(i)); // get the key
					VSstruct curClientVSstruc_local = (VSstruct) SDK.deserialize( serverDataBase_local.VSL.get(i) ); // get the cur VS 
					
					// verify all the sig
					if ( curClientVSstruc_local.pairs[2*i + 1] > 0 ) { // if the current client is not empty
						sig.update(serverDataBase_local.VSL.get(i)); // the current VS
						if (sig.verify(serverDataBase_local.signedVSL.get(i)) == false) { // the current signedVS
							return null;
						}
					}
					
					// test ordered or not?
					if ( curClientVSstruc_local.pairs[2*c + 1] > maxVersionNum ) {
						maxVersionNum = curClientVSstruc_local.pairs[2*c + 1];
					}
				}
				
				// ordered or not?
				if (maxVersionNum > clientVSstruc_local.pairs[2*c + 1]) {
					System.out.println("ERROR in makeUpdate: VS is not ordered");
					return null;
				}
				
			}
			
			
			int newValue; // the value source can be from the server or the input
			if (o == "r") { // read
				newValue = serverDataBase_local.cur_server_value; 
			}
			else if (o == "w") { // write
				newValue = cv;
			}
			else { // neither r nor w
				System.out.println("ERROR: input is neither r nor w.");
				return null;
			}

			// update the version number
			VSstruct newVS = (VSstruct) SDK
					.deserialize(serverDataBase_local.VSL.get(c));
			++newVS.pairs[2 * c + 1];
			// update the hash value of cv in the client database
			byte[] cvByte = new byte[4];
			for (int i = 0; i < 4; ++i) {
				cvByte[i] = (byte) (newValue >>> (i * 8));
			}
			newVS.cvArr = cvByte;
			byte[] newVS_byte = SDK.serialize(newVS);
			// generate the new sig
			Signature sig2 = Signature.getInstance("SHA1WithRSA");
			sig2.initSign(priKey);
			sig2.update(newVS_byte);
			byte[] newSig_byte = sig2.sign();

			// generate the new client database and return
			ClientDataBase packUpdateServer = new ClientDataBase(c, newValue, newVS_byte, newSig_byte);
			System.out.println("Protocal log: Update r was made.");
			return SDK.serialize(packUpdateServer);

		} catch (ClassNotFoundException | IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidKeyException e) {
			System.out.println("ERROR in makeUpdate: incalid key" );
			e.printStackTrace();
			return null;
		} catch (SignatureException e) {
			System.out.println("ERROR in makeUpdate: sig verification failed" );
			e.printStackTrace();
			return null;
		}
				
	}

	
	@Override
	public byte[] updateServer(String o, byte[] packUpdateServer, byte[] serverDataBase, List<PublicKey> pubKeys) {
		try {
			// de-serialize the server data base
			ServerDataBase serverDataBase_local= (ServerDataBase) SDK.deserialize(serverDataBase);

			// de-serialize the request
			ClientDataBase packUpdateServer_local = (ClientDataBase) SDK.deserialize(packUpdateServer);
			
			int client_n = packUpdateServer_local.c;
			
			// verify the sig
			Signature sig = Signature.getInstance("SHA1WithRSA");
			sig.initVerify(pubKeys.get(client_n)); // get the key
			VSstruct curClientVSstruc_local = (VSstruct) SDK.deserialize( packUpdateServer_local.VS ); // get the cur VS 
			if ( curClientVSstruc_local.pairs[2*client_n + 1] > 0 ) { // if the current client is not empty
				sig.update(packUpdateServer_local.VS); // the current VS
				if (sig.verify(packUpdateServer_local.Sig) == false) { // the current signedVS
					System.out.println("ERROR in updateServer: verification failed in client " + packUpdateServer_local.c);
					return null;
				}
			}
			
			// read or write?
			if(o == "w") { // update the value if this is a "w" operation
				serverDataBase_local.cur_server_value = packUpdateServer_local.cv;
			}
			
			// update local server data base's VSL
			serverDataBase_local.VSL.set(client_n, packUpdateServer_local.VS);

			// update local server data base's sig
			serverDataBase_local.signedVSL.set(client_n, packUpdateServer_local.Sig);

			// return the local server data base
			System.out.println("Protocal log: Server was updated.");
			return SDK.serialize(serverDataBase_local);
				
		} catch (ClassNotFoundException | IOException | NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
