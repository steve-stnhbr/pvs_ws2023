package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import at.ac.tuwien.ifs.sge.util.pair.ImmutablePair;

public class UCB1TunedSelectionStrategy<T, A> extends MCTSSelectionStrategy<Risk, RiskAction> {
  @Override
  public MCTSNode<Risk, RiskAction> select(MCTSNode<Risk, RiskAction> root, MCTSTree<Risk, RiskAction> tree) {
    return tree.stream()
      .filter(MCTSNode::isLeaf)
      .map(node -> new ImmutablePair<>(node, calculateUCB1Tuned(node)))
      .max((o1, o2) -> Double.compare(o1.getB(), o2.getB()))
      .orElseThrow()
      .getA();
  }

  private double calculateUCB1Tuned(MCTSNode<Risk, RiskAction> node) {
    // ucb1tuned score
    // TODO: implement right formula
    return (Math.sqrt(2 * Math.log(node.getParent().getVisits()) / node.getVisits()) +
      node.getAverageUtility() +
      Math.sqrt(node.getAverageUtility() * node.getAverageUtility() - (node.getAverageUtility() * node.getAverageUtility()) + Math.sqrt(2 * Math.log(node.getParent().getVisits()) / node.getVisits())));
  }
}
