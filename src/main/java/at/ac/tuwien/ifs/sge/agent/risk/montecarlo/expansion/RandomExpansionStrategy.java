package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import org.checkerframework.checker.units.qual.A;

import java.util.List;

public class RandomExpansionStrategy extends RiskAddAllExpansionStrategyTemplate {

  @Override
  public MCTSNode<Risk, RiskAction> select(List<MCTSNode<Risk, RiskAction>> generatedChildren) {
    return generatedChildren.get((int) (Math.random() * generatedChildren.size()));
  }
}
