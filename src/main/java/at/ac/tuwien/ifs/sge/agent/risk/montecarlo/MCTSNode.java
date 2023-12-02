package at.ac.tuwien.ifs.sge.agent.risk.montecarlo;

import hu.webarticum.treeprinter.TreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MCTSNode<T, A> implements TreeNode {
  private MCTSNode<T, A> parent;
  private List<MCTSNode<T, A>> children;
  private int visits;
  private double utility;
  private int playerId;

  private T state;
  private final A action;

  public MCTSNode(T state) {
    this(state, null, null, -1);
  }

  public MCTSNode(T state, MCTSNode<T, A> parent, A action, int playerId) {
    this.state = state;
    this.parent = parent;
    this.playerId = playerId;
    this.action = action;
    this.children = new ArrayList<>();
  }

  /**
   * This method backpropagates the utility of a child node to the parent node.
   * @param child
   */
  public void backpropagate(MCTSNode<T, A> child) {
    visits++;
    if (child != null) {
      utility += child.getUtility();
    }
    if (parent != null) {
      parent.backpropagate(this);
    }
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

  public void setChildren(List<MCTSNode<T, A>> children) {
    this.children = children;
  }
  public void addChild(MCTSNode<T, A> node) {
    node.setParent(this);
    children.add(node);
  }

  public double getAverageUtility() {
    return utility / visits;
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
}
