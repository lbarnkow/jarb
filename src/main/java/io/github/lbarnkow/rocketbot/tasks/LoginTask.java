package io.github.lbarnkow.rocketbot.tasks;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.lbarnkow.rocketbot.api.Bot;
import io.github.lbarnkow.rocketbot.api.Bot.AuthInfo;
import io.github.lbarnkow.rocketbot.rocketchat.RealtimeClient;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.ReplyErrorException;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.ReceiveLoginReply;
import io.github.lbarnkow.rocketbot.rocketchat.realtime.messages.SendLogin;
import io.github.lbarnkow.rocketbot.taskmanager.Task;

public class LoginTask extends Task {

	private static final Logger logger = LoggerFactory.getLogger(LoginTask.class);

	private static final long MAX_TOKEN_REFRESH_INTERVAL = Duration.ofMinutes(60L).toMillis();

	private final Bot bot;
	private final RealtimeClient realtimeClient;
	private final LoginTaskListener listener;

	public LoginTask(Bot bot, RealtimeClient realtimeClient, LoginTaskListener listener) {
		this.bot = bot;
		this.realtimeClient = realtimeClient;
		this.listener = listener;
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
					logger.info("Successfully acquired auth token for bot '{}'!", bot.getName());
					bot.getAuthHolder().set(authInfo);
					listener.onLoginAuthTokenRefreshed(bot, this);

					sleepTime = calculateSleepTime(authInfo);
					logger.info("Refreshing auth token in {} minutes.", Duration.ofMillis(sleepTime).toMinutes());
				}

				Thread.sleep(sleepTime);
			}

		} catch (ReplyErrorException e) {
			logger.error("Login failed, stopping logins for bot '{}'! Message: '{}'.", bot.getName(),
					e.getError().getMessage());
		} catch (InterruptedException e) {
		}

		logger.info("Stopped login task for bot '{}'.", bot.getName());
	}

	private AuthInfo convertReply(ReceiveLoginReply reply) {
		String userId = reply.getResult().getId();
		String token = reply.getResult().getToken();
		long epochExpires = reply.getResult().getTokenExpires().getDate();
		Instant expires = Instant.ofEpochMilli(epochExpires);

		return new AuthInfo(userId, token, expires);
	}

	private long calculateSleepTime(AuthInfo authInfo) {
		Duration diff = Duration.between(Instant.now(), authInfo.getExpires());
		long sleepTime = Math.min((diff.toMillis() / 2L), MAX_TOKEN_REFRESH_INTERVAL);

		return sleepTime;
	}

	public static interface LoginTaskListener {
		void onLoginAuthTokenRefreshed(Bot bot, LoginTask loginTask);
	}
}
