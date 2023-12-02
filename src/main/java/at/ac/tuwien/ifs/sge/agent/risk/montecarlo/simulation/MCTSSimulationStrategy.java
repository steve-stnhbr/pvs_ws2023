package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;

public abstract class MCTSSimulationStrategy<T, A> {
  public MCTSSimulationStrategy() {
  }

    public abstract void simulate(MCTSNode<T, A> node, long timeout);
}
