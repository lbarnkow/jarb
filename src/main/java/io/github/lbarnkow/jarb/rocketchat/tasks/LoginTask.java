package io.github.lbarnkow.jarb.rocketchat.tasks;

import io.github.lbarnkow.jarb.api.AuthInfo;
import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClient;
import io.github.lbarnkow.jarb.rocketchat.realtime.ReplyErrorException;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.ReceiveLoginReply;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendLogin;
import io.github.lbarnkow.jarb.taskmanager.AbstractBotSpecificTask;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginTask extends AbstractBotSpecificTask {

  private static final long MAX_TOKEN_REFRESH_INTERVAL = Duration.ofMinutes(60L).toMillis();

  private final RealtimeClient realtimeClient;
  private final LoginTaskListener listener;

  /**
   * <code>LoginTask</code> constructor.
   *
   * @param bot            the <code>Bot</code> associated with this
   *                       <code>LoginTask</code>
   * @param realtimeClient an initialized and connected
   *                       <code>RealtimeClient</code> to send message with
   * @param listener       a listener to inform about successful logins and
   *                       updated tokens
   */
  public LoginTask(Bot bot, RealtimeClient realtimeClient, LoginTaskListener listener) {
    super(bot);

    this.realtimeClient = realtimeClient;
    this.listener = listener;
  }

  @Override
  public void runTask() throws Throwable {
    Bot bot = getBot();

    try {
      while (true) {
        String username = bot.getCredentials().getUsername();
        String password = bot.getCredentials().getPassword();
        SendLogin message = new SendLogin(username, password);

        ReceiveLoginReply reply =
            realtimeClient.sendMessageAndWait(message, ReceiveLoginReply.class);
        AuthInfo authInfo = convertReply(reply);

        long sleepTime = 100L;

        if (authInfo.isValid()) {
          log.info("Successfully acquired auth token!");
          listener.onLoginAuthTokenRefreshed(this, bot, authInfo);

          sleepTime = calculateSleepTime(authInfo);
          log.info("Refreshing auth token in {} minutes.",
              Duration.ofMillis(sleepTime).toMinutes());
        }

        Thread.sleep(sleepTime);
      }

    } catch (ReplyErrorException e) {
      log.error("Login failed, stopping logins! Message: '{}'.", e.getError().getMessage());
    } catch (InterruptedException e) {
      log.trace("{} was interrupted.", getClass().getSimpleName());
    }

    log.info("Stopped login task.");
  }

  private AuthInfo convertReply(ReceiveLoginReply reply) {
    String userId = reply.getResult().getId();
    String token = reply.getResult().getToken();
    long epochExpires = reply.getResult().getTokenExpires().getDate();
    Instant expires = Instant.ofEpochMilli(epochExpires);

    return AuthInfo.builder().userId(userId).authToken(token).expires(expires).build();
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
