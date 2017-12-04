make: server.java client.java cryptotest.java
	javac server.java
	javac client.java
	openssl genpkey -algorithm RSA -out RSApriv.pem -pkeyopt rsa_keygen_bits:2048
	openssl rsa -pubout -in RSApriv.pem -out RSApub.pem
	openssl rsa -inform PEM -outform DER -pubin -pubout -in RSApub.pem -out RSApub.der
	openssl pkcs8 -topk8 -nocrypt -inform PEM -outform DER -in RSApriv.pem -out RSApriv.der
	rm RSApriv.pem
	rm RSApub.pem

clean: server.class client.class
	rm server.class
	rm client.class
	rm serverThread.class
	rm clientThread.class
	rm RSApriv.der
	rm RSApub.der

keys: cryptotest.java
	openssl genpkey -algorithm RSA -out RSApriv.pem -pkeyopt rsa_keygen_bits:2048
	openssl rsa -pubout -in RSApriv.pem -out RSApub.pem
	openssl rsa -inform PEM -outform DER -pubin -pubout -in RSApub.pem -out RSApub.der
	openssl pkcs8 -topk8 -nocrypt -inform PEM -outform DER -in RSApriv.pem -out RSApriv.der
	rm RSApriv.pem
	rm RSApub.pem
remove: RSApriv.der RSApub.der
	rm RSApriv.der
	rm RSApub.der

