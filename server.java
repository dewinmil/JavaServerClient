import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Object;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;

class server
{
  public static void main(String args[])
  {
  ConcurrentHashMap<String, String> hash = new ConcurrentHashMap<String, String>();
  ConcurrentHashMap<String, serverThread> threadMap = new ConcurrentHashMap<String, serverThread>();
  ConcurrentHashMap<String, byte[]> keyHash = new ConcurrentHashMap<String, byte[]>();
  cryptotest ct = new cryptotest();
  ct.setPrivateKey("RSApriv.der");
    try
    {
      ServerSocketChannel c = ServerSocketChannel.open();
      c.bind(new InetSocketAddress(getPortNum()));
      int i = 0;
      while(true)
      {
        SocketChannel sc = c.accept();
        serverThread t = new serverThread(hash, sc, threadMap, keyHash);
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
  ConcurrentHashMap<String, byte[]> keyHash;
  serverThread(ConcurrentHashMap<String, String> hashmap, SocketChannel channel,
    ConcurrentHashMap<String, serverThread>  threadPool,
    ConcurrentHashMap<String, byte[]> keyList){
    sc = channel;
    hash = hashmap;
    threadMap = threadPool;
    keyHash = keyList;
  }
  long id = getId();
  String key = new String(Long.toString(id));
  ByteBuffer buffer = ByteBuffer.allocate(256);

  cryptotest ct = new cryptotest();
  public void run(){
    ct.setPrivateKey("RSApriv.der");
    try{

      sc.read(buffer);
      byte clientKey[] = ct.RSADecrypt(buffer.array());
      keyHash.put(key, clientKey);

      byte[] randBytes = new byte[16];
      SecretKey skey = new SecretKeySpec(clientKey, 0, clientKey.length, "AES");

      buffer = ByteBuffer.allocate(16);
      sc.read(buffer);
      randBytes = buffer.array();
      IvParameterSpec iv = new IvParameterSpec(randBytes);

      buffer = ByteBuffer.allocate(4096);
      int buffSize = sc.read(buffer);
      byte[] trimArray = new byte[buffSize];
      
      for(int i = 0; i < buffSize; i++){
        trimArray[i] = buffer.array()[i];
      }
      
      byte buff[] = ct.decrypt(trimArray, skey, iv);
      //String message = new String(buffer.array());
      String message = new String(buff);
      System.out.println("added ID to hash: " + id);
      System.out.println(message);
      hash.put(key, message.trim());
      
      while(loop){
        buffer = ByteBuffer.allocate(16);
        sc.read(buffer);

        randBytes = buffer.array();
        iv = new IvParameterSpec(randBytes);
      
        buffer = ByteBuffer.allocate(4096);

        buffSize = sc.read(buffer);

        trimArray = new byte[buffSize];
      
        for(int i = 0; i < buffSize; i++){
          trimArray[i] = buffer.array()[i];
        }

        buff = ct.decrypt(trimArray, skey, iv);

        message = new String(buff);
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

                t.sendToClient("Received from "+ user + ": " + parts[1], keys);
                t.sendToClient("Enter your message: ", keys);
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
              t.sendToClient("Received from "+ user + ": " + parts[1], keys);
              t.sendToClient("Enter your message: ", keys); 
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
                t.sendToClient("-17b482--exit/call", keys);
                t.breakLoop();
                
                hash.remove(keys);
                threadMap.remove(keys);
                keyHash.remove(keys); 
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


  public void sendToClient(String message, String keys){
    try{
      
      SecretKey skey = new SecretKeySpec(keyHash.get(keys), 0, keyHash.get(keys).length, "AES");

      byte[] randBytes = new byte[16];
      IvParameterSpec iv = new IvParameterSpec(randBytes);
      ByteBuffer buffs = ByteBuffer.wrap(ct.encrypt(message.getBytes(), skey, iv));
      ByteBuffer randBuffer = ByteBuffer.wrap(randBytes);
      sc.write(randBuffer);
      sc.write(buffs);
    }catch(IOException e){
      System.out.println("Caught IO Exception - sendToClient");
    }
  }

  public void breakLoop(){
    loop = false;
  }

}









