package at.ac.tuwien.ifs.sge.agent.risk.util;

import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import com.google.common.primitives.Floats;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DNNTest {
  private final MultiLayerNetwork valueNetwork, policyNetwork;

  public DNNTest(String valueNetworkFileName, String policyNetworkFileName) {
    try {
      String valueNetworkPath = new ClassPathResource(valueNetworkFileName).getFile().getPath();
      valueNetwork = KerasModelImport.importKerasSequentialModelAndWeights(valueNetworkPath);
      policyNetwork = null;
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InvalidKerasConfigurationException e) {
      throw new RuntimeException(e);
    } catch (UnsupportedKerasConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public float predictValue(Risk game, int playerID) {
    List<Double> encodedState = RiskHasher.Scalar.encodeGame(game, playerID);
    INDArray input = Nd4j.create(Floats.toArray(encodedState), encodedState.size());
    INDArray output = valueNetwork.output(input);
    return output.getFloat(0);
  }

  public RiskAction predictAction(Risk game, int playerID) {
    Map<RiskAction, float[]> possibleActions_enc = game.getPossibleActions()
      .stream()
      .map(action -> new Tuple<>(action, RiskHasher.Scalar.encodeAction(action, game)))
      .map(tuple -> new Tuple<>(tuple.getA(), Floats.toArray(tuple.getB())))
      .collect(Collectors.toMap(Tuple::getA, Tuple::getB));
    List<Double> encodedState = RiskHasher.Scalar.encodeGame(game, playerID);
    INDArray input = Nd4j.create(Floats.toArray(encodedState), encodedState.size());
    INDArray output = policyNetwork.output(input);
    float[] predicted = output.toFloatVector();
    return possibleActions_enc.entrySet()
      .stream()
      // finding lowest difference between
      .min((e1, e2) -> Floats.compare(meanSquaredError(e1.getValue(), predicted), meanSquaredError(e2.getValue(), predicted)))
      .map(Map.Entry::getKey)
      .orElseThrow();
  }

  private float meanSquaredError(float[] array1, float[] array2) {
      int length = array1.length;
      float sumSquaredDifferences = 0;

      for (int i = 0; i < length; i++) {
        float difference = array1[i] - array2[i];
        sumSquaredDifferences += difference * difference;
      }

      return sumSquaredDifferences / length;
  }

  public static void main(String[] args) {
    DNNTest test = new DNNTest("value_net.h5", "value_net.h5");
    Risk risk = new Risk();
    System.out.println(test.predictValue(risk, 0));
  }
}
