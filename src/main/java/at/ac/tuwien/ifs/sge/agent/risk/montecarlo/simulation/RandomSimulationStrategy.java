package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import at.ac.tuwien.ifs.sge.util.pair.ImmutablePair;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;

public class RandomSimulationStrategy extends MCTSSimulationStrategy<Risk, RiskAction> {
    private static final SplittableRandom random = new SplittableRandom();

    public RandomSimulationStrategy() {
        super();
    }

    @Override
    public ImmutablePair<List<RiskAction>, Double> simulate(MCTSNode<Risk, RiskAction> node, long timeout, MCTSTree<Risk, RiskAction> tree) {
        Risk risk = node.getState();

        List<RiskAction> actionList = Lists.newArrayList();

        long startTime = System.nanoTime();
        while (!risk.isGameOver() && System.nanoTime() - startTime <= timeout) {
            // if the current player is -1, the game performs actions automatically
            if (risk.getCurrentPlayer() < 0) {
                risk = (Risk) risk.doAction();
                continue;
            }
            // select a random action
            RiskAction action = risk.getPossibleActions()
              .stream()
              .skip(random.nextInt(risk.getPossibleActions().size()))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("No possible actions."));
            risk = (Risk) risk.doAction(action);
            actionList.add(action);
        }
        double heuristic = risk.getHeuristicValue(node.getPlayerId()) / 42; // fraction of captured territories
        node.setUtility(heuristic);
        return new ImmutablePair<>(actionList, heuristic);
    }

}
