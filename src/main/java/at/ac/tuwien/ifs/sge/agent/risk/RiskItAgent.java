package at.ac.tuwien.ifs.sge.agent.risk;

import at.ac.tuwien.ifs.sge.agent.AbstractGameAgent;
import at.ac.tuwien.ifs.sge.agent.GameAgent;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.backpropagation.BasicBackpropagationStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion.RandomExpansionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection.UCB1SelectionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation.RandomSimulationStrategy;
import at.ac.tuwien.ifs.sge.engine.Logger;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

public class RiskItAgent extends AbstractGameAgent<Risk, RiskAction> implements
  GameAgent<Risk, RiskAction> {
  public static final int SIMULATION_STEPS = 45;

  public RiskItAgent(Logger log) {
    super(3D / 4D, 5, TimeUnit.SECONDS, log);
    //Do some setup before the TOURNAMENT starts.
  }

  @Override
  public void setUp(int numberOfPlayers, int playerId) {
    super.setUp(numberOfPlayers, playerId);
    // Do some setup before the MATCH starts
  }

  @Override
  public RiskAction computeNextAction(Risk game, long computationTime, TimeUnit timeUnit) {
    super.setTimers(computationTime, timeUnit); //Makes sure shouldStopComputation() works

    MCTSTree<Risk, RiskAction> tree = new MCTSTree<>(game,
      new UCB1SelectionStrategy<>(),
      // new RandomSelectionStrategy<>(),
      new RandomExpansionStrategy(),
      new RandomSimulationStrategy(),
      new BasicBackpropagationStrategy(),
      playerId);
    while (!shouldStopComputation()) {
      tree.simulate(SIMULATION_STEPS, TIMEOUT);
    }

    new Thread(() -> {
      try {
        new TraditionalTreePrinter().print(tree.getRoot(), new PrintStream(new FileOutputStream("tree.txt")));
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }).start();


    RiskAction bestAction = tree.getBestAction();

    log.debugf("Found best move: %s", bestAction.toString());
    System.out.println("> " + bestAction.toString());

    return bestAction;
  }

  @Override
  public void tearDown() {
    //Do some tear down after the MATCH
  }

  @Override
  public void destroy() {
    //Do some tear down after the TOURNAMENT
  }
}
