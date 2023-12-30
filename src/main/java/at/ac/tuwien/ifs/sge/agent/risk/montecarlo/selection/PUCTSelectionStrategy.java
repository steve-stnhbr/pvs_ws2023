package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;

public class PUCTSelectionStrategy extends MaximizationSelectionStrategyTemplate {
  public static final Double C = 1.0, DEFAULT_ACTION_PROBABILITY = 0.5;
  @Override
  public double calculateScore(MCTSNode<Risk, RiskAction> node, MCTSTree<Risk, RiskAction> tree) {
    return node.getAverageUtility() +
      C * MCTSNode.actionProbabilities.getOrDefault(node.getAction(), DEFAULT_ACTION_PROBABILITY)
        *  Math.sqrt(Math.log(node.getParent().getVisits()) / node.getVisits());

  }
}
