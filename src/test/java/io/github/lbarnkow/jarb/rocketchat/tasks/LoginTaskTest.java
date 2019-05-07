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

import static com.google.common.truth.Truth.assertThat;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.lbarnkow.jarb.api.AuthInfo;
import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Credentials;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClient;
import io.github.lbarnkow.jarb.rocketchat.realtime.ReplyErrorException;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.ReceiveLoginReply;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.ReceiveLoginReply.LoginResult;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendLogin;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawDate;
import io.github.lbarnkow.jarb.rocketchat.tasks.LoginTask.LoginTaskListener;
import io.github.lbarnkow.jarb.taskmanager.TaskWrapper;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class LoginTaskTest implements LoginTaskListener {

  private static final String TEST_USERNAME = "username";
  private static final String TEST_PASSWORD = "password";
  private static final String TEST_BOTNAME = "TestBot";
  private static final String TEST_AUTH_ID = "auth_id";
  private static final String TEST_AUTH_TOKEN = "auth_token";

  private final List<ListenerData> events = new ArrayList<>();

  @Test
  void testTask() throws InterruptedException, ReplyErrorException, IOException, TimeoutException {
    // given
    final RealtimeClient rtClient = mock(RealtimeClient.class);
    final ReceiveLoginReply invalidReply = createLoginReplyData(false);
    final ReceiveLoginReply validReply = createLoginReplyData(true);
    when(rtClient.sendMessageAndWait(any(SendLogin.class), eq(ReceiveLoginReply.class)))
        .thenReturn(invalidReply, validReply);

    final Bot bot = mock(Bot.class);
    final Credentials credentials = new Credentials(TEST_USERNAME, TEST_PASSWORD);
    when(bot.getName()).thenReturn(TEST_BOTNAME);

    final LoginTask task = new LoginTask(bot, credentials, rtClient, this);
    final TaskWrapper wrapper = new TaskWrapper(task);

    // when
    wrapper.startTask(Optional.empty());
    Thread.sleep(300L);
    wrapper.stopTask();

    // then
    verify(rtClient, atLeast(2)).sendMessageAndWait(any(SendLogin.class),
        eq(ReceiveLoginReply.class));
    assertThat(events).isNotEmpty();
    events.forEach(event -> {
      assertThat(event.source).isSameInstanceAs(task);
      assertThat(event.bot.getName()).isEqualTo(TEST_BOTNAME);
      assertThat(event.authInfo.getUserId()).isEqualTo(TEST_AUTH_ID);
      assertThat(event.authInfo.getAuthToken()).isEqualTo(TEST_AUTH_TOKEN);
    });
  }

  private ReceiveLoginReply createLoginReplyData(final boolean valid) {
    final ReceiveLoginReply reply = new ReceiveLoginReply();
    final LoginResult result = new ReceiveLoginReply.LoginResult();
    final RawDate expires = new RawDate();

    if (valid) {
      expires.setDate(Instant.now().plus(7, DAYS).getEpochSecond() * 1000);
    } else {
      expires.setDate(Instant.now().minus(7, DAYS).getEpochSecond() * 1000);
    }

    result.setId(TEST_AUTH_ID);
    result.setToken(TEST_AUTH_TOKEN);
    result.setTokenExpires(expires);

    reply.setResult(result);

    return reply;
  }

  @Override
  public void onLoginAuthTokenRefreshed(final LoginTask source, final Bot bot,
      final AuthInfo authInfo) {
    events.add(new ListenerData(source, bot, authInfo));
  }

  private class ListenerData {
    LoginTask source;
    Bot bot;
    AuthInfo authInfo;

    ListenerData(final LoginTask source, final Bot bot, final AuthInfo authInfo) {
      this.source = source;
      this.bot = bot;
      this.authInfo = authInfo;
    }
  }
}
