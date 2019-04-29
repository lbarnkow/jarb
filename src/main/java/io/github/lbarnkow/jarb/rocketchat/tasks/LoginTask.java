/*
 *    jarb is a framework and collection of Rocket.Chat bots written in Java.
 *
 *    Copyright 2019 Lorenz Barnkow
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.lbarnkow.jarb.rocketchat.tasks;

import io.github.lbarnkow.jarb.api.AuthInfo;
import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Credentials;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClient;
import io.github.lbarnkow.jarb.rocketchat.realtime.ReplyErrorException;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.ReceiveLoginReply;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendLogin;
import io.github.lbarnkow.jarb.taskmanager.AbstractBotSpecificTask;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

/**
 * A background task periodically trying to acquire new authorization tokens by
 * logging in to the chat server for a given bot.
 * 
 * @author lbarnkow
 */
@Slf4j
public class LoginTask extends AbstractBotSpecificTask {

  /**
   * The default interval in which a new authorization token should be acquired by
   * logging in to the chat server.
   */
  private static final long MAX_TOKEN_REFRESH_INTERVAL = Duration.ofMinutes(60L).toMillis();

  /**
   * Externally supplied and pre-configured <code>RealtimeClient</code> to use to
   * log in to the chat server.
   */
  private final transient RealtimeClient realtimeClient;

  /**
   * The listener to inform about new authorization tokens.
   */
  private final transient LoginTaskListener listener;

  /**
   * Externally supplied <code>Credentials</code> to use to log in.
   */
  private final transient Credentials credentials;

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
  public LoginTask(Bot bot, Credentials credentials, RealtimeClient realtimeClient,
      LoginTaskListener listener) {
    super(bot);

    this.realtimeClient = realtimeClient;
    this.listener = listener;
    this.credentials = credentials;
  }

  @Override
  public void runTask() throws Exception {
    final Bot bot = getBot();

    try {
      SendLogin message = new SendLogin(credentials);

      while (true) {
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

  /**
   * A listener that is to be informed whenever a login was successful and
   * produced a new authorization token.
   *
   * @author lbarnkow
   */
  public static interface LoginTaskListener {
    /**
     * Called on every successful login to pass along the new authorization token.
     *
     * @param source   the <code>LoginTask</code> emitting this event
     * @param bot      the <code>Bot</code> associated with the new subscription
     * @param authInfo the <code>AuthInfo</code> containing the new token
     */
    void onLoginAuthTokenRefreshed(LoginTask source, Bot bot, AuthInfo authInfo);
  }
}
