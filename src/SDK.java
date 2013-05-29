import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class SDK implements Serializable{
	
	public class testInfoEachRunServerStruct implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5055042726635718504L;
		public long server_total_writeTime_withSocket;
		public long server_total_writeTime_withoutSocket;
		public long server_active_writeTime_withSocket;
		public long server_active_writeTime_withoutSocket;
		public long server_total_readTime_withSocket;
		public long server_total_readTime_withoutSocket;
		public long server_active_readTime_withSocket;
		public long server_active_readTime_withoutSocket;
		
		public testInfoEachRunServerStruct(){}
	}
	
	private static final long serialVersionUID = 3501302915007689309L;
	// public static int CLIENT_NUM = 150; //!!!!!!only for testing, client# should not be in SDK
	
	public SDK () {};
	
	// input stream buffer size
	public final static int INBUFF = 1024 ; // 1 kb;
	
	// serialize: obj -> byte[]
	public static byte[] serialize(Object obj) {
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bo);
			oo.writeObject(obj);
			byte[] bytes = bo.toByteArray();
			bo.close();
			oo.close();
			return (bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// de-serialize: byte[] -> obj
    public static Object deserialize (byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();
    }

	
}
