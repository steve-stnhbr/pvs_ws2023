package at.ac.tuwien.ifs.sge.agent.risk.montecarlo;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion.MCTSExpansionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection.MCTSSelectionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation.MCTSSimulationStrategy;

import java.util.Comparator;

public class MCTSTree<T, A> {
    private MCTSNode<T, A> root;
    private final MCTSSelectionStrategy<T, A> selectionStrategy;
    private final MCTSExpansionStrategy<T, A> expansionStrategy;
    private final MCTSSimulationStrategy<T, A> simulationStrategy;
    private final int playerId;

    public MCTSTree(T rootContent, MCTSSelectionStrategy<T, A> selectionStrategy, MCTSExpansionStrategy<T, A> expansionStrategy, MCTSSimulationStrategy<T, A> simulationStrategy, int playerId) {
        this.selectionStrategy = selectionStrategy;
        this.expansionStrategy = expansionStrategy;
        this.simulationStrategy = simulationStrategy;
        this.root = new MCTSNode<>(rootContent, null, null, playerId);
        this.playerId = playerId;
    }

    public MCTSNode<T, A> getRoot() {
        return root;
    }

    public void setRoot(MCTSNode<T, A> root) {
        this.root = root;
    }

    public void simulate(int simulationSteps, long timeout) {
        // Selection Stage
        MCTSNode<T, A> node = selectionStrategy.select(root);
        System.out.println("Selected node: " + node);
        // Expansion Stage
        if (node.getVisits() != 0) {
            node = expansionStrategy.expand(node);
            System.out.println("Expanded node: " + node);
        }
        // Simulation Stage
        simulationStrategy.simulate(node, timeout / simulationSteps);
        System.out.println("Simulated node: " + node);
        // Backpropagation Stage
        node.backpropagate(null);
        System.out.println("ROOT: " + root);
    }

    public A getBestAction() {
        return root.getChildren()
          .stream()
          .max(Comparator.comparingDouble(MCTSNode::getAverageUtility))
          .map(MCTSNode::getAction)
          .orElse(null);
    }

}
