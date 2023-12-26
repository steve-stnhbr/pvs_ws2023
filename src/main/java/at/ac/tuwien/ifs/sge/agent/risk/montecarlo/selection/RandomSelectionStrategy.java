package at.ac.tuwien.ifs.sge.agent.risk.montecarlo.selection;

import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSNode;
import at.ac.tuwien.ifs.sge.agent.risk.montecarlo.MCTSTree;
import org.checkerframework.checker.units.qual.A;

public class RandomSelectionStrategy<T, A> extends MCTSSelectionStrategy<T, A> {
    public RandomSelectionStrategy() {
      super();
    }

  @Override
  public MCTSNode<T, A> select(MCTSNode<T, A> root, MCTSTree<T, A> tree) {
    while (!root.isLeaf()) {
        root = root.getChildren().get((int) (Math.random() * root.getChildren().size()));
    }
    return root;
  }

}
