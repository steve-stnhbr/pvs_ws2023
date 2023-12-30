package at.ac.tuwien.ifs.sge.agent.risk.util;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation.MCTSSimulationStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation.RandomSimulationStrategy;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import com.google.common.collect.Lists;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.SplittableRandom;

public class RiskDataGeneration {

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
        List<INDArray> states = Lists.newArrayList();
        Risk risk = new Risk();
        int iterations = 0;
        while (!risk.isGameOver() && iterations < 50000) {
          RiskAction action = selectActionRandom(risk);
          if (action == null) {
            risk = (Risk) risk.doAction();
            continue;
          }
          risk = (Risk) risk.doAction(action);
          INDArray state = RiskHasher.encodeBoard(risk.getBoard());
          states.add(state);
          iterations++;
        }

        double utility = risk.getUtilityValue(playerID);
        states.forEach(state -> DatasetWriter.appendToHDF("data.h5", state, (float) utility));
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
      new RiskDataGeneration().start(6);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
