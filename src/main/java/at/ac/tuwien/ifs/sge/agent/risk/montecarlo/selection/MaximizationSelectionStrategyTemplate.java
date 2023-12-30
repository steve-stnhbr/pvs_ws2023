package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import org.checkerframework.checker.units.qual.A;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;

/**
 * This class is a template for selection strategies that maximize a score.
 */
public abstract class MaximizationSelectionStrategyTemplate extends MCTSSelectionStrategy<Risk, RiskAction> {
    public MaximizationSelectionStrategyTemplate() {
        super();
    }

  /**
   * This method selects the node maximizing the score.
   * @param root The root of the tree
   * @return The selected node.
   */
  @Override
  public MCTSNode<Risk, RiskAction> select(MCTSNode<Risk, RiskAction> root, MCTSTree<Risk, RiskAction> tree) {
    while (!root.isLeaf()) {
      root = selectChild(root.getChildren(), tree);
    }
    return root;
  }

  /**
   * This method selects the child maximizing the score.
   * @param children The children to select from.
   * @return The selected child.
   */
  private MCTSNode<Risk, RiskAction> selectChild(Collection<MCTSNode<Risk, RiskAction>> children, MCTSTree<Risk, RiskAction> tree) {
    return children
      .stream()
      .map((node) -> new AbstractMap.SimpleEntry<>(calculateScore(node, tree), node))
      .max(Comparator.comparingDouble(AbstractMap.SimpleEntry::getKey))
      .orElseThrow(() -> new RuntimeException("No children found"))
      .getValue();
  }

  /**
   * This method calculates the score for a node. The strategy looks for the node maximizing this value and will return it.
   * @param node The node to calculate the score for.
   * @return The score for the node.
   */
  public abstract double calculateScore(MCTSNode<Risk, RiskAction> node, MCTSTree<Risk, RiskAction> tree);
}
