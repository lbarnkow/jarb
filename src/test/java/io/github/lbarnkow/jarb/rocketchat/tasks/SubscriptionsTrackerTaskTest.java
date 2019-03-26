package io.github.lbarnkow.jarb.rocketchat.tasks;

import static com.google.common.truth.Truth.assertThat;
import static io.github.lbarnkow.jarb.rocketchat.tasks.SubscriptionsTrackerTask.DEFAULT_SLEEP_TIME;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.Room;
import io.github.lbarnkow.jarb.api.RoomType;
import io.github.lbarnkow.jarb.rocketchat.RealtimeClient;
import io.github.lbarnkow.jarb.rocketchat.realtime.ReplyErrorException;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.ReceiveGetSubscriptionsReply;
import io.github.lbarnkow.jarb.rocketchat.realtime.messages.SendGetSubscriptions;
import io.github.lbarnkow.jarb.rocketchat.sharedmodel.RawSubscription;
import io.github.lbarnkow.jarb.rocketchat.tasks.SubscriptionsTrackerTask.SubscriptionsTrackerTaskListener;
import io.github.lbarnkow.jarb.taskmanager.TaskWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SubscriptionsTrackerTaskTest implements SubscriptionsTrackerTaskListener {

  private static final String TEST_ROOM_ID_PREFIX = "TEST_RID_ID_";
  private static final String TEST_ROOM_NAME_PREFIX = "TEST_ROOM_NAME_";

  private static final String TEST_BOTNAME = "TestBot";

  private List<ListenerData> events = new ArrayList<>();

  @Test
  void testTask() throws InterruptedException, ReplyErrorException, IOException {
    // given
    ReceiveGetSubscriptionsReply reply = createTestData(5);

    RealtimeClient rtClient = mock(RealtimeClient.class);
    when(rtClient.sendMessageAndWait(any(), any())).thenReturn(reply);

    Bot bot = mock(Bot.class);
    when(bot.getName()).thenReturn(TEST_BOTNAME);

    SubscriptionsTrackerTask task = new SubscriptionsTrackerTask(bot, rtClient, 10L, this);
    TaskWrapper wrapper = new TaskWrapper(task);

    // when
    wrapper.startTask(Optional.empty());
    Thread.sleep(30L);
    wrapper.stopTask();

    // then
    verify(rtClient, atLeast(1)).sendMessageAndWait(any(SendGetSubscriptions.class),
        eq(ReceiveGetSubscriptionsReply.class));
    assertThat(events.size()).isEqualTo(5);
    events.forEach(event -> {
      assertThat(event.source).isSameAs(task);
      assertThat(event.bot.getName()).isEqualTo(TEST_BOTNAME);
      assertThat(event.room.getId()).startsWith(TEST_ROOM_ID_PREFIX);
      assertThat(event.room.getName()).startsWith(TEST_ROOM_NAME_PREFIX);
    });
  }

  @Test
  void testDefaultSleepTime() {
    // given
    Bot bot = mock(Bot.class);
    when(bot.getName()).thenReturn(TEST_BOTNAME);

    // when
    SubscriptionsTrackerTask task = new SubscriptionsTrackerTask(bot, null, null);

    // then
    assertThat(task.getSleepTime()).isEqualTo(DEFAULT_SLEEP_TIME);

  }

  private ReceiveGetSubscriptionsReply createTestData(int num) {
    List<RawSubscription> subs = new ArrayList<>();
    for (int i = 0; i < num; i++) {
      RawSubscription sub = new RawSubscription();
      sub.set_id("TEST_SUB_ID_" + randomUUID().toString());
      sub.setRid(TEST_ROOM_ID_PREFIX + randomUUID().toString());
      sub.setName(TEST_ROOM_NAME_PREFIX + randomUUID().toString());
      sub.setT(RoomType.PUBLIC_CHANNEL.getRawType());
      subs.add(sub);
    }

    ReceiveGetSubscriptionsReply reply = new ReceiveGetSubscriptionsReply();
    reply.setResult(subs);
    return reply;
  }

  @Override
  public void onNewSubscription(SubscriptionsTrackerTask source, Bot bot, Room room) {
    events.add(new ListenerData(source, bot, room));
  }

  private class ListenerData {
    SubscriptionsTrackerTask source;
    Bot bot;
    Room room;

    ListenerData(SubscriptionsTrackerTask source, Bot bot, Room room) {
      this.source = source;
      this.bot = bot;
      this.room = room;
    }
  }
}
