package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
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
  @Override
  public MCTSNode<Risk, RiskAction> select(MCTSNode<Risk, RiskAction> root) {
    while (!root.isLeaf()) {
      root = selectChild(root.getChildren());
    }
    return root;
  }

  private MCTSNode<Risk, RiskAction> selectChild(Collection<MCTSNode<Risk, RiskAction>> children) {
    return children
      .stream()
      .map((node) -> new AbstractMap.SimpleEntry<>(calculateScore(node), node))
      .max(Comparator.comparingDouble(AbstractMap.SimpleEntry::getKey))
      .orElseThrow(() -> new RuntimeException("No children found"))
      .getValue();
  }

  public abstract double calculateScore(MCTSNode<Risk, RiskAction> node);
}
