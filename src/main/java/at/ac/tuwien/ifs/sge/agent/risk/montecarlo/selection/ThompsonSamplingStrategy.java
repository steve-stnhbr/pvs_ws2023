package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;

import java.util.Set;
import org.apache.commons.math3.distribution.BetaDistribution;

public class ThompsonSamplingStrategy<T, A> extends MCTSSelectionStrategy<Risk, RiskAction> {

    public ThompsonSamplingStrategy(int possibleActions) {
    }

    @Override
    public MCTSNode<Risk, RiskAction> select(MCTSNode<Risk, RiskAction> root, MCTSTree<Risk, RiskAction> tree) {
        Risk risk = root.getState();
        double maxThompsonSample = Double.NEGATIVE_INFINITY;
        MCTSNode<Risk, RiskAction> selected = null;

        if (root.isLeaf()) return root;
        for (MCTSNode<Risk, RiskAction> node : tree.getAllNodesForState(risk, tree)) {
            double sample = sampleBeta(tree.getSuccesses().get(node), tree.getFailures().get(node));
            if (sample > maxThompsonSample) {
                maxThompsonSample = sample;
                selected = node;
            }
        }

        return selected;
    }

    /**
     * BetaDistribution is used to sample from a beta distribution with alpha and beta
     *
     * @param alpha alpha
     * @param beta beta
     * @return sample
     */
    public double sampleBeta(int alpha, int beta) {
        double sample = 0.0;
        if (alpha > 0 || beta > 0) {
            sample = new BetaDistribution(alpha, beta).sample();
        }
        return sample;
    }
}
