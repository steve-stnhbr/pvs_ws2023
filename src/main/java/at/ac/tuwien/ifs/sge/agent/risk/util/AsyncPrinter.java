package at.ac.tuwien.ifs.sge.agent.risk.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncPrinter implements Closeable, Appendable {
  private final OutputStream out;

  private final ExecutorService executorService;

  public AsyncPrinter(OutputStream out) {
    this.executorService = Executors.newSingleThreadExecutor();
    this.out = out;
  }

    public void println(String line) {
        executorService.submit(() -> {
          try {
            out.write(line.getBytes());
            out.write('\n');
            out.flush();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    }

  @Override
  public void close() throws IOException {
    executorService.shutdown();
    out.close();
  }

  @Override
  public Appendable append(CharSequence csq) throws IOException {
    return null;
  }

  @Override
  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    return null;
  }

  @Override
  public Appendable append(char c) throws IOException {
    return null;
  }
}
