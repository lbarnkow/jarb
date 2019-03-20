package io.github.lbarnkow.jarb.taskmanager;

public final class DummyTaskForUnitTesting extends Task {
	TaskWrapper myWrapper;
	TaskState stateBeforeInitialization;
	TaskState stateOnInitialization;
	TaskState stateOnRun;
	TaskState stateOnInterruption;
	boolean succeeded;

	private long sleepMillis;
	private boolean failInitialize;
	private boolean failRun;

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
	protected void initializeTask() throws Throwable {
		if (myWrapper != null) {
			stateOnInitialization = myWrapper.getState();
		}

		if (failInitialize) {
			throw new InitializeException();
		}
	}

	@Override
	protected void runTask() throws Throwable {
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
	public final static class InitializeException extends Throwable {
	}

	@SuppressWarnings("serial")
	public final static class RunException extends Throwable {
	}
}