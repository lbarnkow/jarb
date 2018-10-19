import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import messages.requests.ReqConnect;
import messages.requests.ReqGetRooms;
import messages.requests.ReqLogin;
import messages.requests.ReqPong;
import messages.responses.ResBase;
import messages.responses.ResConnected;
import messages.responses.ResLogin;
import messages.responses.ResMethodBase;

public class App {
	private static Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws IOException {
		try {
			// open websocket
			final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(
					new URI("ws://rocket.system.local/websocket/"));

			final Semaphore sync = new Semaphore(0);
			final Gson gson = new Gson();
			final Map<String, String> store = new ConcurrentSkipListMap<>();
			final Map<String, String> responses = new ConcurrentSkipListMap<>();

			// add listener
			clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
				public void handleMessage(String message) {
					ResBase msg = gson.fromJson(message, ResBase.class);

					if (msg.getMsg().equals("connected")) {
						responses.put("connected", message);
						sync.release();

					} else if (msg.getMsg().equals("ping")) {
						logger.info("was pinged; sending pong");
						clientEndPoint.sendMessage(gson.toJson(new ReqPong()));

					} else if (msg.getMsg().equals("result")) {
						ResMethodBase m = gson.fromJson(message, ResMethodBase.class);
						responses.put(m.getId(), message);
						sync.release();

					}
				}
			});

			clientEndPoint.sendMessage(gson.toJson(new ReqConnect()));
			while (!responses.containsKey("connected")) {
				sync.acquire();
			}
			ResConnected connected = gson.fromJson(responses.get("connected"), ResConnected.class);
			logger.info("Initiated session " + connected.getSession());
			store.put("session", connected.getSession());

			ReqLogin loginRequest = new ReqLogin("admin", "qqSKfA1hH9n37uR979iuck7POImY3HZp");
			clientEndPoint.sendMessage(gson.toJson(loginRequest));
			while (!responses.containsKey(loginRequest.getId())) {
				sync.acquire();
			}
			ResLogin loginResponse = gson.fromJson(responses.get(loginRequest.getId()), ResLogin.class);
			logger.info("Logged in: token=" + loginResponse.getResult().getToken());

			ReqGetRooms roomsRequest = new ReqGetRooms(0);
			clientEndPoint.sendMessage(gson.toJson(roomsRequest));
			while (!responses.containsKey(roomsRequest.getId())) {
				sync.acquire();
			}

			clientEndPoint.sendMessage(
					"{\"msg\":\"sub\",\"id\":\"42\",\"name\":\"stream-room-messages\",\"params\":[\"5tGMs9AQhJhApPPm8SBHXsiLJR8YQewBcd\",false]}");
			clientEndPoint.sendMessage("{\"msg\":\"method\",\"id\":\"43\",\"method\":\"subscriptions/get\"}");
			clientEndPoint.sendMessage(
					"{\"msg\":\"method\",\"id\":\"44\",\"method\":\"subscriptions/get\",\"params\":[{\"$date\":1539868771900}]}");
			// clientEndPoint.sendMessage(
			// "{\"msg\":\"method\",\"method\":\"rooms/get\",\"id\":\"42\",\"params\":[{\"$date\":0}]}");

			// wait 5 seconds for messages from websocket
			logger.info("going to sleep...");
			Thread.sleep(600000);
			logger.info("done sleeping!");

			clientEndPoint.userSession.close();

		} catch (InterruptedException ex) {
			System.err.println("InterruptedException exception: " + ex.getMessage());
		} catch (URISyntaxException ex) {
			System.err.println("URISyntaxException exception: " + ex.getMessage());
		}
	}
}
