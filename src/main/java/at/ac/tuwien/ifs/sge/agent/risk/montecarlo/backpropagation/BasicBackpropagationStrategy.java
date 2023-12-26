package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.backpropagation;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import org.checkerframework.checker.units.qual.A;

import java.util.List;

public class BasicBackpropagationStrategy extends MCTSBackpropagationStrategy<Risk, RiskAction> {

  /**
   * This method backpropagates the utility of a child node to the parent node.
   * @param child
   */
  @Override
  public void backpropagate(MCTSNode<Risk, RiskAction> leaf, List<RiskAction> actions, double utility, MCTSTree<Risk, RiskAction> tree) {
    MCTSNode<Risk, RiskAction> node = leaf;
    while (node != null) {
      node.setVisits(node.getVisits() + 1);
      node.setUtility(node.getUtility() + leaf.getUtility());
      node = node.getParent();
    }
  }
}
