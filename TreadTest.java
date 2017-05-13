package streamsAndFiles;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TreadTest extends Thread {

	private Socket client;
	private File file;

	public TreadTest(Socket socket) {
		this.client = socket;
		this.file = new File("/");
	}

	public void run() {
		System.out.println("Client from " + client.getRemoteSocketAddress() + " connected.");
		try {
			ObjectOutputStream outToClient = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream inFromClient = new ObjectInputStream(client.getInputStream());
			boolean clientEnded = false;
			while (!clientEnded) {
				Object obj = inFromClient.readObject();
				String str = (String) obj;
				String[] arrStr = str.split(" ");
				if (arrStr[0].equals("dir")) {
					System.out.println("Responding to Dir");
					redingDirFromInput(outToClient);
				} else if (arrStr[0].equals("cd")) {
					System.out.println("Responding to Cd");
					readCdFromInput(outToClient, arrStr[1]);
				} else if (arrStr[0].equals("mkdir")) {
					System.out.println("Responding to Mkdir");
					readMkdirFromInput(outToClient, arrStr);
				} else if (arrStr[0].equals("logout")) {
					System.out.println("Responding to Logout");
					readLogoutFromInput(outToClient, arrStr);
					clientEnded = true;
				} else if (arrStr[0].equals("readFile")) {
					System.out.println("Reading from file");
					readReadFileFromInput(outToClient, str);
				} else if (arrStr[0].equals("download")) {
					System.out.println("Download from server");
					readDownloadFileFromServer(outToClient, str);
				}else if (arrStr[0].equals("upload")) {
					System.out.println("Load from client");
					loadFileFromInput(outToClient, str, inFromClient);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("There was an exception, aborting client.");
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void loadFileFromInput(ObjectOutputStream outToClient, String str, ObjectInputStream inFromClient) throws ClassNotFoundException, IOException {
		if(str.equals("upload")){
			Object obj = inFromClient.readObject();
			Integer length = ((Integer) obj).intValue();
			String filename = (String) inFromClient.readObject();
			File file = new File("/home/tamara/localDownloads/"+filename);
			obj = inFromClient.readObject();
			byte[] arr = (byte[]) obj;
			if (arr.length == length) {
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(arr);
				fos.close();
			} else {
				System.out.println("Input is not correct");
				return;
			}
		}
		
	}

	private void readDownloadFileFromServer(ObjectOutputStream outToClient, String str) throws IOException {
		File[] arr = file.listFiles();
		String fileName = str.substring(9, str.length());
		System.out.println("Working with file");
		System.out.println("Writing into client");
		for (File aa : arr) {
			Path p = Paths.get(aa.getName());
			String name = p.getFileName().toString();
			if (fileName.equals(name)) {
				outToClient.writeObject("ok");
				outToClient.flush();
				FileInputStream fr = new FileInputStream(aa.getPath());
				BufferedInputStream br = new BufferedInputStream(fr);
				byte[] mybytearray = new byte[(int) aa.length()];
				System.out.println("Reading the file to byte array");
				br.read(mybytearray , 0, mybytearray.length );
				outToClient.writeObject(mybytearray.length );
				outToClient.flush();
				System.out.println("Writing the file name");
				outToClient.writeObject(fileName );
				outToClient.flush();
				System.out.println("Writing the byte array to client");
				outToClient.writeObject(mybytearray);
				outToClient.flush();
				fr.close();
				br.close();
				return;
			}
		}
		outToClient.writeObject("Error");
		outToClient.flush();

	}

	private void readMkdirFromInput(ObjectOutputStream outToClient, String[] arrStr) throws IOException {
		String path = file.getAbsolutePath() + "/" + arrStr[1];
		System.out.println("Path is : " + path);
		File cofFile = new File(path);
		cofFile.mkdir();
		outToClient.writeObject("ok");
		outToClient.flush();
		System.out.println("Setting a new directory name");
	}

	private void readLogoutFromInput(ObjectOutputStream outToClient, String[] arrStr) throws IOException {
		outToClient.writeObject("ok");
		outToClient.flush();
		System.out.println("Closing connection");

	}

	private void readCdFromInput(ObjectOutputStream outToClient, String fileName) throws IOException {
		if (fileName.equals("..")) {
			if ((file = file.getParentFile()) == null) {
				outToClient.writeObject("Error");
			} else {
				outToClient.writeObject("ok");
				outToClient.flush();
				System.out.println("Setting a new directory");
			}
		} else {
			String pathToCheck = file.getPath() + fileName;
			if (file.getParentFile() != null) {
				pathToCheck = file.getPath() + "/" + fileName;
				System.out.println("path to check " + pathToCheck);
			}
			File candidate = new File(pathToCheck);
			if (!candidate.exists()) {
				System.out.println("Error: " + fileName + " - couldn't find it.");
				outToClient.writeObject("error");
				outToClient.flush();
				return;
			}
			if (!candidate.isDirectory()) {
				System.out.println("Error: " + fileName + " is not a directory");
				outToClient.writeObject("error");
				outToClient.flush();
				return;
			}
			outToClient.writeObject("ok");
			outToClient.flush();
			file = candidate;
			System.out.println("Setting a new directory");
		}
	}

	private void redingDirFromInput(ObjectOutputStream outToClient) throws IOException {
		File[] arr = file.listFiles();
		System.out.println("Working with command dir");
		System.out.println("Writing into client");
		int length = arr.length;
		if (file.getParentFile() != null) {
			length += 1;
		}
		outToClient.writeObject(length);
		outToClient.flush();
		if (file.getParentFile() != null) {
			outToClient.writeObject("..");
			outToClient.flush();
		}
		for (File aa : arr) {
			String a = aa.getName();
			outToClient.writeObject(a);
			outToClient.flush();
		}
	}

	private void readReadFileFromInput(ObjectOutputStream outToClient, String str) throws IOException {
		File[] arr = file.listFiles();
		String fileName = str.substring(9, str.length());
		System.out.println("Working with file");
		System.out.println("Writing into client");
		for (File aa : arr) {
			Path p = Paths.get(aa.getName());
			String name = p.getFileName().toString();
			if (fileName.equals(name)) {
				outToClient.writeObject("ok");

				outToClient.flush();

				FileReader fr = new FileReader(aa.getPath());
				BufferedReader br = new BufferedReader(fr);
				String line;
				List<String> list = new ArrayList<>();
				while ((line = br.readLine()) != null) {
					list.add(line);

				}
				outToClient.writeObject(list.size());
				outToClient.flush();
				outToClient.writeObject(list);
				outToClient.flush();
				fr.close();
				br.close();
				return;
			}
		}
		outToClient.writeObject("Error");
		outToClient.flush();
	}

}
