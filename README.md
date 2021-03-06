# SocketRelay

This is a java application that facilities socket relay between a hosted online node instance and a local instance. This was build for solutions like Fantasy Grounds to allows easier hosting of games without machines having open ports to the internet.

The socket relay allows your players to connect to your machines without you having an open port to the internet. To use it follow these steps.

* Run the client on your machine.
* Select a suitable server from the client drop down.
* Select the game you are connecting to.
* Click connect.
* Give your players the url and port on the connection screen. Thats the url they need to connect to to reach your game.

That's it players should be able to connect to your game now.

## Server Component

We have one hosted server in capetown that you can use. Ideally we looking to host more servers around the world. You can host your own server if you do you just need modify the `server.properties` file for your client.

### Starting the server

To start the server you will need the java8 jre at least.

```cmd
java -jar SocketRelayClient-1.0.0.jar
```

### Server configuration

There is more information on setting up a server [here](docs/server.md). If you are willing to help out by adding a server to the set please get us the details and we can add your server details tot eh system.

## Client Component

You can just the `SocketRelayClient.exe` or if you on Linux or Mac you can use java to start the SocketRelay.

```cmd
java -jar SocketRelayClient-1.0.0.jar
```

![client](/images/client.png)

### The client configuration `config.json`

The config.json file describes the known games and the list of servers.

```js
{
	"servers":[
		{
			"name":"Celestial Cape Town",
			"ip":"socketrelay.celestial-games.com",
			"port":20000
		},
		{
			"name":"Localhost Test",
			"ip":"localhost",
			"port":10000
		}
	],
	"games":[
		{
			"name":"Fantasy Grounds",
			"port":1802
		}
	]
```

While you can modify this file it's ideal to rather let us know the changes you want to add and we can facilitated this in the root.

## Downloads

Client downloads there is an MSI for windows and a plane zip file for linux and Mac machines. You can of course use the zip file for windows if you choose.

Clients | Link
-------- | -------------
Win64 msi|http://downloads.celestial-games.com/socketrelay/SocketRelayClient-1.0.0.msi
Raw Zip | http://downloads.celestial-games.com/socketrelay/SocketRelayClientJava.zip

Server | Link
-------- | -------------
Raw Zip  | http://downloads.celestial-games.com/socketrelay/SocketRelayServer.zip

## Getting in touch

Our preference is to communicate on discord. Please feel free to get in touch here. https://discord.gg/PKpkurH

# Guides

* [Fantasy Grounds](docs/fantasygrounds.md)
* [Setting up a server](docs/server.md)
* [FAQ](docs/faq.md)
