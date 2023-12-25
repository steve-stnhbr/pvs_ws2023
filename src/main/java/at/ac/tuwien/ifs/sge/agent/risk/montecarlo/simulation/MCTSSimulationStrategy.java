package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;

import java.util.List;

public abstract class MCTSSimulationStrategy<T, A> {
  public MCTSSimulationStrategy() {
  }

  /**
   * This method runs the simulation from the given node. The simulation should run until the timeout is reached. During the simulation the visits and scores of nodes should be updated
   * to determine the best action.
   * @param node The node to run the simulation from
   * @param timeout The time in nanoseconds the simulation should run
   */
    public abstract List<A> simulate(MCTSNode<T, A> node, long timeout);
}
