package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.IOException;

public class MCTSDeepLearningExpansionStrategy extends MCTSExpansionStrategy<Risk, RiskAction> {
  private final MultiLayerNetwork valueNetwork;

  public MCTSDeepLearningExpansionStrategy(String fileName) {
    try {
      this.valueNetwork = KerasModelImport.
       importKerasSequentialModelAndWeights(fileName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InvalidKerasConfigurationException e) {
      throw new RuntimeException(e);
    } catch (UnsupportedKerasConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public MCTSNode<Risk, RiskAction> expand(MCTSNode<Risk, RiskAction> node) {
    return null;
  }
}
