package at.ac.tuwien.ifs.sge.agent.risk.test;

import at.ac.tuwien.ifs.sge.agent.risk.util.DatasetWriter;
import at.ac.tuwien.ifs.sge.agent.risk.util.LineManager;
import at.ac.tuwien.ifs.sge.agent.risk.util.Tuple;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class PerformanceTestCommand {
  private final static DatasetWriter.CSV csv = new DatasetWriter.CSV("out/performance.csv");
  private static final List<Process> spawnedProcesses = new ArrayList<>();
  int timeout;

  private final LineManager.Interactor interactor;

  public PerformanceTestCommand() {
    this.interactor = new LineManager.Interactor.Default();
  }

  public PerformanceTestCommand(LineManager.Interactor interactor) {
    this.interactor = interactor;
  }

  public void run(int timeout, String player1Name, String player2Name, String gameName) {
    this.timeout = timeout;
    if (player1Name == null) {
      player1Name = "agents/mctsagent.jar";
    }
    if (player2Name == null) {
      player2Name = "agents/RiskItForTheBiscuit.jar";
    }
    if (gameName == null) {
      gameName = "games/sge-risk.jar";
    }

    if (!player1Name.startsWith("/")) {
        player1Name = "/" + player1Name;
    }
    if (!player2Name.startsWith("/")) {
        player2Name = "/" + player2Name;
    }
    if (!gameName.startsWith("/")) {
        gameName = "/" + gameName;
    }

    String player1Path = extractJAR(player1Name);
    String player2Path = extractJAR(player2Name);
    String gamePath = extractJAR(gameName);
    String managePath = extractJAR("/sge.jar");

    System.out.printf("player1Path = %s, player2Path = %s, gamePath = %s, managePath = %s%n", player1Path, player2Path, gamePath, managePath);
    String[] command = new String[] {
      "java",
      "-jar",
      managePath,
      "match",
      gamePath,
      player1Path,
      player2Path,
      "-c",
      String.valueOf(timeout),
      "--time-unit=MILLISECONDS"
    };

    System.out.println("Executing Command: " + String.join(" ", command));

    Process process = null;
    try {
      // Execute the command
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.redirectError(ProcessBuilder.Redirect.INHERIT);
      process = pb.start();

      synchronized (spawnedProcesses) {
        spawnedProcesses.add(process);
      }

      // Read the output
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      List<String> outputLines = new ArrayList<>();
      String line;
      int moves = 0;

      // Read the output until a certain string appears
      while ((line = reader.readLine()) != null) {
        outputLines.add(line);

        // Check if the specified string appears in the output
        if (line.contains("[sge info]")) {
          // Call the storeState function with the last 7 lines of output
          storeState(outputLines);
          moves++;
        }
      }
      // Wait for the process to finish
      process.waitFor();

      // Print the exit value of the process
      interactor.write("Exit value: " + process.exitValue());
      extractScore(outputLines, moves, managePath, gamePath, player1Path, player2Path);
      // delete the temporary files
      deleted(player1Path);
      deleted(player2Path);
      deleted(gamePath);
      deleted(managePath);
      synchronized (spawnedProcesses) {
        spawnedProcesses.remove(process);
      }
    } catch (IOException | InterruptedException e) {
      interactor.write("Exception caught");
      e.printStackTrace();
    }
  }

  private boolean deleted(String s) {
    return deleted(new File(s));
  }

  private boolean deleted(File f) {
    if (f.isDirectory()) {
      for (File c : f.listFiles()) {
        if (!deleted(c)) {
          return false;
        }
      }
    }
    boolean deleted = f.delete();
    if (!deleted) {
      System.out.println("Could not delete " + f.getAbsolutePath());
    }
    return deleted;
  }

  private String extractJAR(String name) {
    System.out.println("Extracting " + name);
    try(JarInputStream jarInputStream = new JarInputStream(getClass().getResourceAsStream(name));) {
      File outputDir = Files.createTempDir();
      File outputFile = new File(outputDir, name);
      if (!outputFile.getParentFile().exists()) {
        boolean created = outputFile.getParentFile().mkdirs();
        if (!created && ! outputFile.getParentFile().exists()) {
          throw new RuntimeException("Could not create directory " + outputFile.getParentFile().getAbsolutePath());
        }
      }
      byte[] buffer = new byte[1024];
      try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputFile), jarInputStream.getManifest())) {
        ZipEntry entry;

        while ((entry = jarInputStream.getNextEntry()) != null) {
          ZipEntry newEntry = new ZipEntry(entry.getName());
          jarOutputStream.putNextEntry(newEntry);

          if (!entry.isDirectory()) {
            int bytesRead;
            while ((bytesRead = jarInputStream.read(buffer)) != -1) {
              jarOutputStream.write(buffer, 0, bytesRead);
            }
          }

          jarOutputStream.closeEntry();
        }
      }

      return outputFile.getAbsolutePath();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // Function to store the last 7 lines of output
  private void storeState(List<String> outputLines) {
    //System.out.println("Last action = " + outputLines.get(outputLines.size() - 1));
    int startIndex = Math.max(0, outputLines.size() - 7);
    List<String> lines = outputLines.subList(startIndex, outputLines.size());
    interactor.write(String.join(" ", lines));
  }

  private void extractScore(List<String> outputLines, int moves, String managePath, String gamePath, String player1Path, String player2Path) {
    String manageName = managePath.substring(managePath.lastIndexOf("/") + 1);
    String gameName = gamePath.substring(gamePath.lastIndexOf("/") + 1);
    String player1Name = player1Path.substring(player1Path.lastIndexOf("/") + 1);
    String player2Name = player2Path.substring(player2Path.lastIndexOf("/") + 1);

    int startIndex = Math.max(0, outputLines.size() - 7);
    List<String> lines = outputLines.subList(startIndex, outputLines.size());
    lines = lines.stream().filter(line -> line != null && line.contains("|") && !line.contains("+")).collect(Collectors.toList());

    String[] line1 = lines.get(0).replace(" ", "").replace("\t", "").split("\\|");
    String player1 = line1[2];
    String player2 = line1[3];

    String[] line2 = lines.get(1).replace(" ", "").replace("\t", "").split("\\|");
    String score1 = line2[2];
    String score2 = line2[3];

    String[] line3 = lines.get(2).replace(" ", "").replace("\t", "").split("\\|");
    String utility1 = line3[2];
    String utility2 = line3[3];

    csv.appendToCSV(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%d", manageName, gameName, player1Name, player2Name, player1, player2, score1, score2, utility1, utility2, timeout, moves));
    interactor.write(String.format("Stored score %s:%s, %s:%s", player1, score1, player2, score2));
  }

  public static void main(String[] args) {
    Map<String, String> argMap = convertToKeyValuePair(args);
    int numThreads = Runtime.getRuntime().availableProcessors() * 2;
    LineManager lineManager = new LineManager(numThreads);
    int timeout = 10000;
    String[] players;

    if (argMap.containsKey("players")) {
      players = argMap.get("players").split(",");
    } else {
      String player1 = argMap.getOrDefault("player1", "agents/mctsagent.jar");
      String player2 = argMap.getOrDefault("player2", "agents/RiskItForTheBiscuit_ucb1_random_random_basic.jar");
      players = new String[] {player1, player2};
    }

    String game = argMap.getOrDefault("game", "games/sge-risk.jar");

    List<Tuple<String, String>> permutations = generatePermutations(players);

    for (int i = 0; i < numThreads; i++) {
      int finalI = i;
      Thread thread = new Thread(() -> {
        int runs = 0;
        PerformanceTestCommand pt = new PerformanceTestCommand(lineManager.getInteractor(finalI).setPrefix("PerformanceTest#" + finalI + ": "));
        while(!Thread.interrupted()) {
          Tuple<String, String> perm = permutations.get((finalI + runs)  % permutations.size());
          System.out.println("Starting game " + game + " player1: " + perm.getA() + " player2: " + perm.getB());
          pt.run(
            timeout * (((finalI + runs) % 4) + 1),
            perm.getA(),
            perm.getB(),
            game
            );
          runs++;
        }
      }, "PerformanceTest #" + i);
      thread.start();
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        // Interrupt the thread
        thread.interrupt();

        // Terminate spawned processes
        synchronized (spawnedProcesses) {
          for (Process p : spawnedProcesses) {
            if (p != null) {
              System.out.println("Destroying process");
              p.destroy();
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
              p.destroyForcibly();
            }
          }
        }
      }));
    }
  }

  private static HashMap<String, String> convertToKeyValuePair(String[] args) {
    HashMap<String, String> params = new HashMap<>();

    for (String arg: args) {
      String[] splitFromEqual = arg.split("=");

      String key = splitFromEqual[0].substring(2);
      String value = splitFromEqual[1];

      params.put(key, value);

    }

    return params;
  }

  public static List<Tuple<String, String>> generatePermutations(String[] array) {
    List<Tuple<String, String>> result = new ArrayList<>();
    List<String> currentPermutation = new ArrayList<>();
    boolean[] used = new boolean[array.length];

    generatePermutationsHelper(array, currentPermutation, used, result);

    return result;
  }

  private static void generatePermutationsHelper(String[] array,
                                                 List<String> currentPermutation,
                                                 boolean[] used,
                                                 List<Tuple<String, String>> result) {
    if (currentPermutation.size() == 2) {
      // Add a new Tuple to the result
      result.add(new Tuple<>(currentPermutation.get(0), currentPermutation.get(1)));
      return;
    }

    for (int i = 0; i < array.length; i++) {
      if (!used[i]) {
        used[i] = true;
        currentPermutation.add(array[i]);
        generatePermutationsHelper(array, currentPermutation, used, result);
        used[i] = false;
        currentPermutation.remove(currentPermutation.size() - 1);
      }
    }
  }

}
