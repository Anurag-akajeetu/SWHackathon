import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.StringTokenizer;
class Server{
	private static ServerSocket 				Server = null;
	private static ExecutorService 				threadPool;
	static double[] 							finalRes = null;
	static double[] 							sig = new double[15500];
	static double 								signal[] = new double[35000];
	static String 								Operation = null; 
	static int 									id = 0;
	static int 									status = 0;
	static Object[] 							res = null;
	static int 									countRun = 0;	
	static String 								charc;
	static String 								name;
	static String								Messsage;

	private static class EchoThread implements Runnable{
		
		private Socket 		socketObject = null;
		public EchoThread(Socket socketObject){
			this.socketObject = socketObject;
		}
		@Override
		public void run(){
			try {
				try{
					BufferedReader 	in;
					PrintStream 	out;
					in = new BufferedReader(new InputStreamReader(socketObject.getInputStream()));
					out = new PrintStream (socketObject.getOutputStream());
					Operation = in.readLine();
					System.out.println("Operation "+ Operation);
					name = in.readLine();
					System.out.println("name "+name);
					charc = in.readLine();
					System.out.println("charc "+charc);
					Messsage=in.readLine();
					String lengthTransmitted = in.readLine();
					System.out.println("Partial Message Recieved "+Messsage);
					System.out.println("Length calculated  "+Messsage.length());
					System.out.println("Length value transmiited " + lengthTransmitted);
					
					if (Operation.equalsIgnoreCase("register")){

						registration();
						out.println("Done");
						out.flush();
					}
					
					else if(Operation.equalsIgnoreCase("classify")) {

						String result = classifyMethod();
						System.out.println("After the classify Method "+result);
						out.println(result);
						out.flush();
					}
					else if(Operation.equalsIgnoreCase("refresh")) {

						String result = refreshMethod();
						System.out.println("After the classify Method "+result);
						out.println(result);
						out.flush();
					}
				}finally{
					socketObject.close();
				}
			} catch(Exception e){
				e.printStackTrace();
			}

		}
	}

	private static class Monitor implements Runnable {
		@Override
		public void run(){
			try
			{
				while(System.in.read() != '\n'){}
				shutdownServer();
			}
			catch(Exception E)
			{

			}

		}
	}

	public static void main(String args[]) throws Exception{


		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(new Monitor());
		Server = new ServerSocket(10008);
		System.out.println("hit Enter to stop the server..");
		while(true){
			Socket socketObject = Server.accept();
			long stopTimer = System.currentTimeMillis();
			System.out.println("Thread Created :"+ stopTimer );
			threadPool.submit(new EchoThread(socketObject));
			System.out.println("Thread");
		}
	}
	private static void shutdownServer()throws Exception{
		Server.close();
		threadPool.shutdown();
		System.exit(0);
	}
	private static void registration() throws Exception{

		System.out.println("in registration");
		String dbClassName = "com.mysql.jdbc.Driver";
		String con = "jdbc:mysql://10.218.110.136/SWhacks";
		Statement stmt = null;
		Class.forName(dbClassName);
		Properties p = new Properties();
		p.put("user","anurag");
		p.put("password","anurag");
		Connection c = DriverManager.getConnection(con,p);
		stmt = c.createStatement();
		//create table eegFeature (name varchar(50), charc varchar(2), meassage varchar(5000));

		String createString = "create table if not exists eegFeaturess ( name varchar(50) , charc varchar(2), meassage BLOB);";
		
		stmt.executeUpdate(createString);
		String insert;
		insert = "insert into eegFeaturess values( '"+name+"' ,'"+charc+"' ,'"+Messsage+"');";
		

		stmt.executeUpdate(insert);
		cleanData();
		System.out.println("It works !");
		c.close();
	}
	private static String classifyMethod() throws Exception{
		System.out.println("In classification");
		System.out.println("It works !");
		
		return pythonCall(Messsage);
	}
	
	private static String refreshMethod() throws Exception{
		System.out.println("In refresh Method");
		System.out.println("It works !");
		
		return pythonRefresh("Retrain");
	}
	static void cleanData(){
		Operation = "";
		charc = "";
		name = "";
		Messsage = "";
	}
	public static String pythonCall(String Message) throws Exception{
		  String msg = Message.substring(1,Message.length()-1);
		  System.out.println(" Printing the input: "+ msg);
		  String command= "python C:/Users/Anurag/swhacks/Mentalist.py "+msg;       
		  Process p = Runtime.getRuntime().exec(command);
		  
		  String s = null;
		  String x = null;
		  BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));;
		  
		  
		// read the output from the command
		  System.out.println("Here is the standard output of the command:\n");
		  String outer = null;
		  while ((s = stdInput.readLine()) != null) {
		      System.out.println(s);
		      outer = s;
		  }
		  
		  BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

		  // read any errors from the attempted command
		  System.out.println("Here is the standard error of the command (if any):\n");
		  while ((x = stdError.readLine()) != null) {
		      System.out.println(x);
		  }
		  System.out.println("Print s "+ outer);
		  return outer;

		}
	public static String pythonRefresh(String Message) throws Exception{
		  String msg = Message;
		  System.out.println(" Printing the input: "+ msg);
		  String command= "python C:/Users/Anurag/swhacks/Mentalist.py "+msg;       
		  Process p = Runtime.getRuntime().exec(command);
		  
		  String s = null;
		  String x = null;
		  BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));;
		  
		  
		// read the output from the command
		  System.out.println("Here is the standard output of the command:\n");
		  String outer = null;
		  while ((s = stdInput.readLine()) != null) {
		      System.out.println(s);
		      outer = s;
		  }
		  
		  BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

		  // read any errors from the attempted command
		  System.out.println("Here is the standard error of the command (if any):\n");
		  while ((x = stdError.readLine()) != null) {
		      System.out.println(x);
		  }
		  System.out.println("Print s "+ outer);
		  return "r";

		}
}
