package at.ac.tuwien.ifs.sge.agent.risk;

import at.ac.tuwien.ifs.sge.agent.risk.util.DatasetWriter;
import at.ac.tuwien.ifs.sge.agent.risk.util.RiskHasher;
import at.ac.tuwien.ifs.sge.engine.Logger;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class SelfPlay {
    private final RiskItAgent player1, player2;
    private Risk risk;

    public SelfPlay() {
        PrintStream stream = new PrintStream(System.out);
        Logger log = new Logger(0, "", "", "", stream, "", "", stream, "", "", stream, "", "", stream, "", "", stream, "");
        this.player1 = new RiskItAgent(log);
        this.player2 = new RiskItAgent(log);
    }

    public void run() {
        risk = new Risk();

        while (!risk.isGameOver()) {
            RiskAction action = null;
            if (risk.getCurrentPlayer() == 0) {
                action = player1.computeNextAction(risk, 30000, TimeUnit.MILLISECONDS);
            } else if (risk.getCurrentPlayer() == 1) {
                action = player2.computeNextAction(risk, 30000, TimeUnit.MILLISECONDS);
            } else {
                risk.doAction();
                continue;
            }
            Risk newState = (Risk) risk.doAction(action);
            String state_str = RiskHasher.CSV.encodeGame(risk, 0);
            String actionEncoding = RiskHasher.CSV.encodeAction(action, risk);
            DatasetWriter.CSV.getInstance().appendToCSV(String.join(",", state_str, actionEncoding), .0f);
            risk = newState;
        }

        System.out.println("Utility 1: " + risk.getUtilityValue(0));
        System.out.println("Utility 2: " + risk.getUtilityValue(1));
    }

    public static void main(String[] args) {
        int numThreads = Runtime.getRuntime().availableProcessors() * 2;

        for (int i = 0; i < numThreads; i++) {
            Thread thread = new Thread(() -> {
                while(!Thread.interrupted()) {
                    new SelfPlay().run();
                }
            }, "SelfPlay #" + i);
            thread.start();
        }
    }
}
