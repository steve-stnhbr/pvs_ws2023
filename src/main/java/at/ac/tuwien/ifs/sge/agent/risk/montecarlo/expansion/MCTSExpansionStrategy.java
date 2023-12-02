package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;

/**
 * This class is used to expand a node in the MCTS algorithm.
 * @param <T> The type of the node.
 */
public abstract class MCTSExpansionStrategy<T, A> {

  /**
   * Expands the given node.
   * @param node The node to expand.
   * @return The selected of the expanded nodes.
   */
  public abstract MCTSNode<T, A> expand(MCTSNode<T, A> node);
}
