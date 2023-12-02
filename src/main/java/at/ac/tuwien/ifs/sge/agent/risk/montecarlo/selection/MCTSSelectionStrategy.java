package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;

public abstract class MCTSSelectionStrategy<T, A> {
    public MCTSSelectionStrategy() {
    }

    /**
     * This method selects a node from the tree. It is called once in the selection phase and is expected to return a node the simulation should be run on
     * @param root The root of the tree
     * @return The node to run the simulation on
     */
    public abstract MCTSNode<T, A> select(MCTSNode<T, A> root);
}
