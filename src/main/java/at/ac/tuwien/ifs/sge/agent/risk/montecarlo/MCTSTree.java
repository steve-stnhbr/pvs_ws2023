package at.ac.tuwien.ifs.sge.agent.risk.montecarlo;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.backpropagation.MCTSBackpropagationStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion.MCTSExpansionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection.MCTSSelectionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation.MCTSSimulationStrategy;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import at.ac.tuwien.ifs.sge.util.pair.ImmutablePair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MCTSTree<T, A> implements Iterable<MCTSNode<T, A>> {
    private MCTSNode<T, A> root;
    private final MCTSSelectionStrategy<T, A> selectionStrategy;
    private final MCTSExpansionStrategy<T, A> expansionStrategy;
    private final MCTSSimulationStrategy<T, A> simulationStrategy;
    private final MCTSBackpropagationStrategy<T, A> backpropagationStrategy;
    private final int playerId;

    //private final Map<T, List<MCTSNode<T,A>>> stateToNodes = new HashMap<>();
    private final Map<Integer, List<MCTSNode<T,A>>> stateToNodes = new HashMap<>();

    public MCTSTree(T rootContent, MCTSSelectionStrategy<T, A> selectionStrategy, MCTSExpansionStrategy<T, A> expansionStrategy, MCTSSimulationStrategy<T, A> simulationStrategy, MCTSBackpropagationStrategy<T, A> backpropagationStrategy, int playerId) {
        this.selectionStrategy = selectionStrategy;
        this.expansionStrategy = expansionStrategy;
        this.simulationStrategy = simulationStrategy;
        this.backpropagationStrategy = backpropagationStrategy;
        this.root = new MCTSNode<>(rootContent, null, null, playerId, this);
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
     * This method returns the best action according to the current tree. It will return the action accessible from the root node with the highest average utility.
     * @return The best action according to the current tree.
     */
    public A getBestAction() {
        System.out.println("Root visits: " + root.getVisits());
        System.out.println("map: " + stateToNodes.values().stream().map(List::size).reduce(0, Integer::sum));
        System.out.println(stateToNodes);
        System.out.println("tree:" + this.stream().count());
        return root.getChildren()
          .stream()
          .max(Comparator.comparingDouble(MCTSNode::getAverageUtility))
          .map(MCTSNode::getAction)
          .orElse(root.getChildren().get(new Random().nextInt(root.getChildren().size())).getAction());
    }

    public List<MCTSNode<T, A>> getAllNodesForState(T state, MCTSTree<T, A> tree) {
        List<MCTSNode<T, A>> listOfNodesForState = tree.stream()
          .filter(node -> node.getState() == (state))
          .collect(Collectors.toList());
        return listOfNodesForState;
        // FIXME: for some reason the map does not work
        //return stateToNodes.getOrDefault(state, new ArrayList<>());
    }

    public void onAdd(MCTSNode<T, A> node) {
        // FIXME: if the map is used, uncomment this
        //stateToNodes.computeIfAbsent(node.getState().hashCode(), k -> new ArrayList<>()).add(node);
    }

    @Override
    public Iterator<T, A> iterator() {
        return new Iterator<>(root);
    }

    public Stream<MCTSNode<T, A>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public Spliterator<MCTSNode<T, A>> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED);
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
