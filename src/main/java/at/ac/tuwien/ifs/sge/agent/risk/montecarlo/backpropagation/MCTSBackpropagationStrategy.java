package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.backpropagation;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;

import java.util.List;

public abstract class MCTSBackpropagationStrategy<T, A> {

    public MCTSBackpropagationStrategy() {
    }

    /**
     * This method backpropagates the utility of a child node to the parent node.
     * @param child
     */
    public abstract void backpropagate(MCTSNode<T, A> leaf, List<A> actions);
}
