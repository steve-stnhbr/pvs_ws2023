package at.ac.tuwien.ifs.sge.agent.risk.util;

import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskBoard;
import java.lang.reflect.Field;

public class RiskHasher {
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

    private static float hashField(String fieldName, Object value) {
      // You may implement a custom hash function based on the field name and value
      // Here, we use a simple hash function for demonstration purposes
      int hashCode = fieldName.hashCode() + value.hashCode();
      return (float) hashCode / Integer.MAX_VALUE; // Normalize to a float between 0 and 1
    }

    public static void main(String[] args) {
      Risk risk = new Risk();
      RiskBoard riskBoard = risk.getBoard();
      System.out.println(calculateDCNNKey(riskBoard));
    }
  }
