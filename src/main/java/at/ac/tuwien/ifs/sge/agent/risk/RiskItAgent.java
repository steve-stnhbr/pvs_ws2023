package at.ac.tuwien.ifs.sge.agent.risk;

import at.ac.tuwien.ifs.sge.agent.AbstractGameAgent;
import at.ac.tuwien.ifs.sge.agent.GameAgent;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.backpropagation.AMAFBackpropagationStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.backpropagation.MCTSBackpropagationStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion.MCTSExpansionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion.RandomExpansionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection.MCTSSelectionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection.UCB1SelectionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation.MCTSSimulationStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation.RandomSimulationStrategy;
import at.ac.tuwien.ifs.sge.engine.Logger;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

public class RiskItAgent extends AbstractGameAgent<Risk, RiskAction> implements
  GameAgent<Risk, RiskAction> {
  public static final int SIMULATION_STEPS = 45;

  private static final MCTSSelectionStrategy<Risk, RiskAction> DEFAULT_SELECTION_STRATEGY = new UCB1SelectionStrategy<>();
  private static final MCTSExpansionStrategy<Risk, RiskAction> DEFAULT_EXPANSION_STRATEGY = new RandomExpansionStrategy();
  private static final MCTSSimulationStrategy<Risk, RiskAction> DEFAULT_SIMULATION_STRATEGY = new RandomSimulationStrategy();
  private static final MCTSBackpropagationStrategy<Risk, RiskAction> DEFAULT_BACKPROPAGATION_STRATEGY = new AMAFBackpropagationStrategy();

  private final MCTSSelectionStrategy<Risk, RiskAction> selectionStrategy;
  private final MCTSExpansionStrategy<Risk, RiskAction> expansionStrategy;
  private final MCTSSimulationStrategy<Risk, RiskAction> simulationStrategy;
  private final MCTSBackpropagationStrategy<Risk, RiskAction> backpropagationStrategy;

  public RiskItAgent(Logger log) {
    //Do some setup before the TOURNAMENT starts.
    this(log,
      getDefaultSelectionStrategy(),
      getDefaultExpansionStrategy(),
      getDefaultSimulationStrategy(),
      getDefaultBackpropagationStrategy()
    );
  }

  public RiskItAgent(Logger log, MCTSSelectionStrategy<Risk, RiskAction> selectionStrategy, MCTSExpansionStrategy<Risk, RiskAction> expansionStrategy, MCTSSimulationStrategy<Risk, RiskAction> simulationStrategy,
                     MCTSBackpropagationStrategy<Risk, RiskAction> backpropagationStrategy) {
    super(3D / 4D, 5, TimeUnit.SECONDS, log);
    this.selectionStrategy = selectionStrategy;
    this.expansionStrategy = expansionStrategy;
    this.simulationStrategy = simulationStrategy;
    this.backpropagationStrategy = backpropagationStrategy;

    log.inff("Using selection strategies: %n\t%s,%n\t%s,%n\t%s,%n\t%s",
      selectionStrategy.getClass().getSimpleName(),
      expansionStrategy.getClass().getSimpleName(),
      simulationStrategy.getClass().getSimpleName(),
      backpropagationStrategy.getClass().getSimpleName());
  }

  private static MCTSSelectionStrategy<Risk, RiskAction> getDefaultSelectionStrategy() {
    return DEFAULT_SELECTION_STRATEGY;
  }

  private static MCTSExpansionStrategy<Risk, RiskAction> getDefaultExpansionStrategy() {
    return DEFAULT_EXPANSION_STRATEGY;
  }

  private static MCTSSimulationStrategy<Risk, RiskAction> getDefaultSimulationStrategy() {
    return DEFAULT_SIMULATION_STRATEGY;
  }

  private static MCTSBackpropagationStrategy<Risk, RiskAction> getDefaultBackpropagationStrategy() {
    return DEFAULT_BACKPROPAGATION_STRATEGY;
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
