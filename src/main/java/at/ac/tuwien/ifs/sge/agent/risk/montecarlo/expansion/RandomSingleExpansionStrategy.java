package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.util.Constants;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;

public class RandomSingleExpansionStrategy extends MCTSExpansionStrategy<Risk, RiskAction> {

  @Override
  public MCTSNode<Risk, RiskAction> expand(MCTSNode<Risk, RiskAction> node) {
    RiskAction selectedAction = node.getState().getPossibleActions().stream()
      .skip(Constants.RANDOM.nextInt(node.getState().getPossibleActions().size()))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("No possible actions."));
    Risk newState = (Risk) node.getState().doAction(selectedAction);
    MCTSNode<Risk, RiskAction> generatedNode = new MCTSNode<>(newState, node, selectedAction, node.getPlayerId(), node.getTree());
    node.addChild(generatedNode);
    return generatedNode;
  }
}
