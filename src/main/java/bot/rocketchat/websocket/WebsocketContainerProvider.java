package bot.rocketchat.websocket;

import javax.inject.Singleton;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;

@Singleton
public class WebsocketContainerProvider {
	WebsocketContainerProvider() {
	}

	public WebSocketContainer get() {
		return ContainerProvider.getWebSocketContainer();
	}
}
