# Setting up as SocketRelayServer

You can set up your own server and if you want to set up some we can share with the community that would be appreciated too. Just get in touch if you want us to add your server.

## Ubuntu

We are using Ubuntu LET 16 but most linux versions should suffice.

### Prepare your environment

Your environment will need jre1.8, and unzip.

```sh
~$ sudo apt update
~$ sudo apt-get install unzip
~$ sudo apt install openjdk-8-jre-headless
```

### Download and unpack the server

Next we download the zip file from the server and unpack it.

```sh
~$ wget http://downloads.celestial-games.com/socketrelay/SocketRelayServer.zip
~$ unzip SocketRelayServer.zip
Archive:  SocketRelayServer.zip
   creating: SocketRelayServer/
  inflating: SocketRelayServer/gson-2.8.6.jar
  inflating: SocketRelayServer/mina-core-2.1.3.jar
  inflating: SocketRelayServer/slf4j-api-1.7.30.jar
  inflating: SocketRelayServer/slf4j-jdk14-1.7.30.jar
  inflating: SocketRelayServer/socketrelay-common-1.0.0.jar
  inflating: SocketRelayServer/SocketRelayHost-1.0.0.jar
  inflating: SocketRelayServer/SocketRelayServer.sh
```

### Configure the .sh

Once unpacked we need to make some changes to the start script and to the config.

```sh
~$ cd SocketRelayServer/
~/SocketRelayServer$ chmod 777 SocketRelayServer.sh
~/SocketRelayServer$ sed -i -e 's/\r$//' SocketRelayServer.sh
```

Next modify the .sh to have the correct directory and also you can adjust the amount of ram you give it.

```sh
~/SocketRelayServer$ vi SocketRelayServer.sh
```

Edit 

`PATH_TO_RUN=/home/{user}/SocketRelayServer`

Change {user} to your user directory.

### Create a config file

The system needs a config file to set up teh ports. The file is called `config.json`.

```sh
vi config.json
```

```js
{
	'serverport': 20000, // The port this server will listen on, this is the port the client connects to.
	'serverIp': '0.0.0.0', // This is the ip address you want the server to bind to 0.0.0.0 will bind to all available interfaces
	'clientLow': 20001, // This is the low port to open for player connection.
	'clientHigh': 21000 // This is the high port to open for player connection.
}
```

Save that file.

### Start the service

To start the service you just need to execute teh .sh with a start paramater.

```sh
:~/SocketRelayServer$ ./SocketRelayServer.sh start
Starting socketrelayserver ...
socketrelayserver started ...
:~/SocketRelayServer$ nohup: appending output to 'nohup.out'
```
