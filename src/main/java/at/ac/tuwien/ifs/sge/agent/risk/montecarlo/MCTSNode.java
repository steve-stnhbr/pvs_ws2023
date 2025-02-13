package at.ac.tuwien.ifs.sge.agent.risk.montecarlo;

import at.ac.tuwien.ifs.sge.agent.risk.util.TreePrinter;
import at.ac.tuwien.ifs.sge.game.risk.board.Risk;
import at.ac.tuwien.ifs.sge.game.risk.board.RiskAction;
import hu.webarticum.treeprinter.TreeNode;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MCTSNode<T, A> implements TreeNode, TreePrinter.TreeNode {
  public static final Map<RiskAction, Double> actionProbabilities = new HashMap<>();
  private final Map<String, Object> properties;
  private MCTSNode<T, A> parent;
  private List<MCTSNode<T, A>> children;
  private int visits;
  private double utility, amafUtility;
  private int playerId;

  private final MCTSTree<T, A> tree;

  private T state;
  private final A action;

  public MCTSNode(T state, MCTSTree<T, A> tree) {
    this(state, null, null, -1, tree);
  }

  public MCTSNode(T state, MCTSNode<T, A> parent, A action, int playerId, MCTSTree<T, A> tree) {
    this.state = state;
    this.parent = parent;
    this.playerId = playerId;
    this.action = action;
    this.children = new ArrayList<>();
    this.properties = new HashMap<>();
    this.tree = tree;
  }

  public MCTSNode<T, A> getParent() {
    return parent;
  }

  public void setParent(MCTSNode<T, A> parent) {
    this.parent = parent;
  }

  public List<MCTSNode<T, A>> getChildren() {
    return children;
  }

  @Override
  public String getLabel() {
    return visits + " " + utility;
  }

  public void setChildren(List<MCTSNode<T, A>> children) {
    this.children = children;
  }
  public void addChild(MCTSNode<T, A> node) {
    node.setParent(this);
    children.add(node);
    tree.onAdd(node);
  }

  public double getAverageUtility() {
    return utility / visits;
  }

  /**
   * Given an action the child node of the current node is returned if it has the given action.
   *
   * @param action the action which the child is returned for
   * @return the child which has the wanted action
   */
  public MCTSNode<T,A> getChildForAction(RiskAction action) {

    for (MCTSNode<T,A> child : this.children) {
      if (child.action.equals(action)) {
        return child;
      }
    }
    return null;
  }

  public double calculateAMAFUtilityValue() {
    int totalVisits = 0;
    double totalAMAFUtility = 0.0;

    for (MCTSNode<T,A> child : children) {
      double amafUtility = child.getAmafUtility();

      totalVisits += child.visits;
      totalAMAFUtility += (amafUtility * child.visits);
    }

    if (totalVisits > 0) {
      return totalAMAFUtility / totalVisits;
    } else {
      return 0.0;
    }
  }

  public int getVisits() {
    return visits;
  }

  public void setVisits(int visits) {
    this.visits = visits;
  }

  public double getUtility() {
    return utility;
  }

  public void setUtility(double utility) {
    this.utility = utility;
  }

  public int getPlayerId() {
    return playerId;
  }

  public void setPlayerId(int playerId) {
    this.playerId = playerId;
  }

  public boolean isLeaf() {
    return children.isEmpty();
  }

  public T getState() {
    return state;
  }

  public void setState(T state) {
    this.state = state;
  }

  public A getAction() {
    return action;
  }

  public void setProperty(String key, Object value) {
      properties.put(key, value);
  }
  public boolean hasProperty(String key) {
      return properties.containsKey(key);
  }

  public double getAmafUtility() {
    return amafUtility;
  }

  public void setAmafUtility(double amafUtility) {
    this.amafUtility = amafUtility;
  }

  public Object getProperty(String key) {
      return properties.get(key);
  }

  public String getStringProperty(String key) {
      return (String) properties.get(key);
  }

  public int getIntProperty(String key) {
      return (int) properties.get(key);
  }

  public int getIntProperty(String key, int defaultValue) {
      if (properties.containsKey(key)) {
          return (int) properties.get(key);
      } else {
          return defaultValue;
      }
  }

  /**
   * Implements TreeNode interface: This method returns the content of the node. It will be displayed in the tree-visualization.
   * @return The content of the node.
   */
  @Override
  public String content() {
    return "(" + visits + ", " + utility + ")";
  }

  /**
   * Implements TreeNode interface: This method returns the children of the node. They will be displayed in the tree-visualization.
   * @return The children of the node.
   */
  @Override
  public List<TreeNode> children() {
    return this.children.stream().map(c -> (TreeNode) c).collect(Collectors.toList());
  }


  @Override
  public String toString() {
    return "MCTSNode{" +
      ", visits=" + visits +
      ", utility=" + utility +
      ", playerId=" + playerId +
      ", state=" + state +
      ", action=" + action +
      '}';
  }

  public MCTSTree<T, A> getTree() {
    return tree;
  }
}
