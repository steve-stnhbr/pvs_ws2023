package at.ac.tuwien.ifs.sge.agent.risk;

import at.ac.tuwien.ifs.sge.agent.AbstractGameAgent;
import at.ac.tuwien.ifs.sge.agent.GameAgent;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.backpropagation.BasicBackpropagationStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.backpropagation.MCTSBackpropagationStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion.MCTSExpansionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion.RandomExpansionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection.MCTSSelectionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection.UCB1SelectionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation.MCTSSimulationStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation.RandomSimulationStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.util.FireAndForget;
import at.ac.tuwien.ifs.sge.agent.risk.util.TreePrinter;
import at.ac.tuwien.ifs.sge.engine.Logger;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;
import org.checkerframework.checker.units.qual.A;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

public class RiskItAgent extends AbstractGameAgent<Risk, RiskAction> implements
  GameAgent<Risk, RiskAction> {
  public static final int SIMULATION_STEPS = 45;

  private final MCTSSelectionStrategy<Risk, RiskAction> selectionStrategy;
  private final MCTSExpansionStrategy<Risk, RiskAction> expansionStrategy;
  private final MCTSSimulationStrategy<Risk, RiskAction> simulationStrategy;
  private final MCTSBackpropagationStrategy<Risk, RiskAction> backpropagationStrategy;

  public RiskItAgent(Logger log) {
    //Do some setup before the TOURNAMENT starts.
    this(log, new UCB1SelectionStrategy<>(), new RandomExpansionStrategy(), new RandomSimulationStrategy(), new BasicBackpropagationStrategy());
  }

  public RiskItAgent(Logger log, MCTSSelectionStrategy<Risk, RiskAction> selectionStrategy, MCTSExpansionStrategy<Risk, RiskAction> expansionStrategy, MCTSSimulationStrategy<Risk, RiskAction> simulationStrategy, MCTSBackpropagationStrategy<Risk, RiskAction> backpropagationStrategy) {
    super(3D / 4D, 5, TimeUnit.SECONDS, log);
    this.selectionStrategy = selectionStrategy;
    this.expansionStrategy = expansionStrategy;
    this.simulationStrategy = simulationStrategy;
    this.backpropagationStrategy = backpropagationStrategy;
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
      this.selectionStrategy,
      this.expansionStrategy,
      this.simulationStrategy,
      this.backpropagationStrategy,
      playerId);
    while (!shouldStopComputation()) {
      tree.simulate(SIMULATION_STEPS, TIMEOUT);
    }

    RiskAction bestAction = tree.getBestAction();

    log.debugf("Found best move: %s", bestAction.toString());

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
