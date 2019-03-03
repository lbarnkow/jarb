package io.github.lbarnkow.rocketbot.taskmanager;

public final class DummyTask extends Task {
	TaskState stateOnConstruction;
	TaskState stateOnInitialization;
	TaskState stateOnRun;
	TaskState stateOnInterruption;
	boolean succeeded;

	private long sleepMillis;
	private boolean failInitialize;
	private boolean failRun;

	public DummyTask(long sleepMillis, boolean failInitialize, boolean failRun) {
		this.sleepMillis = sleepMillis;
		this.failInitialize = failInitialize;
		this.failRun = failRun;

		stateOnConstruction = getState();
	}

	public DummyTask(long sleepMillis) {
		this(sleepMillis, false, false);
	}

	public DummyTask() {
		this(0L);
	}

	@Override
	protected void initializeTask() throws Throwable {
		stateOnInitialization = getState();

		if (failInitialize) {
			throw new InitializeException();
		}
	}

	@Override
	protected void runTask() throws Throwable {
		stateOnRun = getState();

		if (failRun) {
			throw new RunException();
		}

		try {
			Thread.sleep(sleepMillis);
			succeeded = true;
		} catch (InterruptedException e) {
			stateOnInterruption = getState();
		}
	}

	@SuppressWarnings("serial")
	public final static class InitializeException extends Throwable {
	}

	@SuppressWarnings("serial")
	public final static class RunException extends Throwable {
	}
}