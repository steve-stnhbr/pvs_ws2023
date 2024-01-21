package at.ac.tuwien.ifs.sge.agent.risk;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.backpropagation.MCTSBackpropagationStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.expansion.MCTSExpansionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection.MCTSSelectionStrategy;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.simulation.MCTSSimulationStrategy;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Instantiator {
  public static MCTSSelectionStrategy<Risk, RiskAction> createInstanceSelection(Class<?> clazz) {
    return createInstance(clazz);
  }

  public static MCTSExpansionStrategy<Risk, RiskAction> createInstanceExpansion(Class<?> clazz) {
    return createInstance(clazz);
  }

  public static MCTSSimulationStrategy<Risk, RiskAction> createInstanceSimulation(Class<?> clazz) {
    return createInstance(clazz);
  }

  public static MCTSBackpropagationStrategy<Risk, RiskAction> createInstanceBackpropagation(Class<?> clazz) {
    return createInstance(clazz);
  }

  public static <T> T createInstance(Class<?> clazz) {
    try {
      Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException("Could not instantiate " + clazz.getSimpleName(), e);
    }
  }

}
