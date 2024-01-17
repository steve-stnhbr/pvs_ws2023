package at.ac.tuwien.ifs.sge.agent.risk.montecarlo;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.backpropagation.MCTSBackpropagationStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion.MCTSExpansionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection.MCTSSelectionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation.MCTSSimulationStrategy;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import at.ac.tuwien.ifs.sge.util.pair.ImmutablePair;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MCTSTree<T, A> implements Iterable<MCTSNode<T, A>> {
    private MCTSNode<T, A> root;
    private final MCTSSelectionStrategy<T, A> selectionStrategy;
    private final MCTSExpansionStrategy<T, A> expansionStrategy;
    private final MCTSSimulationStrategy<T, A> simulationStrategy;
    private final MCTSBackpropagationStrategy<T, A> backpropagationStrategy;
    private final int playerId;

    private final Map<MCTSNode<Risk, RiskAction>, Integer> successes = new HashMap<>(); // needed only for taylor sampling
    private final Map<MCTSNode<Risk, RiskAction>, Integer> failures = new HashMap<>(); // needed only for taylor sampling


    public MCTSTree(T rootContent, MCTSSelectionStrategy<T, A> selectionStrategy, MCTSExpansionStrategy<T, A> expansionStrategy, MCTSSimulationStrategy<T, A> simulationStrategy, MCTSBackpropagationStrategy<T, A> backpropagationStrategy, int playerId) {
        this.selectionStrategy = selectionStrategy;
        this.expansionStrategy = expansionStrategy;
        this.simulationStrategy = simulationStrategy;
        this.backpropagationStrategy = backpropagationStrategy;
        this.root = new MCTSNode<>(rootContent, null, null, playerId);
        this.playerId = playerId;
    }

    public MCTSNode<T, A> getRoot() {
        return root;
    }

    public void simulate(int simulationSteps, long timeout) {
        long timePerStep = timeout / simulationSteps;
        // Selection Stage
        MCTSNode<T, A> node = selectionStrategy.select(root, this);
        // Expansion Stage
        if (node.getVisits() != 0) {
            node = expansionStrategy.expand(node);
        }
        // Simulation Stage
        ImmutablePair<List<A>, Double> simulated = simulationStrategy.simulate(node, timePerStep, this);
        List<A> actions = simulated.getA();
        double utility = simulated.getB();
        // Backpropagation Stage
        backpropagationStrategy.backpropagate(node, actions, utility, this);
    }

    /**
     * This method updates the successes and failures hashmaps for taylor sampling.
     * If the utility increases after an action it is deemed a success. If it decreases it is deemed a failure.
     *
     * @param toUpdate the Node to update the maps for
     * @param utilBefore the utility before the action (assumed to be 0.5 for the game start)
     * @param utilAfter the utility after the action
     */
    public void updateSucessesAndFailures(MCTSNode<Risk, RiskAction> toUpdate, double utilBefore, double utilAfter) {
        if (utilBefore >= utilAfter) {
            if (this.failures.containsKey(toUpdate)) {
                int failuresCountUpdate = this.failures.get(toUpdate) + 1;
                this.failures.put(toUpdate, failuresCountUpdate);
            } else {
                this.failures.put(toUpdate, 1);
            }
        } else {
            if (this.successes.containsKey(toUpdate)) {
                int successesCountUpdate = this.successes.get(toUpdate) + 1;
                this.successes.put(toUpdate, successesCountUpdate);
            } else {
                this.successes.put(toUpdate, 1);
            }
        }
    }

    public Map<MCTSNode<Risk, RiskAction>, Integer> getSuccesses() {
        return successes;
    }

    public Map<MCTSNode<Risk, RiskAction>, Integer> getFailures() {
        return failures;
    }

    /**
     * This method returns the best action according to the current tree. It will return the action accessible from the root node with the highest average utility.
     * @return The best action according to the current tree.
     */
    public A getBestAction() {
        return root.getChildren()
          .stream()
          .max(Comparator.comparingDouble(MCTSNode::getAverageUtility))
          .map(MCTSNode::getAction)
          .orElse(null);
    }

    public List<MCTSNode<Risk, RiskAction>> getAllNodesForState(Risk state, MCTSTree<Risk, RiskAction> tree) {
        List<MCTSNode<Risk, RiskAction>> listOfNodesForState = new LinkedList<MCTSNode<Risk, RiskAction>>();
        for (MCTSNode<Risk, RiskAction> node : tree){
            if (node.getState().equals(state)) {
                listOfNodesForState.add(node);
            }
        }
        return listOfNodesForState;
    }

    @Override
    public Iterator<T, A> iterator() {
        return new Iterator<>(root);
    }

    public Stream<MCTSNode<T, A>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public Spliterator<MCTSNode<T, A>> spliterator() {
        return new Spliterator<>() {
            @Override
            public boolean tryAdvance(java.util.function.Consumer<? super MCTSNode<T, A>> action) {
                if (root == null) {
                    return false;
                }
                action.accept(root);
                return true;
            }

            @Override
            public Spliterator<MCTSNode<T, A>> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.ORDERED;
            }
        };
    }

    public static class Iterator<T, A> implements java.util.Iterator<MCTSNode<T, A>> {
        private MCTSNode<T, A> current;

        public Iterator(MCTSNode<T, A> root) {
            this.current = root;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public MCTSNode<T, A> next() {
            // inorder traversal of the unbound tree
            MCTSNode<T, A> next = current;
            if (!current.getChildren().isEmpty()) {
                current = current.getChildren().get(0);
            } else {
                while (current.getParent() != null && current.getParent().getChildren().indexOf(current) == current.getParent().getChildren().size() - 1) {
                    current = current.getParent();
                }
                if (current.getParent() == null) {
                    current = null;
                } else {
                    current = current.getParent().getChildren().get(current.getParent().getChildren().indexOf(current) + 1);
                }
            }
            return next;
        }
    }
}
