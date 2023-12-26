package at.ac.tuwien.ifs.sge.agent.risk.util;

public class FireAndForget {
  private Runnable runnable;

  public FireAndForget(Runnable runnable) {
    this(runnable, false);
  }

  public FireAndForget(Runnable runnable, boolean autoStart) {
    this.runnable = runnable;
    if (autoStart) {
      start();
    }
  }

  public Thread start() {
    Thread thread = new Thread(runnable);
    thread.start();
    return thread;
  }

}
