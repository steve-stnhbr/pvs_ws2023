package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.backpropagation;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;

import java.util.List;

/**
 * This backpropagation heuristic considers all moves as the first move. When updating the data in a node the
 * visit counts and values are modified accordingly. The aim of this heuristic is to potentially improve the performance
 * by making the exploration more balanced. This implementation is probably not a perfect representation of AMAF
 * because the utility calculation is a bit random.
 */
public class AMAFBackpropagationStrategy extends MCTSBackpropagationStrategy<Risk, RiskAction> {

    @Override
    public void backpropagate(MCTSNode<Risk, RiskAction> leaf, List<RiskAction> actions, double utility, MCTSTree<Risk, RiskAction> tree) {
        MCTSNode<Risk, RiskAction> node = leaf;
        while (node != null) {
            // update
            node.setVisits(node.getVisits() + 1);
            node.setUtility(node.calculateAMAFUtilityValue());
            node = node.getParent();
        }
    }
}
