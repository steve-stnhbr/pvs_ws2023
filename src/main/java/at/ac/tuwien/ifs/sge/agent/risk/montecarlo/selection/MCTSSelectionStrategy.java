package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;

public abstract class MCTSSelectionStrategy<T, A> {
    public MCTSSelectionStrategy() {
    }

    public abstract MCTSNode<T, A> select(MCTSNode<T, A> root);
}
