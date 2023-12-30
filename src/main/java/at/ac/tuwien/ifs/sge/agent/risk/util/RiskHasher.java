package at.ac.tuwien.ifs.sge.agent.risk.util;

import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskBoard;
import com.google.flatbuffers.FlatBufferBuilder;
import org.checkerframework.checker.units.qual.K;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RiskHasher {
  private static final String[] SETTINGS_FIELD_NAMES = {
    "numberOfPlayers",
    "tradeInBonus",
    "tradeInTerritoryBonus",
    "maxExtraBonus",
    "cardTypesWithoutJoker",
    "reinforcementAtLeast",
    "reinforcementThreshold",
    "occupyOnlyWithAttackingArmies",
    "fortifyOnlyFromSingleTerritory",
    "fortifyOnlyWithNonFightingArmies",
    "withMissions"
  }, BOARD_FIELD_NAMES = {
    "gameBoard",
    "territories",
    "fortifyConnectivityGraph",
    "fortifyConnectivityInspector",
    "deckOfCards",
    "discardPile",
    "allMissions",
    "playerMissions",
    "playerCards",
    "continents",
    "nonDeployedReinforcements",
    "reinforcedTerritories",
    "involvedTroopsInAttacks",
    "map",
    "tradeInTerritories",
    "minMatchingTerritories",
    "maxMatchingTerritories",
    "tradeIns",
    "attackingId",
    "defendingId",
    "troops",
  }, STATE_FIELD_NAMES = {
    "tradedInId",
    "hasOccupiedCountry",
    "phase",
    "initialSelectMaybe",
    "initialReinforceMaybe"
  };

  public static final String[] FIELD_NAMES = concatWithArrayCopy(SETTINGS_FIELD_NAMES, concatWithArrayCopy(BOARD_FIELD_NAMES, STATE_FIELD_NAMES));

    public static float calculateDCNNKey(RiskBoard riskBoard) {
      float result = 0.0f;

      Class<?> riskBoardClass = RiskBoard.class;
      Field[] fields = riskBoardClass.getDeclaredFields();

      for (Field field : fields) {
        field.setAccessible(true);

        try {
          Object value = field.get(riskBoard);
          if (value != null) {
            result += hashField(field.getName(), value);
          }
        } catch (IllegalAccessException e) {
          e.printStackTrace(); // Handle the exception as per your needs
        }
      }

      return result;
    }

  public static float[] calculateDCNNFeatures(RiskBoard riskBoard) {
    Class<?> riskBoardClass = RiskBoard.class;
    Field[] fields = riskBoardClass.getDeclaredFields();

    float[] result = new float[fields.length];
    int i = 0;

    for (Field field : fields) {
      field.setAccessible(true);

      try {
        Object value = field.get(riskBoard);
        if (value != null) {
          result[i++] = hashField(field.getName(), value);
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace(); // Handle the exception as per your needs
      }
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getFieldValue(Object obj, String fieldName) {
    Class<?> riskBoardClass = obj.getClass();
    try {
      Field field = riskBoardClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      return (T) field.get(obj);
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException("Field " + fieldName + " not found in class " + riskBoardClass.getName());
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Field " + fieldName + " in class " + riskBoardClass.getName() + " not accessible");
    }
  }
    private static float hashField(String fieldName, Object value) {
      // You may implement a custom hash function based on the field name and value
      // Here, we use a simple hash function for demonstration purposes
      int hashCode = fieldName.hashCode() + value.hashCode();
      return (float) hashCode / Integer.MAX_VALUE; // Normalize to a float between 0 and 1
    }

  public static <K, V> INDArray encodeMap(Map<K, V> map, int maxKey) {
    // Adjust the shape based on the number of possible keys
    int[] shape = {1, maxKey * 2};
    INDArray mapEncoding = Nd4j.zeros(shape);

    for (Map.Entry<K, V> entry : map.entrySet()) {
      int key = entry.getKey().hashCode();
      int value = entry.getValue().hashCode();

      // Encode key presence
      mapEncoding.putScalar(0, key, 1);

      // Encode associated value
      mapEncoding.putScalar(0, maxKey + key, value);
    }

    return mapEncoding;
  }

  public static <T> INDArray encodeList(List<T> list) {
    // Adjust the shape based on the number of possible keys
    int[] shape = {1, list.size()};
    INDArray listEncoding = Nd4j.zeros(shape);

    for (int i = 0; i < list.size(); i++) {
      T element = list.get(i);
      listEncoding.put(0, i, encodeElement(element));
    }

    return listEncoding;
  }

  public static <V,E> INDArray encodeGraph(Graph<V, DefaultEdge> graph) {
    int numVertices = graph.vertexSet().size();
    int[] shape = {numVertices, numVertices};
    INDArray adjacencyMatrix = Nd4j.zeros(shape);

    for (DefaultEdge edge : graph.edgeSet()) {
      int sourceVertex = graph.getEdgeSource(edge).hashCode();
      int targetVertex = graph.getEdgeTarget(edge).hashCode();
      adjacencyMatrix.putScalar(sourceVertex, targetVertex, 1);
      adjacencyMatrix.putScalar(targetVertex, sourceVertex, 1);
    }

    return adjacencyMatrix;
  }

  @SuppressWarnings("unchecked")
  public static INDArray encodeBoard(RiskBoard board) {
    long[] shape = {1, FIELD_NAMES.length};
    try (INDArray stateTensor = Nd4j.zeros(shape)) {
      for (int i = 0; i < FIELD_NAMES.length; i++) {
          String fieldName = FIELD_NAMES[i];
          Object fieldValue = getFieldValue(board, fieldName);
          var encoding = encodeElement(fieldValue);
          var encodingShape = encoding.shape();
          if (encodingShape.length > 0) {
            shape = stateTensor.shape();
            var newShape = new long[shape.length + encodingShape.length];
            System.arraycopy(shape, 0, newShape, 0, shape.length);
            System.arraycopy(encodingShape, 0, newShape, shape.length, encodingShape.length);
            System.out.printf("Trying to reshape:%n\told: %s%n\tencoding: %s%n\tnew: %s%n", Arrays.toString(shape), Arrays.toString(encodingShape), Arrays.toString(newShape));
            stateTensor.reshape(newShape);
          }
          stateTensor.put(i, encoding);
      }
        return stateTensor;
    }
  }

  @SuppressWarnings("unchecked")
  public static INDArray encodeElement(Object element) {
    if (element instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) element;
      return encodeMap(map, 100);
    } else if (element instanceof List) {
      List<?> list = (List<?>) element;
      return encodeList(list);
    } else if (element instanceof Graph) {
      Graph<?, DefaultEdge> graph = (Graph<?, DefaultEdge>) element;
      return encodeGraph(graph);
    } else {
      return Nd4j.scalar(element.hashCode());
    }
  }

  static <T> T[] concatWithArrayCopy(T[] array1, T[] array2) {
    T[] result = Arrays.copyOf(array1, array1.length + array2.length);
    System.arraycopy(array2, 0, result, array1.length, array2.length);
    return result;
  }


  public static void main(String[] args) {
      Risk risk = new Risk();
      RiskBoard riskBoard = risk.getBoard();
      var encoding = Tensor.encodeBoard(riskBoard);
      System.out.println("shape: " + Arrays.toString(encoding.shape()) + "Encoding: " + encoding);
    }

  public static class Tensor {

    public static INDArray encodeBoard(RiskBoard instance) {
      List<INDArray> tensors = new ArrayList<>();

      for (String fieldName : FIELD_NAMES) {
        try {
          Object fieldValue = getFieldValue(instance, fieldName);
          INDArray encoded = encodeObject(fieldValue);
          tensors.add(encoded);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      return aggregate(tensors);
    }

    public static INDArray encodeObject(Object object) {
      if (object instanceof Collection) {
        Collection<?> collection = (Collection<?>) object;
        List<INDArray> tensors = new ArrayList<>();
        for (Object element : collection) {
          tensors.add(encodeObject(element));
        }
        return aggregate(tensors);
      } else if (object instanceof Map) {
        Map<?, ?> map = (Map<?, ?>) object;
        List<INDArray> tensors = new ArrayList<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
          tensors.add(encodeObject(entry.getKey()));
          tensors.add(encodeObject(entry.getValue()));
        }
        return aggregate(tensors);
      } else {
        return Nd4j.scalar(object == null ? 0 : object.hashCode());
      }
    }

  }

  private static INDArray aggregate(List<INDArray> tensors) {
      return aggregate(tensors.toArray(new INDArray[0]));
  }

  private static INDArray aggregate(INDArray... tensors) {
    if (tensors.length == 0) {
      return Nd4j.scalar(0);
    }
    return Nd4j.concat(0, tensors);
  }

}
