# SocketRelay

This is a java application that facilities socket relay between a hosted online node instance and a local instance. This was build for solutions like Fantasy Grounds to allows easier hosting of games without machines having open ports to the internet.

## Server Component

We have one hosted server in capetown that you can use. Ideally we looking to host more servers around the world. You can host your own server if you do you just need modify the `server.properties` file for your client.

Starting the server.

```cmd
java -jar SocketRelayClient-1.0.0.jar
```

## Client Component

```cmd
java -jar SocketRelayClient-1.0.0.jar
```

![client](/images/client.png)

### Server Properties file

The server properties file describes hosted server you can use from the client.

```properties
Celestial\ Cape\ Town=socketrelay.celestial-games.com:20000
Local\ Test=localhost:10000
```

