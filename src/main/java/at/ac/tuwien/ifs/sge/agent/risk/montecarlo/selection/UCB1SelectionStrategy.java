package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;

import java.util.Set;

public class UCB1SelectionStrategy<T, A> extends MCTSSelectionStrategy<Risk, RiskAction> {

    public static final double EXPLORATION_PARAM = 1.1; // this can be configured

    public UCB1SelectionStrategy() {
        super();
    }

    /**
     * This method selects the next action by using UCB1
     *
     * @param root the current node
     * @return the child node with the selected action
     */
    @Override
    public MCTSNode<Risk, RiskAction> select(MCTSNode<Risk, RiskAction> root, MCTSTree<Risk, RiskAction> tree) {
        Risk risk = root.getState();
        Set<RiskAction> possibleActions = risk.getPossibleActions();
        double maxUCB = Double.NEGATIVE_INFINITY;
        MCTSNode<Risk, RiskAction> selected = null;

        if (root.isLeaf()) return root;
        for (RiskAction action : possibleActions) {
            MCTSNode<Risk, RiskAction> child = root.getChildForAction(action);
            if(child != null) {
                double explorationVal = EXPLORATION_PARAM * Math.sqrt(Math.log(root.getVisits()) / child.getVisits());
                double ucbVal = child.getAverageUtility() + explorationVal;
                if (ucbVal > maxUCB) {
                    maxUCB = ucbVal;
                    selected = child;
                }
            }
            else {
                // TODO - is this even relevant?
            }
        }
        return selected;
    }
}
