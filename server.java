import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Object;


class server
{
  public static void main(String args[])
  {
  ConcurrentHashMap<String, String> hash = new ConcurrentHashMap<String, String>();
    try
    {
      ServerSocketChannel c = ServerSocketChannel.open();
      c.bind(new InetSocketAddress(getPortNum()));
      while(true)
      {
        SocketChannel sc = c.accept();
        serverThread t = new serverThread(hash, sc);
        t.start();
      }
    }
    catch(IOException e)
    {
      System.out.println("Got and IO Exception");
    }
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




class serverThread extends Thread{
  SocketChannel sc;
  ConcurrentHashMap<String, String> hash;
  serverThread(ConcurrentHashMap<String, String> hashmap, SocketChannel channel){
    sc = channel;
    hash = hashmap;
  }
  public void run(){
    long id = getId();
    String key = new String(Long.toString(id));
    try{
      ByteBuffer buffer = ByteBuffer.allocate(4096);
      sc.read(buffer);
      String message = new String(buffer.array());
      System.out.println(message);
      hash.put(key, message);
      
      boolean loop = true;
      while(loop){
        buffer = ByteBuffer.allocate(4096);
        sc.read(buffer);
        message = new String(buffer.array());
        System.out.println(message);
        if(message.trim().equals("exit")){
          loop = false;
        }
      }
    }
    catch(IOException e){
      System.out.println("Got an IO Exception");
			
    }
  }
}
