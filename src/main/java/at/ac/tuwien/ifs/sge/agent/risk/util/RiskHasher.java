package at.ac.tuwien.ifs.sge.agent.risk.util;

import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskBoard;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskCard;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskTerritory;
import at.ac.tuwien.ifs.sge.game.risk.mission.RiskMission;
import org.bytedeco.opencv.presets.opencv_core;
import org.datavec.api.writable.Text;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static at.ac.tuwien.ifs.sge.agent.risk.util.RiskHasher.Version.V1;
import static at.ac.tuwien.ifs.sge.agent.risk.util.RiskHasher.Version.V2;

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
    //"gameBoard",
    "territories",
    //"fortifyConnectivityGraph",
    //"fortifyConnectivityInspector",
    //"deckOfCards",
    //"discardPile",
    //"allMissions",
    //"playerMissions",
    "playerCards",
    //"continents",
    "nonDeployedReinforcements",
    "reinforcedTerritories",
    "involvedTroopsInAttacks",
    //"map",
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

  public static final String[] FIELD_NAMES = concatWithArrayCopy(BOARD_FIELD_NAMES, STATE_FIELD_NAMES);

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

  public static Field getField(Object obj, String fieldName) {
    Class<?> riskBoardClass = obj.getClass();
    try {
      Field field = riskBoardClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field;
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException("Field " + fieldName + " not found in class " + riskBoardClass.getName());
    }
  }

  public static <T> List<T> convertArrayToList(Object array) {
    if (array == null) {
      return null;
    }
    List<T> result = new ArrayList<>();
    int length = Array.getLength(array);
    for (int i = 0; i < length; i++) {
      T element = (T) Array.get(array, i);
      result.add(element);
    }
    return result;
  }

  private static float hashField(String fieldName, Object value) {
    // You may implement a custom hash function based on the field name and value
    // Here, we use a simple hash function for demonstration purposes
    int hashCode = fieldName.hashCode() + value.hashCode();
    return (float) hashCode / Integer.MAX_VALUE; // Normalize to a float between 0 and 1
  }

  static <T> T[] concatWithArrayCopy(T[] array1, T[] array2) {
    T[] result = Arrays.copyOf(array1, array1.length + array2.length);
    System.arraycopy(array2, 0, result, array1.length, array2.length);
    return result;
  }

  public static <T> T invoke(Object obj, String methodName, Object... args) {
    Class<?> riskBoardClass = obj.getClass();
    try {
      Class<?>[] argTypes = new Class<?>[args.length];
      for (int i = 0; i < args.length; i++) {
        argTypes[i] = args[i].getClass();
      }

      Method method = riskBoardClass.getDeclaredMethod(methodName, argTypes);
      method.setAccessible(true);
      return (T) method.invoke(obj, args);
    } catch (Exception e) {
      throw new IllegalStateException("Method " + methodName + " not found in class " + riskBoardClass.getName());
    }
  }


  public static void main(String[] args) {
    Risk risk = new Risk();
    try {
      PrintWriter writer = new PrintWriter(new FileWriter("out/test.csv"));
      var encoding = CSV.encodeGame(risk, 0);
      writer.println(CSV.generateHeader());
      writer.println(encoding);
      System.out.println("Encoding: " + encoding);
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public enum Version {
    V1,
    V2
  }

  public static class CSV {
    public static Version version = V1;
    public static String generateHeader() {
      String s = "isCurrentPlayer,";
      List<String> fields = new ArrayList<>(List.of(FIELD_NAMES));
      fields.add("action");
      fields.add("values");
      return s + String.join(",", fields);
    }


    public static String encodeGame(Risk risk, int playerID) {
      StringBuilder sb = new StringBuilder();
      sb.append(risk.getCurrentPlayer() == playerID ? "1" : "0");
      sb.append(",");
      sb.append(encodeBoard(risk.getBoard(), playerID));
      return sb.toString();
    }

    public static String encodeBoard(RiskBoard instance, int playerID) {
      List<String> fields = new ArrayList<>();
      for (String fieldName : FIELD_NAMES) {
        try {
          switch (fieldName) {
            case "playerCards":
              int cardAmount = 44;
              if (version == V1) {
                int[] playerCards = new int[cardAmount];
                Map<Integer, List<RiskCard>> cards = getFieldValue(instance, fieldName);
                List<RiskCard> cardsList = cards.get(playerID);
                for (RiskCard riskCard : cardsList) {
                  if (riskCard.getCardType() < 1) {
                    playerCards[42 + riskCard.getTerritoryId() + 1] = 1;
                    continue;
                  }
                  playerCards[riskCard.getTerritoryId()] = 1;
                }

                fields.add(Arrays
                  .stream(playerCards)
                  .mapToObj(String::valueOf)
                  .collect(Collectors.joining(":")));
              } else if (version == V2) {
                int[] playerCards = new int[cardAmount * 2];
                Map<Integer, List<RiskCard>> cards = getFieldValue(instance, fieldName);
                for(int i = 0; i < 2; i++) {
                  List<RiskCard> cardsList = cards.get(i);
                  for (RiskCard riskCard : cardsList) {
                    if (riskCard.getCardType() < 1) {
                      playerCards[i * 44 + 42 + riskCard.getTerritoryId() + 1] = 1;
                      continue;
                    }
                    playerCards[i * 44 + riskCard.getTerritoryId()] = 1;
                  }
                }

                fields.add(Arrays
                  .stream(playerCards)
                  .mapToObj(String::valueOf)
                  .collect(Collectors.joining(":")));
              }

              continue;
            case "phase":
              Class<?> enumElement = Class.forName("at.ac.tuwien.ifs.sge.game.risk.board.RiskBoard$RiskPhase");
              enumElement.getClassLoader().loadClass(enumElement.getName());
              Object[] enumElements = enumElement.getEnumConstants();
              Object phase = getFieldValue(instance, fieldName);
              fields.add(Arrays
                .stream(enumElements)
                .map(el -> el.equals(phase) ? "1" : "0")
                .collect(Collectors.joining(":")));
              continue;
            case "territories":
              Map<Integer, RiskTerritory> territories = getFieldValue(instance, "territories");
              if (version == V1) {
                fields.add(territories.values()
                  .stream()
                  .map(t -> (t.getOccupantPlayerId() == playerID ? 1 : 0) + ":" + t.getTroops())
                  .collect(Collectors.joining(";"))
                );
              } else if (version == V2) {
                fields.add(territories.values()
                  .stream()
                  .map(t -> (t.getOccupantPlayerId() == playerID ? 1 : -1) * + t.getTroops())
                  .map(String::valueOf)
                  .collect(Collectors.joining(":"))
                );
              }
              continue;
            case "involvedTroopsInAttacks":
              Map<Integer, Integer> attacks = getFieldValue(instance, "involvedTroopsInAttacks");
              fields.add(IntStream.range(0, 42)
                .map(i -> attacks.getOrDefault(i, 0))
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(":"))
              );
              continue;
            case "nonDeployedReinforcements":
              List<Integer> ndr = convertArrayToList(getFieldValue(instance, "nonDeployedReinforcements"));
              List<String> list = new ArrayList<>();
              if (version == V1) {
                list.add(ndr.get(playerID).toString());
              } else {
                list.add(ndr.get(playerID).toString());
                list.addAll(IntStream.range(0, ndr.size())
                  .filter(i-> i != playerID)
                  .mapToObj(i -> -1 * ndr.get(i))
                  .map(String::valueOf)
                  .collect(Collectors.toList())
                );
              }
              fields.add(String.join(":", list));
              continue;
            case "reinforcedTerritories":
              Set<Integer> reinforced = getFieldValue(instance, "reinforcedTerritories");
              fields.add(IntStream.range(0, 42)
                .mapToObj(i -> reinforced.contains(i) ? "1" : "0")
                .collect(Collectors.joining(":"))
              );
              continue;
            case "tradeInTerritories":
              Set<Integer> trade = getFieldValue(instance, "tradeInTerritories");
              fields.add(IntStream.range(0, 42)
                .mapToObj(i -> trade.contains(i) ? "1" : "0")
                .collect(Collectors.joining(":"))
              );
              continue;
          }
          Field field = getField(instance, fieldName);
          Object value = field.get(instance);
          if (field.getType().isArray()) {
            value = convertArrayToList(value);
          }
          String encoded = encodeObject(value);
          fields.add(encoded);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      return String.join(",", fields);
    }

    public static String encodeAction(RiskAction action, Risk risk) {
      RiskBoard board = risk.getBoard();
      int actionTypes = 7;
      int[] actionEncoding = new int[actionTypes + 42 * 2 + 1];
      int lastIndex = actionEncoding.length - 1;

      if (risk.getCurrentPlayer() >= 0) {
        if (RiskHasher.<Boolean>invoke(risk, "isInitialSelect")) {
          // Code for isInitialSelect
          actionEncoding[0] = 1;
          actionEncoding[actionTypes + action.selected()] = 1;
        } else if (RiskHasher.<Boolean>invoke(risk, "isInitialReinforce")) {
          // Code for isInitialReinforce
          actionEncoding[1] = 1;
          actionEncoding[actionTypes + action.reinforcedId()] = 1;
          actionEncoding[lastIndex] = 1;
        } else if (board.hasToTradeInCards(risk.getCurrentPlayer())) {
          // Code for hasToTradeInCards
          actionEncoding[2] = 1;
          actionEncoding[lastIndex] = action.getBonus();
        } else if (RiskHasher.<Boolean>invoke(board, "isReinforcementPhase")) {
          // Code for isReinforcementPhase
          actionEncoding[3] = 1;
          actionEncoding[actionTypes + action.reinforcedId()] = 1;
          actionEncoding[lastIndex] = action.getBonus();
        } else if (RiskHasher.<Boolean>invoke(board, "isAttackPhase")) {
          // Code for isAttackPhase
          actionEncoding[4] = 1;
          actionEncoding[actionTypes + action.attackingId()] = 1;
          actionEncoding[actionTypes + 42 + action.defendingId()] = 1;
          actionEncoding[lastIndex] = action.getBonus();
        } else if (RiskHasher.<Boolean>invoke(board, "isOccupyPhase")) {
          // Code for isOccupyPhase
          actionEncoding[5] = 1;
          actionEncoding[actionTypes + RiskHasher.<Integer>getFieldValue(board, "attackingId")] = 1;
          actionEncoding[actionTypes + RiskHasher.<Integer>getFieldValue(board, "defendingId")] = 1;
          actionEncoding[lastIndex] = action.getBonus();
        } else if (RiskHasher.<Boolean>invoke(board, "isFortifyPhase")) {
          // Code for isFortifyPhase
          actionEncoding[6] = 1;
          actionEncoding[actionTypes + action.attackingId()] = 1;
          actionEncoding[actionTypes + 42 + action.defendingId()] = 1;
          actionEncoding[lastIndex] = action.getBonus();
        }
      }
      return Arrays.stream(actionEncoding)
        .mapToObj(String::valueOf)
        .collect(Collectors.joining(":"));
    }


    public static String encodeObject(Object object) {
      if (object instanceof Collection) {
        Collection<?> collection = (Collection<?>) object;
        StringBuilder sb = new StringBuilder();
        for (Object element : collection) {
          sb.append(encodeObject(element));
          sb.append(";");
        }
        return sb.toString();
      } else if (object instanceof Map) {
        Map<?, ?> map = (Map<?, ?>) object;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
          sb.append(encodeObject(entry.getKey()));
          sb.append(":");
          sb.append(encodeObject(entry.getValue()));
          sb.append(";");
        }
        return sb.toString();
      } else if (object instanceof RiskTerritory) {
        RiskTerritory territory = (RiskTerritory) object;
        int occupantPlayerId = getFieldValue(territory, "occupantPlayerId");
        int troops = getFieldValue(territory, "troops");
        return String.format("%s:%s", occupantPlayerId, troops);
      } else if (object instanceof RiskCard) {
        RiskCard card = (RiskCard) object;
        int cardTypes = 4;
        if (version == V1) {
          return String.format("%s:%s", card.getCardType(), card.getTerritoryId());
        } else if (version == V2) {
          List<Integer> list = new ArrayList<>();
          list.addAll(IntStream.range(0, cardTypes)
            .map(i -> card.getCardType() == i ? 1 : 0)
            .boxed()
            .collect(Collectors.toList()));
          list.addAll(IntStream.range(0, 42)
            .map(i -> card.getTerritoryId() == i ? 1 : 0)
            .boxed()
            .collect(Collectors.toList()));
          return list.stream().map(String::valueOf).collect(Collectors.joining(":"));
        }
      } else if (object instanceof RiskMission) {
        RiskMission mission = (RiskMission) object;
        if (version == V1) {
          List<String> list = new ArrayList<>();
          list.add(encodeObject(mission.getTargetIds()));
          list.add(String.valueOf(mission.getRiskMissionType().ordinal()));
          list.add(String.valueOf(mission.getOccupyingWith()));
          return String.join(":", list);
        } else if (version == V2) {
          List<Integer> list = IntStream.range(0, 42)
            .map(i -> mission.getTargetIds().contains(i) ? 1 : 0)
            .boxed().collect(Collectors.toList());
          list.add((mission.getRiskMissionType().ordinal()));
          list.add((mission.getOccupyingWith()));
          return list.stream().map(String::valueOf).collect(Collectors.joining(":"));
        }
        return String.format("%s;%s:%s", encodeObject(mission.getTargetIds()), mission.getRiskMissionType().ordinal(), mission.getOccupyingWith());
      } else if (object instanceof Boolean) {
        Boolean bool = (Boolean) object;
        return bool ? "1" : "0";
      } else {
        return object == null ? "0" : String.valueOf(object.hashCode());
      }
      return "";
    }

  }

  public static class Scalar {
    public static Version version = V1;

    public static List<Double> encodeGame(Risk risk, int playerID) {
      List<Double> list = new ArrayList<>();
      list.add(risk.getCurrentPlayer() == playerID ? 1d : 0d);
      list.addAll(encodeBoard(risk.getBoard(), playerID));
      return list;
    }

    public static List<Double> encodeBoard(RiskBoard instance, int playerID) {
      List<Double> list = new ArrayList<>();
      for (String fieldName : FIELD_NAMES) {

        try {
          switch (fieldName) {
            case "playerCards":
              int cardAmount = 44;
              int[] playerCards = new int[cardAmount];
              Map<Integer, List<RiskCard>> cards = getFieldValue(instance, fieldName);
              List<RiskCard> cardsList = cards.get(playerID);
              for (RiskCard riskCard : cardsList) {
                if (riskCard.getCardType() < 1) {
                  playerCards[42 + riskCard.getTerritoryId() + 1] = 1;
                  continue;
                }
                playerCards[riskCard.getTerritoryId()] = 1;
              }

              list.addAll(Arrays.stream(playerCards).asDoubleStream().boxed().collect(Collectors.toList()));

              continue;
            case "phase":
              Class<?> enumElement = Class.forName("at.ac.tuwien.ifs.sge.game.risk.board.RiskBoard$RiskPhase");
              enumElement.getClassLoader().loadClass(enumElement.getName());
              Object[] enumElements = enumElement.getEnumConstants();
              Object phase = getFieldValue(instance, fieldName);
              list.addAll(Arrays
                .stream(enumElements)
                .map(el -> el.equals(phase) ? 1d : 0d)
                .collect(Collectors.toList()));
              continue;
            case "territories":
              Map<Integer, RiskTerritory> territories = getFieldValue(instance, "territories");
              if (version == V1) {
                list.addAll(territories.values()
                  .stream()
                  .map(t -> (t.getOccupantPlayerId() == playerID ? t.getTroops() : -t.getTroops()))
                  .map(Integer::doubleValue)
                  .collect(Collectors.toList())
                );
              } else if (version == V2) {
                list.addAll(territories.values()
                  .stream()
                  .map(t -> (t.getOccupantPlayerId() == playerID ? t.getTroops() : -t.getTroops()))
                  .map(Integer::doubleValue)
                  .collect(Collectors.toList())
                );
              }
              continue;
            case "involvedTroopsInAttacks":
              Map<Integer, Integer> attacks = getFieldValue(instance, "involvedTroopsInAttacks");
              list.addAll(IntStream.range(0, 42)
                .map(i -> attacks.getOrDefault(i, 0))
                .boxed()
                .map(Integer::doubleValue)
                .collect(Collectors.toList())
              );
              continue;
            case "nonDeployedReinforcements":
              List<Integer> ndr = convertArrayToList(getFieldValue(instance, "nonDeployedReinforcements"));
              if (version == V1) {
                list.add(ndr.get(playerID).doubleValue());
              } else if (version == V2) {
                list.add(ndr.get(playerID).doubleValue());
                list.addAll(IntStream.range(0, ndr.size())
                    .filter(i-> i != playerID)
                    .mapToObj(i -> -1 * ndr.get(i).doubleValue())
                    .collect(Collectors.toList())
                  );
              }
              continue;
            case "reinforcedTerritories":
              Set<Integer> reinforced = getFieldValue(instance, "reinforcedTerritories");
              list.addAll(IntStream.range(0, 42)
                .mapToObj(i -> reinforced.contains(i) ? 1d : 0d)
                .collect(Collectors.toList())
              );
              continue;
            case "tradeInTerritories":
              Set<Integer> trade = getFieldValue(instance, "tradeInTerritories");
              list.addAll(IntStream.range(0, 42)
                .mapToObj(i -> trade.contains(i) ? 1d : 0d)
                .collect(Collectors.toList())
              );
              continue;
          }
          Field field = getField(instance, fieldName);
          Object value = field.get(instance);
          if (field.getType().isArray()) {
            value = convertArrayToList(value);
          }
          List<Double> encoded = encodeObject(value);
          list.addAll(encoded);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      return list;
    }

    public static List<Double> encodeAction(RiskAction action, Risk risk) {
      RiskBoard board = risk.getBoard();
      int actionTypes = 7;
      int[] actionEncoding = new int[actionTypes + 42 * 2 + 1];
      int lastIndex = actionEncoding.length - 1;

      if (risk.getCurrentPlayer() >= 0) {
        if (RiskHasher.<Boolean>invoke(risk, "isInitialSelect")) {
          // Code for isInitialSelect
          actionEncoding[0] = 1;
          actionEncoding[actionTypes + action.selected()] = 1;
        } else if (RiskHasher.<Boolean>invoke(risk, "isInitialReinforce")) {
          // Code for isInitialReinforce
          actionEncoding[1] = 1;
          actionEncoding[actionTypes + action.reinforcedId()] = 1;
          actionEncoding[lastIndex] = 1;
        } else if (board.hasToTradeInCards(risk.getCurrentPlayer())) {
          // Code for hasToTradeInCards
          actionEncoding[2] = 1;
          actionEncoding[lastIndex] = action.getBonus();
        } else if (RiskHasher.<Boolean>invoke(board, "isReinforcementPhase")) {
          // Code for isReinforcementPhase
          actionEncoding[3] = 1;
          actionEncoding[actionTypes + action.reinforcedId()] = 1;
          actionEncoding[lastIndex] = action.getBonus();
        } else if (RiskHasher.<Boolean>invoke(board, "isAttackPhase")) {
          // Code for isAttackPhase
          actionEncoding[4] = 1;
          actionEncoding[actionTypes + action.attackingId()] = 1;
          actionEncoding[actionTypes + 42 + action.defendingId()] = 1;
          actionEncoding[lastIndex] = action.getBonus();
        } else if (RiskHasher.<Boolean>invoke(board, "isOccupyPhase")) {
          // Code for isOccupyPhase
          actionEncoding[5] = 1;
          actionEncoding[actionTypes + RiskHasher.<Integer>getFieldValue(board, "attackingId")] = 1;
          actionEncoding[actionTypes + RiskHasher.<Integer>getFieldValue(board, "defendingId")] = 1;
          actionEncoding[lastIndex] = action.getBonus();
        } else if (RiskHasher.<Boolean>invoke(board, "isFortifyPhase")) {
          // Code for isFortifyPhase
          actionEncoding[6] = 1;
          actionEncoding[actionTypes + action.attackingId()] = 1;
          actionEncoding[actionTypes + 42 + action.defendingId()] = 1;
          actionEncoding[lastIndex] = action.getBonus();
        }
      }
      return Arrays.stream(actionEncoding)
        .mapToObj(Double::valueOf)
        .collect(Collectors.toList());
    }


    public static List<Double> encodeObject(Object object) {
      List<Double> list = new ArrayList<>();
      if (object instanceof Collection) {
        Collection<?> collection = (Collection<?>) object;
        for (Object element : collection) {
          list.addAll(encodeObject(element));
        }
        return list;
      } else if (object instanceof Map) {
        Map<?, ?> map = (Map<?, ?>) object;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
          list.addAll(encodeObject(entry.getKey()));
          list.addAll(encodeObject(entry.getValue()));
        }
        return list;
      } else if (object instanceof RiskTerritory) {
        RiskTerritory territory = (RiskTerritory) object;
        double occupantPlayerId = getFieldValue(territory, "occupantPlayerId");
        double troops = getFieldValue(territory, "troops");
        return List.of(occupantPlayerId, troops);
      } else if (object instanceof RiskCard) {
        RiskCard card = (RiskCard) object;
        int cardTypes = 4;
        if (version == V1) {
          return List.of((double) card.getCardType(), (double) card.getTerritoryId());
        } else if (version == V2) {
          list.addAll(IntStream.range(0, cardTypes)
            .map(i -> card.getCardType() == i ? 1 : 0)
            .boxed()
            .map(Integer::doubleValue)
            .collect(Collectors.toList()));
          list.addAll(IntStream.range(0, 42)
            .map(i -> card.getTerritoryId() == i ? 1 : 0)
            .boxed()
            .map(Integer::doubleValue)
            .collect(Collectors.toList()));
        }
        return List.of();
      } else if (object instanceof RiskMission) {
        RiskMission mission = (RiskMission) object;
        if (version == V1) {
          list.addAll(encodeObject(mission.getTargetIds()));
          list.add((double) mission.getRiskMissionType().ordinal());
          list.add((double) mission.getOccupyingWith());
          return list;
        } else if (version == V2) {
          list.addAll(IntStream.range(0, 42)
            .map(i -> mission.getTargetIds().contains(i) ? 1 : 0)
            .boxed()
            .map(Double::valueOf)
            .collect(Collectors.toList()));
          list.add((double) mission.getRiskMissionType().ordinal());
          list.add((double) mission.getOccupyingWith());
          return list;
        }
        return List.of();
      } else if (object instanceof Boolean) {
        Boolean bool = (Boolean) object;
        return List.of(bool ? 1d : 0d);
      } else {
        return (List.of(object == null ? 0 : (double) object.hashCode()));
      }
    }

  }

}
