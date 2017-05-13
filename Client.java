package streamsAndFiles;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
	public static final String LOCALHOST = "127.0.0.1";
	public static final int port = 10888;

	public static void main(String[] args) {
		try {
			Socket clientSocket = new Socket(LOCALHOST, port);
			ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());
			String input;
			Scanner in = new Scanner(System.in);
			System.out.println("Enter your command");

			while (!(input = in.nextLine()).isEmpty()) {
				String inputStr = input;
				String[] arr = inputStr.split(" ");
				if (arr[0].equals("dir")) {
					outToServer.writeObject("dir");
					outToServer.flush();
					readDirFromServer(clientSocket, inFromServer);
				} else if (arr[0].equals("cd")) {
					if (arr[1].equals("..")) {
						outToServer.writeObject("cd ..");
						outToServer.flush();
						readBackToFromServer(clientSocket, inFromServer);
					} else {
						outToServer.writeObject(inputStr);
						outToServer.flush();
						readCdFromServer(clientSocket, inFromServer, arr[1]);
					}

				} else if (arr[0].equals("logout")) {
					outToServer.writeObject("logout");
					outToServer.flush();
					readLogOutFromServer(clientSocket, inFromServer);
				} else if (arr[0].equals("mkdir")) {
					outToServer.writeObject(inputStr);
					outToServer.flush();
					readMkdirFromServer(clientSocket, inFromServer, arr[1]);
				} else if (arr[0].equals("readFile")) {
					outToServer.writeObject(inputStr);
					outToServer.flush();
					readReadFileFromServer(clientSocket, inFromServer);
				} else if (arr[0].equals("download")) {
					System.out.println("Download from server");
					outToServer.writeObject(inputStr);
					outToServer.flush();
					readDownloadedFileFromServer(clientSocket, inFromServer);
				} else if (arr[0].equals("upload")) {
					System.out.println("Load into server");
					outToServer.writeObject(arr[0]);
					loadIntoServer(arr[1],outToServer);
					readResultFromServer(clientSocket, inFromServer);
				} 
				
				System.out.println("Enter your command");
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Client Error: " + e.getMessage());
			System.err.println("Localized: " + e.getLocalizedMessage());
			System.err.println("Stack Trace: " + e.getStackTrace());
		}
	}

	private static void loadIntoServer(String string, ObjectOutputStream outToServer) throws IOException {
		File file = new File(string);
		FileOutputStream fr = new FileOutputStream(file);
		BufferedOutputStream br = new BufferedOutputStream(fr);
		byte[] mybytearray = new byte[(int) file.length()];
		System.out.println("Reading the file to byte array");
		br.write(mybytearray, 0, mybytearray.length);
		outToServer.writeObject(mybytearray.length );
		outToServer.flush();
		System.out.println("Writing the file name");
		outToServer.writeObject(file.getName());
		outToServer.flush();
		System.out.println("Writing the byte array to server");
		outToServer.writeObject(mybytearray);
		outToServer.flush();
		fr.close();
		br.close();
		return;
	}

	private static void readResultFromServer(Socket clientSocket, ObjectInputStream inFromServer) throws ClassNotFoundException, IOException {
		Object obj = inFromServer.readObject();
		String rez = (String) obj;
		if (rez.equals("ok")) {
			System.out.println("The file has successfully loaded");
		} else {
			System.out.println("The file has not been found");
		}
		
	}

	private static void readDownloadedFileFromServer(Socket clientSocket, ObjectInputStream inFromServer)
			throws ClassNotFoundException, IOException {
		Object obj = inFromServer.readObject();
		String rez = (String) obj;
		if (rez.equals("ok")) {
			obj = inFromServer.readObject();
			Integer length = ((Integer) obj).intValue();
			String filename = (String) inFromServer.readObject();
			File file = new File("/home/tamara/localDownloads/"+filename);
			obj = inFromServer.readObject();
			byte[] arr = (byte[]) obj;
			if (arr.length == length) {
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(arr);
				fos.close();
			} else {
				System.out.println("Input is not correct");
				return;
			}

		} else {
			System.out.println("The file has not been found");
		}
	}

	@SuppressWarnings("unchecked")
	private static void readReadFileFromServer(Socket clientSocket, ObjectInputStream inFromServer)
			throws ClassNotFoundException, IOException {
		Object obj = inFromServer.readObject();
		String rez = (String) obj;
		if (rez.equals("ok")) {
			obj = inFromServer.readObject();
			Integer length = ((Integer) obj).intValue();
			obj = inFromServer.readObject();
			List<String> list = (List<String>) obj;
			if (list.size() == length) {
				for (String str : list) {
					System.out.println(str);
				}
			} else {
				System.out.println("Input is not correct");
				return;
			}

		} else {
			System.out.println("The file has not been found");
		}
	}

	private static void readMkdirFromServer(Socket clientSocket, ObjectInputStream inFromServer, String dirName)
			throws ClassNotFoundException, IOException {
		Object obj = inFromServer.readObject();
		String rez = (String) obj;
		if (rez.equals("ok")) {
			System.out.println("OK. A new directory " + dirName + " has been created ");
		} else {
			System.out.println("Error");
		}

	}

	private static void readBackToFromServer(Socket clientSocket, ObjectInputStream inFromServer)
			throws ClassNotFoundException, IOException {
		Object obj = inFromServer.readObject();
		String rez = (String) obj;
		if (rez.equals("ok")) {
			System.out.println("OK. Directory has been changed:.. ");
		} else {
			System.out.println("Error");
		}
	}

	private static void readLogOutFromServer(Socket clientSocket, ObjectInputStream inFromServer)
			throws IOException, ClassNotFoundException {
		Object obj = inFromServer.readObject();
		String rez = (String) obj;
		if (rez.equals("ok")) {
			System.out.println("OK. Logout is done");
		} else {
			System.out.println("Error. Logout cannot be done");
		}
	}

	private static void readCdFromServer(Socket clientSocket, ObjectInputStream inFromServer, String cd)
			throws IOException, ClassNotFoundException {
		Object obj = inFromServer.readObject();
		String rez = (String) obj;
		if (rez.equals("ok")) {
			System.out.println("OK. Directory has been changed to: " + cd);
		} else {
			System.out.println("Error");
		}
	}

	private static void readDirFromServer(Socket clientSocket, ObjectInputStream inFromServer)
			throws IOException, ClassNotFoundException {
		Object obj = inFromServer.readObject();
		String[] arr;
		Integer length = ((Integer) obj).intValue();
		arr = new String[length];

		for (String str : arr) {
			obj = inFromServer.readObject();
			if (obj instanceof String) {
				str = (String) obj;
				System.out.println(str);
			} else {
				System.out.println("Input from server is not a string");
			}
		}
	}

}
