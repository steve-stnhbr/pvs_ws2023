package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;

public class UCB1TunedSelectionStrategy extends MaximizationSelectionStrategyTemplate {

  @Override
  public double calculateScore(MCTSNode<Risk, RiskAction> node, MCTSTree<Risk, RiskAction> tree) {
    // TODO: implement right formula
    return (Math.sqrt(2 * Math.log(node.getParent().getVisits()) / node.getVisits()) +
      node.getAverageUtility() +
      Math.sqrt(node.getAverageUtility() * node.getAverageUtility() - (node.getAverageUtility() * node.getAverageUtility()) + Math.sqrt(2 * Math.log(node.getParent().getVisits()) / node.getVisits())));
  }

}
