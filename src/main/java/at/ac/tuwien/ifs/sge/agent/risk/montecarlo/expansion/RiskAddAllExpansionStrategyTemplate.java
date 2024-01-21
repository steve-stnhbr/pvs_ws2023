package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a template for expansion strategies that add nodes for all possible actions to the tree.
 */
public abstract class RiskAddAllExpansionStrategyTemplate extends MCTSExpansionStrategy<Risk, RiskAction> {
  @Override
  public MCTSNode<Risk, RiskAction> expand(MCTSNode<Risk, RiskAction> node) {
    List<MCTSNode<Risk, RiskAction>> generatedChildren = new ArrayList<>();
    node.getState().getPossibleActions()
      .forEach(action -> {
        Risk newState = (Risk) node.getState().doAction(action).getGame();
        MCTSNode<Risk, RiskAction> generatedChild = new MCTSNode<>(newState, node, action, newState.getCurrentPlayer(), node.getTree());
        node.addChild(generatedChild);
        generatedChildren.add(generatedChild);
      });

    return select(generatedChildren);
  }

  /**
   * This method selects a node from the generated children. The simulation is run from this node
   * @param generatedChildren A list of nodes for all possible actions from the expanded node
   * @return The selected node the simulation should be run from
   */
  public abstract MCTSNode<Risk, RiskAction> select(List<MCTSNode<Risk, RiskAction>> generatedChildren);
}
