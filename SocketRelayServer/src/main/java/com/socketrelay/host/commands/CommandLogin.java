package com.socketrelay.host.commands;

import static com.socketrelay.host.Consts.LoginAttribute;
import static com.socketrelay.host.Consts.PlayerAttribute;
import static com.socketrelay.host.Consts.UserAccountInfoAttribute;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.playfab.PlayFabErrors.PlayFabResult;
import com.playfab.PlayFabServerAPI;
import com.playfab.PlayFabServerModels.AuthenticateSessionTicketRequest;
import com.playfab.PlayFabServerModels.AuthenticateSessionTicketResult;
import com.socketrelay.games.GameManager;
import com.socketrelay.games.GameSession;
import com.socketrelay.messages.Login;

public class CommandLogin extends Command<Login> {
	static final Logger log=LoggerFactory.getLogger(CommandLogin.class);

	private GameManager gameManager;
	
	public CommandLogin(GameManager gameManager){
		this.gameManager=gameManager;
	}
	
	@Override
	public void processCommand(IoSession session, Login message) {
		// Check login details
		AuthenticateSessionTicketRequest authenticateSessionTicketRequest=new AuthenticateSessionTicketRequest();
		authenticateSessionTicketRequest.SessionTicket=message.getSessionTicket();
		
		PlayFabResult<AuthenticateSessionTicketResult> response = PlayFabServerAPI.AuthenticateSessionTicket(authenticateSessionTicketRequest);
		if (response.Error!=null) {
			log.error("Closeing session due to an invalide sessionTicket. "+response.Error.errorMessage);
			session.closeNow();
		} else {
			// Add login record to session
			session.setAttribute(LoginAttribute,message);
			// Add player data to session
			session.setAttribute(UserAccountInfoAttribute,response.Result.UserInfo);
			session.setAttribute(PlayerAttribute,response.Result.UserInfo.TitleInfo.DisplayName);

			// Create game session. (It will live until all clients and the host DC or if the host DC's for 5 min)
			GameSession gameSession=gameManager.getGameSession(message.getGameSessionId());
			session.setAttribute(gameSession,gameSession);

			switch (message.getType()) {
			case Host:
				gameSession.setHost(session);
				break;
			case Player:
				gameSession.addPlayer(response.Result.UserInfo.TitleInfo.DisplayName, session);
				break;
			default:
				break;
			}			
		}
	}
}
