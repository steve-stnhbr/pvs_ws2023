package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import at.ac.tuwien.ifs.sge.util.pair.ImmutablePair;
import com.google.common.collect.Lists;

import javax.annotation.concurrent.Immutable;
import java.util.Comparator;
import java.util.List;

public class HeuristicSimulationStrategy extends MCTSSimulationStrategy<Risk, RiskAction> {

  @Override
  public ImmutablePair<List<RiskAction>, Double> simulate(MCTSNode<Risk, RiskAction> node, long timeout, MCTSTree<Risk, RiskAction> tree) {
    Risk risk = node.getState();

    List<RiskAction> actionList = Lists.newArrayList();

    long startTime = System.nanoTime();
    while (!risk.isGameOver() && System.nanoTime() - startTime <= timeout) {
      // if the current player is -1, the game performs actions automatically
      if (risk.getCurrentPlayer() < 0) {
        risk = (Risk) risk.doAction();
        continue;
      }
      // select a random action
      Risk finalRisk = risk;
      RiskAction action = risk.getPossibleActions()
        .stream()
        .max(Comparator.comparingDouble(a -> finalRisk.doAction(a).getHeuristicValue(node.getPlayerId())))
        .orElseThrow(() -> new RuntimeException("No possible actions."));
      risk = (Risk) risk.doAction(action);
      actionList.add(action);
    }

    double utility = risk.getHeuristicValue(node.getPlayerId());

    node.setUtility(utility);
    node.setProperty("lastUtility", utility);
    return new ImmutablePair<>(actionList, utility);
  }
}
