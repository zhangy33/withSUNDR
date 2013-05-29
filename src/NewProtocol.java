import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

/**
 * @author Yuanyuan Zhang
 * @version 1.0
 */

public interface NewProtocol {
	
	
	/**
	 * initialize
	 * @param client_num  client number
	 * @param CLIENT_TTNUM  total number of clients
	 */
	byte[] initializeClientDataBase(int client_num, int CLIENT_TTNUM);
	
	
	/**
	 * initialize
	 * @param CLIENT_TTNUM  total number of clients
	 */
	public byte[] initializeServerDataBase(int CLIENT_TTNUM);
	
	
	/**
	 * client sends a request to the server.
	 * @param c		client number
	 * @param o		type of operation
	 * @param cv	current value
	 */
	public byte[] makeRequest(int c, String o, int cv);
	
	
	/**
	 * server processes the request and replies to the client.
	 * @param cv	current value
	 */
	public byte[] processRequest(byte[] Req, byte[] serverDataBase);
	
	
	/**
	 * client verifies the data, sends data to the server and updates information if valid.
	 * @param cv	current value
	 * @throws SignatureException 
	 * @throws NoSuchAlgorithmException 
	 */
	public byte[] makeUpdate(int c, int cv, String o, byte[] clientDataBase, byte[] serverDataBase, PrivateKey priKey, List<PublicKey> pubKeys);
	
	
	/**
	 * server updates the information.
	 * @param cv	current value
	 */
	public byte[] updateServer(String o, byte[] packUpdateServer, byte[] serverDataBase, List<PublicKey> pubKeys);

}
