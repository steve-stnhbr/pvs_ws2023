package at.ac.tuwien.ifs.sge.agent.risk.util;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation.MCTSSimulationStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation.RandomSimulationStrategy;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import com.google.common.collect.Lists;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.opencv.core.Mat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

public class RiskDataGeneration {
  private static final int MAX_ITERATIONS = 5000;

  private static final SplittableRandom RANDOM = new SplittableRandom();

  private PrintStream out;

  private MCTSSimulationStrategy<Risk, RiskAction> strategy;

  private final Runnable runnable;
  private final int playerID = 0;

  public RiskDataGeneration() throws FileNotFoundException {
    this(new RandomSimulationStrategy());
  }

  public RiskDataGeneration(MCTSSimulationStrategy<Risk, RiskAction> strategy) throws FileNotFoundException {
    this("out/data.csv", strategy);
  }

  public RiskDataGeneration(String fileName, MCTSSimulationStrategy<Risk, RiskAction> strategy)
      throws FileNotFoundException {
    this(new PrintStream(new FileOutputStream(fileName), true), strategy);
  }

  public RiskDataGeneration(PrintStream out, MCTSSimulationStrategy<Risk, RiskAction> strategy) {
    this.out = out;

    this.runnable = () -> {
      while (!Thread.currentThread().isInterrupted()) {
        List<Tuple<INDArray, Float>> states = Lists.newArrayList();
        Risk risk = new Risk();
        int iterations = 0;
        while (!risk.isGameOver() && iterations < MAX_ITERATIONS) {
          RiskAction action = selectActionRandom(risk);
          if (action == null) {
            risk = (Risk) risk.doAction();
            continue;
          }
          risk = (Risk) risk.doAction(action);
          INDArray state = RiskHasher.Tensor.encodeBoard(risk.getBoard());
          states.add(new Tuple<>(state, (float) risk.getHeuristicValue(playerID)));
          iterations++;
        }

        double utility = risk.getUtilityValue(playerID);
        double heuristic = risk.getHeuristicValue(playerID);
        System.out.println("Finished game with " + utility);
        states.forEach(state -> DatasetWriter.CSV.appendToCSV("out/data.csv", state.getA(), (float) utility, state.getB(), (float) heuristic));
      }
    };
  }

  private RiskAction selectActionRandom(Risk risk) {
    // if the current player is -1, the game performs actions automatically
    if (risk.getCurrentPlayer() < 0) {
      return null;
    }
    // select a random action
    return risk.getPossibleActions()
        .stream()
        .skip(RANDOM.nextInt(risk.getPossibleActions().size()))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("No possible actions."));
  }

  private RiskAction selectActionMaxHeuristic(Risk risk) {
    // if the current player is -1, the game performs actions automatically
    if (risk.getCurrentPlayer() < 0) {
      return null;
    }
    return risk.getPossibleActions()
        .stream()
        .max(Comparator.comparingDouble(a -> risk.doAction(a).getHeuristicValue(playerID)))
        .orElseThrow(() -> new RuntimeException("No possible actions."));
  }

  public void start(int numThreads) {
    for (int i = 0; i < numThreads; i++) {
      Thread thread = new Thread(this.runnable);
      thread.start();
    }
  }

  public static void main(String[] args) {
    try {
      new RiskDataGeneration().start(12);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
