package io.github.lbarnkow.jarb.tasks;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.jarb.api.AuthInfo;
import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClient;
import io.github.lbarnkow.jarb.rocketchat.realtime.ReplyErrorException;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.ReceiveLoginReply;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendLogin;

public class LoginTask extends AbstractBaseTask {

	private static final Logger logger = LoggerFactory.getLogger(LoginTask.class);

	private static final long MAX_TOKEN_REFRESH_INTERVAL = Duration.ofMinutes(60L).toMillis();

	private final Bot bot;
	private final RealtimeClient realtimeClient;
	private final LoginTaskListener listener;

	public LoginTask(Bot bot, RealtimeClient realtimeClient, LoginTaskListener listener) {
		super(bot.getName());

		this.realtimeClient = realtimeClient;
		this.listener = listener;
		this.bot = bot;
	}

	@Override
	protected void initializeTask() throws Throwable {
	}

	@Override
	protected void runTask() throws Throwable {
		try {
			while (true) {
				String username = bot.getCredentials().getUsername();
				String passwordHash = bot.getCredentials().getPasswordHash();
				SendLogin message = new SendLogin(username, passwordHash);

				ReceiveLoginReply reply = realtimeClient.sendMessageAndWait(message, ReceiveLoginReply.class);
				AuthInfo authInfo = convertReply(reply);

				long sleepTime = 100L;

				if (authInfo.isValid()) {
					logger.info("Successfully acquired auth token!");
					listener.onLoginAuthTokenRefreshed(this, bot, authInfo);

					sleepTime = calculateSleepTime(authInfo);
					logger.info("Refreshing auth token in {} minutes.", Duration.ofMillis(sleepTime).toMinutes());
				}

				Thread.sleep(sleepTime);
			}

		} catch (ReplyErrorException e) {
			logger.error("Login failed, stopping logins! Message: '{}'.", e.getError().getMessage());
		} catch (InterruptedException e) {
		}

		logger.info("Stopped login task.");
	}

	private AuthInfo convertReply(ReceiveLoginReply reply) {
		String userId = reply.getResult().getId();
		String token = reply.getResult().getToken();
		long epochExpires = reply.getResult().getTokenExpires().get$date();
		Instant expires = Instant.ofEpochMilli(epochExpires);

		return new AuthInfo(userId, token, expires);
	}

	private long calculateSleepTime(AuthInfo authInfo) {
		Duration diff = Duration.between(Instant.now(), authInfo.getExpires());
		long sleepTime = Math.min((diff.toMillis() / 2L), MAX_TOKEN_REFRESH_INTERVAL);

		return sleepTime;
	}

	public static interface LoginTaskListener {
		void onLoginAuthTokenRefreshed(LoginTask source, Bot bot, AuthInfo authInfo);
	}
}
