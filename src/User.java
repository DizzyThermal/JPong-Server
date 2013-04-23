public class User
{
	private int id;
	private String ip;
	private int port;
	private String name;
	
	public User(int id, String ip, int port, String name)
	{
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.name = name;
	}
	
	public int getId()					{ return id;		}
	public String getIp()				{ return ip;		}
	public int getPort()				{ return port;		}
	public String getName()				{ return name;		}
	
	public void setId(int id)			{ this.id = id;		}
	public void setIp(String ip)		{ this.ip = ip;		}
	public void setPort(int port)	{ this.port = port;	}
	public void setName(String name)	{ this.name = name;	}
	
	public void setInfo(int id, String ip, int port, String name)
	{
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.name = name;
	}
}