package at.ac.tuwien.ifs.sge.agent.risk;

import at.ac.tuwien.ifs.sge.agent.risk.util.DatasetWriter;
import at.ac.tuwien.ifs.sge.agent.risk.util.RiskHasher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PerformanceTestCommand {
  private final static DatasetWriter.CSV csv = new DatasetWriter.CSV("out/performance.csv");

  public void run(int timeout) {
    String command = "java -jar game/sge-1.0.2-exe.jar match game/sge-risk-1.0.2-exe.jar game/agents/mctsagent.jar build/libs/RiskItForTheBiscuit.jar -c " + timeout + " --time-unit=MILLISECONDS";
    Process process = null;
    try {
      // Execute the command
      ProcessBuilder pb = new ProcessBuilder(command.split(" "));
      pb.redirectError(ProcessBuilder.Redirect.INHERIT);
      process = pb.start();

      // Read the output
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      List<String> outputLines = new ArrayList<>();
      String line;

      // Read the output until a certain string appears
      while ((line = reader.readLine()) != null) {
        outputLines.add(line);
        System.out.println(line);

        // Check if the specified string appears in the output
        if (line.contains("[sge info]")) {
          // Call the storeState function with the last 7 lines of output
          storeState(outputLines);
          break;
        }
      }

      // Wait for the process to finish
      process.waitFor();

      // Print the exit value of the process
      System.out.println("Exit value: " + process.exitValue());
      extractScore(outputLines);
    } catch (IOException | InterruptedException e) {
      System.out.println("Exception caught");
      if (process != null) {
        System.out.println("Destroying process");
        process.destroyForcibly();
      }
      e.printStackTrace();
    }
  }
  // Function to store the last 7 lines of output
  private void storeState(List<String> outputLines) {
    //System.out.println("Last action = " + outputLines.get(outputLines.size() - 1));
    int startIndex = Math.max(0, outputLines.size() - 7);
    List<String> last7Lines = outputLines.subList(startIndex, outputLines.size());
    last7Lines.forEach(System.out::println);
  }

  private void extractScore(List<String> outputLines) {
    int startIndex = Math.max(0, outputLines.size() - 6);
    List<String> lines = outputLines.subList(startIndex, outputLines.size());

    String[] line1 = lines.get(1).replace(" ", "").replace("\t", "").split("\\|");
    String player1 = line1[2];
    String player2 = line1[3];

    String[] line2 = lines.get(3).replace(" ", "").replace("\t", "").split("\\|");
    String score1 = line2[2];
    String score2 = line2[3];

    String[] line3 = lines.get(4).replace(" ", "").replace("\t", "").split("\\|");
    String utility1 = line3[2];
    String utility2 = line3[3];

    csv.appendToCSV(String.format("%s,%s,%s,%s,%s,%s", player1, player2, score1, score2, utility1, utility2));
    System.out.printf("%s: Stored score %s:%s, %s:%s", Thread.currentThread().getName(), player1, score1, player2, score2);
  }

  public static void main(String[] args) {
    int numThreads = Runtime.getRuntime().availableProcessors() * 2;

    for (int i = 0; i < numThreads; i++) {
      Thread thread = new Thread(() -> {
        PerformanceTestCommand pt = new PerformanceTestCommand();
        while(!Thread.interrupted()) {
          pt.run();
        }
      }, "PerformanceTest #" + i);
      thread.start();
      Runtime.getRuntime().addShutdownHook(new Thread(thread::interrupt));
    }
  }

}
