package io.github.lbarnkow.jarb.taskmanager;

import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVATING;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.ACTIVE;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEACTIVATING;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.DEAD;
import static io.github.lbarnkow.jarb.taskmanager.TaskState.UNUSED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

public class MockHelper {
  public static Map<Task, TaskWrapper> generateTaskMocks(int unused, int activating, int active,
      int deactivating, int dead) {
    Map<Task, TaskWrapper> result = new HashMap<>();

    for (int i = 0; i < unused; i++) {
      Task task = mock(Task.class);
      TaskWrapper wrapper = mock(TaskWrapper.class);
      when(wrapper.getState()).thenReturn(UNUSED);
      result.put(task, wrapper);
    }
    for (int i = 0; i < activating; i++) {
      Task task = mock(Task.class);
      TaskWrapper wrapper = mock(TaskWrapper.class);
      when(wrapper.getState()).thenReturn(ACTIVATING);
      result.put(task, wrapper);
    }
    for (int i = 0; i < active; i++) {
      Task task = mock(Task.class);
      TaskWrapper wrapper = mock(TaskWrapper.class);
      when(wrapper.getState()).thenReturn(ACTIVE);
      result.put(task, wrapper);
    }
    for (int i = 0; i < deactivating; i++) {
      Task task = mock(Task.class);
      TaskWrapper wrapper = mock(TaskWrapper.class);
      when(wrapper.getState()).thenReturn(DEACTIVATING);
      result.put(task, wrapper);
    }
    for (int i = 0; i < dead; i++) {
      Task task = mock(Task.class);
      TaskWrapper wrapper = mock(TaskWrapper.class);
      when(wrapper.getState()).thenReturn(DEAD);
      result.put(task, wrapper);
    }

    return result;
  }
}
