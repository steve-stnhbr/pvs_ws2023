package at.ac.tuwien.ifs.sge.agent.risk;

import at.ac.tuwien.ifs.sge.agent.AbstractGameAgent;
import at.ac.tuwien.ifs.sge.agent.GameAgent;
import at.ac.tuwien.ifs.sge.agent.mctsagent.MctsAgent;
import at.ac.tuwien.ifs.sge.agent.risk.util.DatasetWriter;
import at.ac.tuwien.ifs.sge.agent.risk.util.RiskHasher;
import at.ac.tuwien.ifs.sge.agent.risk.util.Triple;
import at.ac.tuwien.ifs.sge.engine.Logger;
import at.ac.tuwien.ifs.sge.engine.factory.AgentFactory;
import at.ac.tuwien.ifs.sge.engine.loader.AgentLoader;
import at.ac.tuwien.ifs.sge.game.Game;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskBoard;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PerformanceTest {
    private static final DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("HH:mm:ss");

    private final GameAgent<Risk, RiskAction> player1, player2;
    private Risk risk;

    private final DatasetWriter.CSV reflectionWriter, noReflectionWriter;

    public PerformanceTest(GameAgent<Risk, RiskAction> player1, GameAgent<Risk, RiskAction> player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.reflectionWriter = new DatasetWriter.CSV("out/data_reflection.csv");
        this.noReflectionWriter = new DatasetWriter.CSV("out/data_no_reflection.csv");
    }

    public void run() {
        risk = new Risk();
        player1.setUp(2, 0);
        player2.setUp(2, 1);

        List<Triple<String, String, Integer>> record = new ArrayList<>();
        int iterations = 0;
        while (!risk.isGameOver()) {
            RiskAction action = null;
            if (risk.getCurrentPlayer() == 0) {
                System.out.println("Player 1's turn, current Utility:" + risk.getUtilityValue(0));
                action = player1.computeNextAction(risk, 20000, TimeUnit.MILLISECONDS);
            } else if (risk.getCurrentPlayer() == 1) {
                System.out.println("Player 2's turn, current Utility:" + risk.getUtilityValue(1));
                action = player2.computeNextAction(risk, 20000, TimeUnit.MILLISECONDS);
            }
            System.out.println("Generated action: " + action);
            System.out.println(formatter.format(LocalTime.now()) + ": iteration #" + iterations + " done");
            if (action == null) {
                System.out.println("No action provided");
                risk = (Risk) risk.doAction();
                continue;
            }
            Risk newState = (Risk) risk.doAction(action);
            String state_str = RiskHasher.CSV.encodeGame(risk, 0);
            String actionEncoding = RiskHasher.CSV.encodeAction(action, risk);
            String noReflectionEncoding = RiskHasher.NoReflection.CSV.encodeGame(risk, 0);
            String noReflectionActionEncoding = RiskHasher.NoReflection.CSV.encodeAction(action);
            record.add(new Triple<>(String.join(",", state_str, actionEncoding), String.join(",", noReflectionEncoding, noReflectionActionEncoding), iterations));
            risk = newState;
            iterations++;
        }
        record.forEach(t -> reflectionWriter.appendToCSV(t.getA(), t.getC(), (float) risk.getUtilityValue(0)));
        record.forEach(t -> noReflectionWriter.appendToCSV(t.getB(), t.getC(), (float) risk.getUtilityValue(0)));

        player1.tearDown();
        player2.tearDown();

        System.out.println("Utility 1: " + risk.getUtilityValue(0));
        System.out.println("Utility 2: " + risk.getUtilityValue(1));
    }

    public static void main(String[] args) {
        PrintStream stream = new PrintStream(System.out);
        Logger log = new Logger(0, "", "", "", stream, "", "", stream, "", "", stream, "", "", stream, "", "", stream, "");

        GameAgent<Risk, RiskAction> player1 = new RiskItAgent(log);
        GameAgent<Risk, RiskAction> player2 = new MctsAgent<>(log);
        int numThreads = Runtime.getRuntime().availableProcessors() * 2;

        for (int i = 0; i < numThreads; i++) {
            Thread thread = new Thread(() -> {
                PerformanceTest sp = new PerformanceTest(player1, player2);
                while(!Thread.interrupted()) {
                    sp.run();
                }
            }, "PerformanceTest #" + i);
            thread.start();
        }
    }
}
