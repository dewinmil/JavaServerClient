import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Object;
import java.util.*;

class server
{
  public static void main(String args[])
  {
  ConcurrentHashMap<String, String> hash = new ConcurrentHashMap<String, String>();
  ConcurrentHashMap<String, serverThread> threadMap = new ConcurrentHashMap<String, serverThread>();
    try
    {
      ServerSocketChannel c = ServerSocketChannel.open();
      c.bind(new InetSocketAddress(getPortNum()));
      int i = 0;
      while(true)
      {
        SocketChannel sc = c.accept();
        serverThread t = new serverThread(hash, sc, threadMap);
        threadMap.put(Long.toString(t.getId()), t);
        t.start();
        i++;
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
  boolean loop = true;
  SocketChannel sc;
  ConcurrentHashMap<String, String> hash;
  ConcurrentHashMap<String, serverThread> threadMap;
  serverThread(ConcurrentHashMap<String, String> hashmap, SocketChannel channel,
    ConcurrentHashMap<String, serverThread>  threadPool){
    sc = channel;
    hash = hashmap;
    threadMap = threadPool;
  }
  long id = getId();
  String key = new String(Long.toString(id));
  ByteBuffer buffer = ByteBuffer.allocate(4096);
  public void run(){
    try{
      System.out.println("added ID to hash: " + id);
      sc.read(buffer);
      String message = new String(buffer.array());
      System.out.println(message);
      hash.put(key, message.trim());
      
      while(loop){
        buffer = ByteBuffer.allocate(4096);
        sc.read(buffer);
        message = new String(buffer.array());
        System.out.println(message);
        if(message.trim().equals("exit")){
          loop = false;
        }
        String parts[] = message.split(" ", 2);
        if(hash.containsValue(parts[0].substring(1))){
          if(parts[0].substring(0,1).equals("-")){
            for(String keys: Collections.list(hash.keys())){  
              if(hash.get(keys).equals(parts[0].substring(1))){
                System.out.println(keys);
                serverThread t = threadMap.get(keys);
                String user = hash.get(key);
                t.sendToClient("Received from "+ user + ": " + parts[1]);
                t.sendToClient("Enter your message: ");
              }
            } 
          }
        }

       
        if(parts[0].equals("-broadcast")){
          String user = hash.get(key);
          for(String keys: Collections.list(hash.keys())){
            if(!keys.equals(key)){
              System.out.println(keys);
              serverThread t = threadMap.get(keys);
              t.sendToClient("Received from "+ user + ": " + parts[1]);
              t.sendToClient("Enter your message: "); 
            }
          } 
        }

        if(parts[0].equals("-remove")){
          System.out.println("is remove");
          if(hash.containsValue(parts[1].trim())){
          System.out.println("valid name");
            for(String keys: Collections.list(hash.keys())){
              if(hash.get(keys).equals(parts[1].trim())){
                System.out.println("found name");
                serverThread t = threadMap.get(keys);
                //t.interrupt();
                t.sendToClient("-17b482--exit/call");
                t.breakLoop();
                
                hash.remove(keys);
                threadMap.remove(keys); 
              }
            }
          } 
        }




    if(message.trim().equals("-users")){
          String list = "";
          for(String str: hash.values()){  
             list += str + "\n";
          }
            buffer = ByteBuffer.allocate(4096);
            buffer = ByteBuffer.wrap(list.getBytes());
            sc.write(buffer);

            System.out.println(list);
            String str = "Enter your message: ";
            buffer = ByteBuffer.allocate(4096);
            buffer = ByteBuffer.wrap(str.getBytes());
            sc.write(buffer);
        }
      }
    }
    catch(IOException e){
      System.out.println("Got an IO Exception");
			
    }
  }


  public void sendToClient(String message){
    try{
      ByteBuffer buffs = ByteBuffer.wrap(message.getBytes());
      sc.write(buffs);
    }catch(IOException e){
      System.out.println("Caught IO Exception - sendToClient");
    }
  }

  public void breakLoop(){
    loop = false;
  }

}
