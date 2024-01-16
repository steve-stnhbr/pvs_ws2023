package at.ac.tuwien.ifs.sge.agent.risk.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LineManager {
  public final HashMap<Integer, String> lines;
  public final List<Interactor> interactors;

  public LineManager(int numLines) {
    lines = new HashMap<>(numLines);
    this.interactors = new ArrayList<>(numLines);
    for (int i = 0; i < numLines; i++) {
      lines.put(i, "");
      interactors.add(new Interactor(i, this));
    }
  }

  public void write(int line, String s) {
    lines.put(line, s);
    System.out.println("\f");
    for (int i = 0; i < lines.size(); i++) {
      System.out.println(lines.get(i));
    }
  }

  public Interactor getInteractor(int index) {
    return interactors.get(index);
  }

  public void clearScreen() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }

  public void clearConsole() {
    try {
      final String os = System.getProperty("os.name");

      if (os.contains("Windows")) {
        Runtime.getRuntime().exec("cls");
      }
      else {
        Runtime.getRuntime().exec("clear");
      }
    }
    catch (final Exception e) {
      //  Handle any exceptions.
    }
  }

  public void backspaces() {
    for(int i = 0; i < 80*300; i++) // Default Height of cmd is 300 and Default width is 80
      System.out.print("\b"); // Prints a backspace
  }

  public static class Interactor {
    private final int index;
    private final LineManager lineManager;

    protected String prefix;

    public Interactor(int index, LineManager lineManager) {
        this.index = index;
        this.lineManager = lineManager;
    }

    public void write(String s) {
        lineManager.write(index, prefix + s);
    }

    public Interactor setPrefix(String prefix) {
      this.prefix = prefix;
      return this;
    }

    public static class Default extends Interactor {
      public Default() {
        super(0, null);
      }

      private Default(int index, LineManager lineManager) {
        super(index, lineManager);
      }

      public void write(String s) {
        System.out.println(prefix + s);
      }
    }
  }


}
