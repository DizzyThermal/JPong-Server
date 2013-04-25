import java.awt.Rectangle;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Main
{
	public static DatagramSocket serverSocket;
	
	public static DatagramPacket receivePacket;
	public static DatagramPacket sendPacket1;
	public static DatagramPacket sendPacket2;
	
	public static ArrayList<Rectangle> p1 = new ArrayList<Rectangle>();
	public static ArrayList<Rectangle> p2 = new ArrayList<Rectangle>();

	public static Rectangle ball = new Rectangle(395, 295, 10, 10);
	public static int[] ballVec = { 2, 2 };
	
	public static final int X = 0;
	public static final int Y = 1;
	
	public static final int SPEED_1 = 1;
	public static final int SPEED_2 = 2;
	
	public static InetAddress p1IP;
	public static InetAddress p2IP;
	
	public static byte[] sendData = new byte[1024];
	public static byte[] receiveData = new byte[1024];

	public static void main(String[] args)
	{
		buildRectangles();
		try
		{
			serverSocket = new DatagramSocket(Integer.parseInt(Resource.PORT));
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			System.out.println(InetAddress.getLocalHost());
			// Wait for Player 1
			serverSocket.receive(receivePacket);
			p1IP = receivePacket.getAddress();
			if((new String(receivePacket.getData())).trim().contains("/connected"))
			{
				sendData = "/player 1".getBytes();
				sendPacket1 = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), Integer.parseInt(Resource.PORT));
				serverSocket.send(sendPacket1);
				
				System.out.println("Player 1 Arrived!");
				// Wait for Player 2
				receiveData = new byte[1024];
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				p2IP = receivePacket.getAddress();
				if((new String(receivePacket.getData())).trim().contains("/connected"))
				{
					sendData = "/player 2".getBytes();
					sendPacket2 = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), Integer.parseInt(Resource.PORT));
					serverSocket.send(sendPacket2);
					
					System.out.println("Player 2 Arrived!");
					
					// Both Players Are Here, FIRE FIRE!!!
					sendData = "/go".getBytes();
					sendPacket1 = new DatagramPacket(sendData, sendData.length, p1IP, Integer.parseInt(Resource.PORT));
					serverSocket.send(sendPacket1);
					sendData = "/go".getBytes();
					sendPacket2 = new DatagramPacket(sendData, sendData.length, p2IP, Integer.parseInt(Resource.PORT));
					serverSocket.send(sendPacket2);
					
					getReady();
				}
				else
					System.out.println("2: Something Goofed!");
			}
			else
				System.out.println("1: Something Goofed!");
		}
		catch (Exception e) { e.printStackTrace(); }
		int refresh = 0;
		long start = 0, finish = 0;
		while(true) 
		{
			start = System.nanoTime();
			try
			{
				receiveData = new byte[1024];
				receivePacket = new DatagramPacket(receiveData, receiveData.length);

				serverSocket.receive(receivePacket);
				if((new String(receivePacket.getData())).trim().contains("/coords"))
				{
					updatePlayerCoords(new String(receivePacket.getData()).trim());
					if (refresh > 2)
					{
						moveBall();
						checkForBallCollisions();
						refresh = 0;
					}
					else
						refresh++;
					//checkForBallCollisions();
					sendCoords();
				}
				else
					System.out.println("Errorneous Coordinate Received");
				finish = System.nanoTime();
				if((finish - start) < 333000000)
					Thread.sleep(1);
			}
			catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	public static void getReady()
	{
		try { Thread.sleep(3000); }
		catch(Exception e) { e.printStackTrace(); }
	}
	
	public static void buildRectangles()
	{
		for(int i = 0; i < 5; i++)
		{
			p1.add(new Rectangle(20, ((i*20) + 240), 20, 20));
			p2.add(new Rectangle(750, ((i*20) + 240), 20, 20));
		}
	}
	
	public static void updatePlayerCoords(String incomingMessage)
	{
		incomingMessage = incomingMessage.substring(8);
		int player = Integer.parseInt(incomingMessage.split("\\\\")[0]);
		int y = Integer.parseInt(incomingMessage.split("\\\\")[1].split("\\.")[0]);
		
		updateRectangle(player, y);
	}
	
	public static void updateRectangle(int player, int y)
	{
		switch(player)
		{
			case 1:
				for(int i = 0; i < 5; i++)
					p1.get(i).setLocation((int)(p1.get(i).getX()), (int)((i*20) + y));
				break;
			case 2:
				for(int i = 0; i < 5; i++)
					p2.get(i).setLocation((int)(p2.get(i).getX()), (int)((i*20) + y));
				break;
		}
	}
	
	public static void checkForBallCollisions()
	{
		if(ball.getX() < -50)
		{
			try
			{
				sendData = "/score 2".getBytes();
				sendPacket1 = new DatagramPacket(sendData, sendData.length, p1IP, Integer.parseInt(Resource.PORT));
				serverSocket.send(sendPacket1);
				sendData = "/score 2".getBytes();
				sendPacket2 = new DatagramPacket(sendData, sendData.length, p2IP, Integer.parseInt(Resource.PORT));
				serverSocket.send(sendPacket2);
			}
			catch(Exception e) { e.printStackTrace(); }
			
			placeBall(395, 295);
			setBallDirection((-1*ballVec[X]), (-1*ballVec[Y]));
		}
		else if(ball.getX() > 850)
		{
			try
			{
				sendData = "/score 1".getBytes();
				sendPacket1 = new DatagramPacket(sendData, sendData.length, p1IP, Integer.parseInt(Resource.PORT));
				serverSocket.send(sendPacket1);
				sendData = "/score 1".getBytes();
				sendPacket2 = new DatagramPacket(sendData, sendData.length, p2IP, Integer.parseInt(Resource.PORT));
				serverSocket.send(sendPacket2);
			}
			catch(Exception e) { e.printStackTrace(); }
			
			placeBall(395, 295);
			setBallDirection((-1*ballVec[X]), (-1*ballVec[Y]));
		}
		else if((ball.getY() < 0) || (ball.getY() > 590))
		{
			setBallDirection(ballVec[X], (-1*ballVec[Y]));
		}
		else if(ball.intersects(p1.get(0)))
			setBallDirection((-1*ballVec[X]), (-SPEED_2));
		else if(ball.intersects(p1.get(1)))
			setBallDirection((-1*ballVec[X]), (-SPEED_1));
		else if(ball.intersects(p1.get(2)))
			setBallDirection((-1*ballVec[X]), 0);
		else if(ball.intersects(p1.get(3)))
			setBallDirection((-1*ballVec[X]), (SPEED_1));
		else if(ball.intersects(p1.get(4)))
			setBallDirection((-1*ballVec[X]), (SPEED_2));
		else if(ball.intersects(p2.get(0)))
			setBallDirection((-1*ballVec[X]), (-SPEED_2));
		else if(ball.intersects(p2.get(1)))
			setBallDirection((-1*ballVec[X]), (-SPEED_1));
		else if(ball.intersects(p2.get(2)))
			setBallDirection((-1*ballVec[X]), 0);
		else if(ball.intersects(p2.get(3)))
			setBallDirection((-1*ballVec[X]), (SPEED_1));
		else if(ball.intersects(p2.get(4)))
			setBallDirection((-1*ballVec[X]), (SPEED_2));
	}
	
	public static void placeBall(int x, int y)
	{
		ball.setLocation(x, y);
	}
	
	public static void setBallDirection(int xVec, int yVec)
	{
		ballVec[X] = xVec;
		ballVec[Y] = yVec;
	}
	
	public static void moveBall()
	{
		ball.setLocation((int)(ball.getX()+ballVec[X]), (int)(ball.getY()+ballVec[Y]));
	}
	
	public static void sendCoords()
	{
		try
		{
			sendData = ("/coordinates " + p2.get(0).getY() + "\\" + ball.getX() + "\\" + ball.getY()).getBytes();
			sendPacket1 = new DatagramPacket(sendData, sendData.length, p1IP, Integer.parseInt(Resource.PORT));
			serverSocket.send(sendPacket1);
			sendData = ("/coordinates " + p1.get(0).getY() + "\\" + ball.getX() + "\\" + ball.getY()).getBytes();
			sendPacket2 = new DatagramPacket(sendData, sendData.length, p2IP, Integer.parseInt(Resource.PORT));
			serverSocket.send(sendPacket2);
		}
		catch(Exception e) { e.printStackTrace(); }
	}
}