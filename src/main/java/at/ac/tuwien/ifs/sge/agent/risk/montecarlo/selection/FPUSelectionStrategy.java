package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;

public class FPUSelectionStrategy extends MaximizationSelectionStrategyTemplate {
  private static final double DEFAULT_FPU = 0.5;

  @Override
  public double calculateScore(MCTSNode<Risk, RiskAction> node, MCTSTree<Risk, RiskAction> tree) {
    return node.getVisits() == 0 ? DEFAULT_FPU : UCB1SelectionStrategy.EXPLORATION_PARAM * Math.sqrt(Math.log(node.getParent().getVisits()) / node.getVisits());
  }
}
