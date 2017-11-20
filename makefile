make: server.java client.java
	javac server.java
	javac client.java

remove: server.class client.class
	rm server.class
	rm client.class
	rm serverThread.class
	rm clientThread.class
