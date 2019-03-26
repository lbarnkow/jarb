package io.github.lbarnkow.jarb.rocketchat.tasks;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.jarb.api.RoomType.PUBLIC_CHANNEL;
import static io.github.lbarnkow.jarb.rocketchat.tasks.PublicChannelAutoJoinerTask.DEFAULT_SLEEP_TIME;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.lbarnkow.jarb.api.AuthInfo;
import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.misc.Holder;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClient;
import io.github.lbarnkow.jarb.rocketchat.RestClient;
import io.github.lbarnkow.jarb.rocketchat.realtime.ReplyErrorException;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.ReceiveJoinRoomReply;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendJoinRoom;
import io.github.lbarnkow.jarb.rocketchat.rest.RestClientException;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChannelListJoinedReply;
import io.github.lbarnkow.jarb.rocketchat.rest.messages.ChannelListReply;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawChannel;
import io.github.lbarnkow.jarb.taskmanager.TaskWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PublicChannelAutoJoinerTaskTest {

  private static final String TEST_BOTNAME = "TestBot";
  private static final String TEST_ROOM_ID_PREFIX = "TEST_RID_ID_";
  private static final String TEST_ROOM_NAME_PREFIX = "TEST_ROOM_NAME_";

  @Test
  void testTask()
      throws InterruptedException, RestClientException, ReplyErrorException, IOException {
    // given
    AuthInfo authInfo = AuthInfo.builder().userId(null).authToken(null).expires(null).build();
    Holder<AuthInfo> authInfoHolder = new Holder<>(authInfo);

    RestClient restClient = mock(RestClient.class);
    when(restClient.getChannelList(eq(authInfo))) //
        .thenReturn(new ChannelListReply(createChannelData(5)));
    when(restClient.getChannelListJoined(eq(authInfo)))
        .thenReturn(new ChannelListJoinedReply(createChannelData(2)));

    ReceiveJoinRoomReply joinRoomReply = mock(ReceiveJoinRoomReply.class);
    when(joinRoomReply.isSuccess()).thenReturn(true, false, true);
    RealtimeClient rtClient = mock(RealtimeClient.class);
    when(rtClient.sendMessageAndWait(any(SendJoinRoom.class), eq(ReceiveJoinRoomReply.class)))
        .thenReturn(joinRoomReply);

    Bot bot = mock(Bot.class);
    when(bot.getName()).thenReturn(TEST_BOTNAME);
    when(bot.offerRoom(any())).thenReturn(true, false, true);

    PublicChannelAutoJoinerTask task =
        new PublicChannelAutoJoinerTask(restClient, rtClient, bot, authInfoHolder, 10L);
    TaskWrapper wrapper = new TaskWrapper(task);

    // when
    wrapper.startTask(Optional.empty());
    Thread.sleep(30L);
    wrapper.stopTask();

    // then
    verify(restClient, atLeast(1)).getChannelList(eq(authInfo));
    verify(restClient, atLeast(1)).getChannelListJoined(eq(authInfo));

    ArgumentCaptor<SendJoinRoom> captor = ArgumentCaptor.forClass(SendJoinRoom.class);
    verify(rtClient, atLeast(4)).sendMessageAndWait(captor.capture(),
        eq(ReceiveJoinRoomReply.class));

    captor.getAllValues().forEach(message -> {
      assertThat(message.getParams().get(0).toString()).startsWith(TEST_ROOM_ID_PREFIX);
    });
  }

  @Test
  void testDefaultSleepTime() {
    // given
    Bot bot = mock(Bot.class);
    when(bot.getName()).thenReturn(TEST_BOTNAME);

    // when
    PublicChannelAutoJoinerTask task = new PublicChannelAutoJoinerTask(null, null, bot, null);

    // then
    assertThat(task.getSleepTime()).isEqualTo(DEFAULT_SLEEP_TIME);
  }

  private List<RawChannel> createChannelData(int numChannels) {
    List<RawChannel> channels = new ArrayList<>();

    for (int i = 0; i < numChannels; i++) {
      RawChannel channel = new RawChannel();
      channel.set_id(TEST_ROOM_ID_PREFIX + randomUUID().toString());
      channel.setName(TEST_ROOM_NAME_PREFIX + randomUUID().toString());
      channel.setT(PUBLIC_CHANNEL.getRawType());
      channels.add(channel);
    }

    return channels;
  }

}
