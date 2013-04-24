import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main
{
	public static ServerSocket serverSocket;
	public static String incomingConnectionString;
	
	public static BufferedReader bReader;
	public static PrintWriter pWriter;
	
	public static int clients = 0;
	
	public static ArrayList<User> userList = new ArrayList<User>();

	public static void main(String[] args)
	{
		try
		{
			serverSocket = new ServerSocket(Integer.parseInt(Resource.PORT));
		}
		catch (Exception e) { e.printStackTrace(); }
		
		System.out.println("JPong Server Listening on Port: " + Resource.PORT);

		while(true) 
		{
			try
			{
				Socket connected = serverSocket.accept();
				
				
				
				addUser(++userId, serverSocket.accept());
				sendUserListToEveryone();
			}
			catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	public static void addUser(int id, Socket clientSocket)
	{
		String ip = clientSocket.getInetAddress() + "";
		ip = ip.substring(1);
		int port = clientSocket.getPort();
		String name = "";

		while(!clientSocket.isClosed())
		{
			String clientMessage = null;

			try { clientMessage = bReader.readLine(); }
			catch(Exception e) { e.printStackTrace(); }

			if (clientMessage == null)
				continue;
	
			name = clientMessage.substring(11);
			try { clientSocket.close(); }
			catch(Exception e) { e.printStackTrace(); }
		}
		
		userList.add(new User(id, ip, port, name));
	}
	
	public static void sendUserListToEveryone()
	{
		
		for(int i = 0; i < userList.size(); i++)
		{
			try
			{
				Socket clientSocket = new Socket(userList.get(i).getIp(), (Integer.parseInt(Resource.PORT)+1));
				pWriter = new PrintWriter(clientSocket.getOutputStream(), true);
				bReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			}
			catch(Exception e) { e.printStackTrace(); }
			
			pWriter.println(getUserList());
		}
	}
	
	public static String getUserList()
	{
		String userListStr = "";

		for(int i = 0; i < userList.size(); i++)
		{
			int id = userList.get(i).getId();
			String ip = userList.get(i).getIp();
			int port = userList.get(i).getPort();
			String name = userList.get(i).getName();

			userListStr = userListStr + id + "\\" + name;
			if((i+1) < userList.size())
				userListStr = userListStr + "\\";
		}
		
		return userListStr;
	}
}