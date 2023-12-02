package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import com.google.common.collect.Lists;

import java.util.Arrays;

public class RandomSimulationStrategy extends MCTSSimulationStrategy<Risk, RiskAction> {
    public RandomSimulationStrategy() {
        super();
    }

    @Override
    public void simulate(MCTSNode<Risk, RiskAction> node, long timeout) {
        Risk risk = node.getState();

        long startTime = System.nanoTime();
        while (!risk.isGameOver() && System.nanoTime() - startTime <= timeout) {
            if (risk.getCurrentPlayer() < 0) {
                risk = (Risk) risk.doAction();
                continue;
            }
            // select a random action
            RiskAction action = risk.getPossibleActions()
              .stream()
              .skip((int) (Math.random() * risk.getPossibleActions().size()))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("No possible actions."));
            risk = (Risk) risk.doAction(action);
        }

        node.setUtility(risk.getPlayerUtilityWeight(node.getPlayerId()));
        System.out.println("Finished simulation with utility: " + node.getUtility());
    }

}
