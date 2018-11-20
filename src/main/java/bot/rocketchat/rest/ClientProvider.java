package bot.rocketchat.rest;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@Singleton
public class ClientProvider {
	ClientProvider() {
	}

	public Client newClient() {
		return ClientBuilder.newClient();
	}
}
