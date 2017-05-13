package streamsAndFiles;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	// private static final Logger logger = Logger.getLogger("Server");
	public ServerSocket serverSock;
	public final static int DEFAULT_PORT = 10888;

//	static File f = new File("/");

	public static void main(String[] args) {

		try (ServerSocket severSock = new ServerSocket(DEFAULT_PORT)) {
			System.out.println("Server set to port " + DEFAULT_PORT);			
			boolean serverEnded = false;
			while (!serverEnded) {
				Socket client = severSock.accept();
				TreadTest test = new TreadTest(client);
				System.out.println("Thread created");
				test.start();
			}
			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
