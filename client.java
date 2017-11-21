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
        if(t.isAlive()){
          buff = ByteBuffer.wrap(m.getBytes());
          sc.write(buff);
          if(m.equals("exit")){
            loop = false;
          }
        }else{
          loop = false;
        }
      }
      sc.close();
    }
    catch(IOException e)
    {
      //System.out.println("You have been removed from the server.");
      //expect io exception when removed from server / sc is closed
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
        }else if(message.trim().equals("-17b482--exit/call")){
          loop = false;
          System.out.println("\n" + "You have been removed from the server");
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



