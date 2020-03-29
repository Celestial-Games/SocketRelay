# Fantasy Grounds Guide

Fantasy grounds typically uses port 1802 for client connections. This means that people online need to be able to connect to your ip address on that port. It is possible to change the port Fantasy Grounds uses but it is not convenient. With this in mind it's easiest if you and your players all use the Socket Relay tool.

## Host Setup

Start your Fantasy Realms game and make sure it is ready for connections.

Start up the Socket Relay Client pick a server and the game. Click connect as host. This should connect and at the top of your screen you should see **socketrelay.celestial-games.com:20089** the exact url will depend on you server chosen and the port will change each time you connect. The port is not allocated to your game. Your players will need to connect to that port.

![client](/images/fc_host.png)

> You will need to send your players the port number and tell them which server you connected to.

Start your Fantasy Grounds and host a game as you would always.

## Client Setup

Start the Socket Relay Client. Click the 'Connect as Player' button.

You should see

![client](/images/fc_port.png)

Enter the port number from your hoist player and click ok.

Now start up fantasy grounds and say join game.

![client](/images/fc_join.jpg)

> You must say localhost for the address as FG will be connecting your instance of Socket Relay.

Click start and FG should connect to your host players machine.