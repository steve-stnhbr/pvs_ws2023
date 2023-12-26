package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;

import java.util.Set;
import org.apache.commons.math3.distribution.BetaDistribution;

public class ThompsonSamplingStrategy<T, A> extends MCTSSelectionStrategy<Risk, RiskAction> {
    private int armCount;
    /* TODO update these successes and failures in the simulation part - this class should only be used
        in combination with a working update to these arrays in the simulation part of MCTS
     */

    private int[] successes, failures;

    public ThompsonSamplingStrategy() {
        super();
    }

    @Override
    public MCTSNode<Risk, RiskAction> select(MCTSNode<Risk, RiskAction> root, MCTSTree<Risk, RiskAction> tree) {
        Risk risk = root.getState();
        Set<RiskAction> possibleActions = risk.getPossibleActions();
        this.armCount = possibleActions.size();
        this.successes = new int[this.armCount];
        this.failures = new int[this.armCount];
        double maxThompsonSample = Double.NEGATIVE_INFINITY;
        MCTSNode<Risk, RiskAction> selected = null;

        if (root.isLeaf()) return root;
        int actionIndex = 0;
        for (RiskAction action : possibleActions) {
            double sample = sampleBeta(successes[actionIndex], failures[actionIndex]);
            if (sample > maxThompsonSample) {
                maxThompsonSample = sample;
                MCTSNode<Risk, RiskAction> child = root.getChildForAction(action);
                if (child != null) {
                    selected = child;
                }
            }
            actionIndex++;
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
