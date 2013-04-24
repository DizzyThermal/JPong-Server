import java.awt.Rectangle;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
	public static int[] ballVec = new int[2];
	
	public static final int X = 0;
	public static final int Y = 1;
	
	public static final int SPEED_1 = 1;
	public static final int SPEED_2 = 2;
	
	public static byte[] sendData;
	public static byte[] receiveData;

	public static void main(String[] args)
	{
		buildRectangles();

		try
		{
			serverSocket = new DatagramSocket(Integer.parseInt(Resource.PORT));
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			// Wait for Player 1
			serverSocket.receive(receivePacket);
			if((new String(receiveData)).contains("/connected"))
			{
				sendData = "/player 1".getBytes();
				sendPacket1 = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), Integer.parseInt(Resource.PORT));
				serverSocket.send(sendPacket1);
				
				System.out.println("Player 1 Arrived!");
				// Wait for Player 2
				serverSocket.receive(receivePacket);
				if((new String(receiveData)).contains("/connected"))
				{
					sendData = "/player 2".getBytes();
					sendPacket2 = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), Integer.parseInt(Resource.PORT));
					serverSocket.send(sendPacket2);
					
					System.out.println("Player 2 Arrived!");
					
					// Both Players Are Here, FIRE FIRE!!!
					sendData = "/go".getBytes();
					serverSocket.send(sendPacket1);
					sendData = "/go".getBytes();
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

		while(true) 
		{
			try
			{
				serverSocket.receive(receivePacket);
				if((new String(receiveData)).contains("/coords"))
				{
					updatePlayerCoords(new String(receiveData));
					checkForBallCollisions();
					moveBall();
				}
				else
					System.out.println("Errorneous Coordinate Received");
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
		int y = Integer.parseInt(incomingMessage.split("\\\\")[1]);
		
		switch(player)
		{
			case 1:
				updateRectangle(p1, y);
				break;
			case 2:
				updateRectangle(p2, y);
				break;
		}
	}
	
	public static void updateRectangle(ArrayList<Rectangle> rec, int y)
	{
		for(int i = 0; i < 5; i++)
			rec.get(i).move((int)(rec.get(i).getX()), (int)((i*20) + y));
	}
	
	public static void checkForBallCollisions()
	{
		if(ball.getX() < 0)
		{
			try
			{
				sendData = "/score 2".getBytes();
				serverSocket.send(sendPacket1);
				sendData = "/score 2".getBytes();
				serverSocket.send(sendPacket2);
			}
			catch(Exception e) { e.printStackTrace(); }
			
			placeBall(395, 295);
			setBallDirection((-1*ballVec[X]), (-1*ballVec[Y]));
		}
		else if(ball.getX() > (800 - 10))
		{
			try
			{
				sendData = "/score 1".getBytes();
				serverSocket.send(sendPacket1);
				sendData = "/score 1".getBytes();
				serverSocket.send(sendPacket2);
			}
			catch(Exception e) { e.printStackTrace(); }
			
			placeBall(395, 295);
			setBallDirection((-1*ballVec[X]), (-1*ballVec[Y]));
		}
		else if((ball.getY() < 0) || (ball.getY() < (600-10)))
		{
			setBallDirection(ballVec[X], (-1*ballVec[Y]));
		}
		else if(ball.intersects(p1.get(0)))
			setBallDirection((-1*ballVec[X]), (SPEED_2)*ballVec[Y]);
		else if(ball.intersects(p1.get(1)))
			setBallDirection((-1*ballVec[X]), (SPEED_1)*ballVec[Y]);
		else if(ball.intersects(p1.get(2)))
			setBallDirection((-1*ballVec[X]), 0);
		else if(ball.intersects(p1.get(3)))
			setBallDirection((-1*ballVec[X]), (SPEED_1)*-1*ballVec[Y]);
		else if(ball.intersects(p1.get(4)))
			setBallDirection((-1*ballVec[X]), (SPEED_2)*-1*ballVec[Y]);
		else if(ball.intersects(p2.get(0)))
			setBallDirection((-1*ballVec[X]), (SPEED_2)*ballVec[Y]);
		else if(ball.intersects(p2.get(1)))
			setBallDirection((-1*ballVec[X]), (SPEED_1)*ballVec[Y]);
		else if(ball.intersects(p2.get(2)))
			setBallDirection((-1*ballVec[X]), 0);
		else if(ball.intersects(p2.get(3)))
			setBallDirection((-1*ballVec[X]), (SPEED_1)*-1*ballVec[Y]);
		else if(ball.intersects(p2.get(4)))
			setBallDirection((-1*ballVec[X]), (SPEED_2)*-1*ballVec[Y]);
	}
	
	public static void placeBall(int x, int y)
	{
		ball.move(x, y);
	}
	
	public static void setBallDirection(int xVec, int yVec)
	{
		ballVec[X] = xVec;
		ballVec[Y] = yVec;
	}
	
	public static void moveBall()
	{
		ball.move((int)(ball.getX()+ballVec[X]), (int)(ball.getY()+ballVec[Y]));
	}
}