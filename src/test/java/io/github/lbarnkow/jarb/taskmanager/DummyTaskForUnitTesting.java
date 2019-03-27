package io.github.lbarnkow.jarb.taskmanager;

public final class DummyTaskForUnitTesting extends AbstractBaseTask {
  TaskWrapper myWrapper;
  TaskState stateBeforeInitialization;
  TaskState stateOnInitialization;
  TaskState stateOnRun;
  TaskState stateOnInterruption;
  boolean succeeded;

  private long sleepMillis;
  private boolean failInitialize;
  private boolean failRun;

  /**
   * A dummy task used for unit testing. This task will not do any meaningful
   * work, but instead used <code>Thread.sleep()</code> to simulate computational
   * work using up time. Additionally, it can be configured to throw exceptions
   * during intialization or work phases.
   *
   * @param sleepMillis    the amount of milliseconds to sleep durch the work
   *                       phase
   * @param failInitialize <code>true</code> if this instance should throw an
   *                       exception during task initialization
   * @param failRun        <code>true</code> if this instance should throw an
   *                       exception during work phase
   */
  public DummyTaskForUnitTesting(long sleepMillis, boolean failInitialize, boolean failRun) {
    this.sleepMillis = sleepMillis;
    this.failInitialize = failInitialize;
    this.failRun = failRun;
  }

  public DummyTaskForUnitTesting(long sleepMillis) {
    this(sleepMillis, false, false);
  }

  public DummyTaskForUnitTesting() {
    this(0L);
  }

  void captureStateBeforeInitialization(TaskWrapper wrapper) {
    myWrapper = wrapper;
    stateBeforeInitialization = wrapper.getState();
  }

  @Override
  public void initializeTask() throws Throwable {
    if (myWrapper != null) {
      stateOnInitialization = myWrapper.getState();
    }

    if (failInitialize) {
      throw new InitializeException();
    }
  }

  @Override
  public void runTask() throws Throwable {
    if (myWrapper != null) {
      stateOnRun = myWrapper.getState();
    }

    if (failRun) {
      throw new RunException();
    }

    try {
      Thread.sleep(sleepMillis);
      succeeded = true;
    } catch (InterruptedException e) {
      if (myWrapper != null) {
        stateOnInterruption = myWrapper.getState();
      }
    }
  }

  @SuppressWarnings("serial")
  public static final class InitializeException extends Throwable {
  }

  @SuppressWarnings("serial")
  public static final class RunException extends Throwable {
  }
}