package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.backpropagation;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;

import java.util.List;

public class ThompsonSamplingBackpropagationStrategyModified extends MCTSBackpropagationStrategy<Risk, RiskAction> {

    /**
     * This method backpropagates the utility of a child node to the parent node.
     */
    @Override
    public void backpropagate(MCTSNode<Risk, RiskAction> leaf, List<RiskAction> actions, double utility, MCTSTree<Risk, RiskAction> tree) {
        MCTSNode<Risk, RiskAction> node = leaf;
        while (node != null) {
            node.setVisits(node.getVisits() + 1);
            node.setUtility(node.getUtility() + leaf.getUtility());
            double utilityAfter = node.getUtility();
            if (node.getParent() != null) {
                MCTSNode<Risk, RiskAction> currentParent = node.getParent();
                double utilityBefore = currentParent.getUtility() + leaf.getUtility();
                if (utilityAfter >= utilityBefore) {
                    node.setProperty("successes", node.hasProperty("successes") ? node.getIntProperty("successes") + 1 : 1);
                } else {
                    node.setProperty("failures", node.hasProperty("failures") ? node.getIntProperty("failures") + 1 : 1);
                }
                if (currentParent.getParent() == null) { // if parent is root we need to update it too
                    utilityBefore = 0.5; // I assume the starting utility to be 0.5
                    if (utilityAfter >= utilityBefore) {
                        currentParent.setProperty("successes", currentParent.hasProperty("successes") ? currentParent.getIntProperty("successes") + 1 : 1);
                    } else {
                        currentParent.setProperty("failures", currentParent.hasProperty("failures") ? currentParent.getIntProperty("failures") + 1 : 1);
                    }
                }
            }
            node = node.getParent();
        }
    }
}
