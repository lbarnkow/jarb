package io.github.lbarnkow.jarb.taskmanager;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.lbarnkow.jarb.api.Bot;

class AbstractBotSpecificTaskTest {

	@Test
	void test() {
		// given
		Bot bot = Mockito.mock(Bot.class);
		when(bot.getName()).thenReturn("TestBot");

		// when
		AbstractBotSpecificTaskTestImpl task = new AbstractBotSpecificTaskTestImpl(bot);

		// then
		assertThat(task.getBot()).isSameAs(bot);
		assertThat(task.getName()).contains(bot.getName());
	}

	private static class AbstractBotSpecificTaskTestImpl extends AbstractBotSpecificTask {
		public AbstractBotSpecificTaskTestImpl(Bot bot) {
			super(bot);
		}

		@Override
		public void runTask() throws Throwable {
		}

	}
}
