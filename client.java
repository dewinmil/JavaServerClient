import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

class client
{
  public static void main(String args[])
  {
	
    String ip = getIPNum();
    int port = getPortNum();
    try
    {
      
      SocketChannel sc = SocketChannel.open();
      sc.connect(new InetSocketAddress(ip, port));
      Console cons = System.console();

      
      String m = cons.readLine("Enter your userName: ");
      ByteBuffer buff = ByteBuffer.wrap(m.getBytes());
      //ByteBuffer rec = ByteBuffer.allocate(4096);
      sc.write(buff);
      
      clientThread t = new clientThread(sc);
      t.start();
      boolean loop = true;
      while(loop){
        m = cons.readLine("Enter your message: ");
        buff = ByteBuffer.wrap(m.getBytes());
        //ByteBuffer rec = ByteBuffer.allocate(4096);
        sc.write(buff);
        //buff.flip();
        //sc.read(buff);
        //received = new String(buff.array());
        //System.out.println(received);
        if(m.equals("exit")){
          loop = false;
        }
      }
      sc.close();
    }
    catch(IOException e)
    {
      System.out.println("Got an exception.");
    }
  }

  public static String  getIPNum(){
    Console cons = System.console();

    String ipstr = cons.readLine("Enter an IP to connect to: ");		
    return ipstr;
  }

  public static int getPortNum(){
    int iport = 0;
    Console cons = System.console();
    String port = cons.readLine("Enter a port to listen on: ");
    try {
      iport = Integer.parseInt(port);
    } catch (NumberFormatException e) {
      System.out.println("Invalid port number. Please try again.");
      getPortNum();
    }
    if(iport > 0){
      return iport;
    }
    else{
      System.out.println("Invalid port number. Please try again.");
      getPortNum();
    }
    return iport;
  }
}

class clientThread extends Thread{
  SocketChannel sc;
  clientThread(SocketChannel channel){
    sc = channel;
  }
  public void run(){
    try{
      boolean loop = true;
      while(loop){
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        sc.read(buffer);
        String message = new String(buffer.array());
        if(message.trim().equals("Enter your message:")){
          System.out.printf(message);
        }else{
          System.out.println("\n" + message);
        }
      }
    }
    catch(IOException e){
      System.out.println("Exiting Client");
                        
    }
  }
}



