package at.ac.tuwien.ifs.sge.agent.risk.util;

import java.util.List;

public class TreePrinter {

  /*
  public static <T extends TreeNode> Graph<TreeNode, DefaultEdge> createTree(T root) {
    Graph<TreeNode, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    graph.addVertex(root);
    buildGraph(root, graph);
    return graph;
  }

  public static <V, E> File drawGraph(Graph<V, E> graph) throws IOException {
    System.out.println("Drawing graph with " + graph.vertexSet().size() + " vertices and " + graph.edgeSet().size() + " edges");
    JGraphXAdapter<V, E> graphAdapter = new JGraphXAdapter<>(graph);
    graphAdapter.setLabelsVisible(false);
    mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
    layout.execute(graphAdapter.getDefaultParent());
    BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 1, new Color(1f,1f,1f,.8f), true, null);
    File imgFile = new File("out/graph.png");
    ImageIO.write(image, "PNG", imgFile);
    return imgFile;
  }

  public static <T extends TreeNode> File drawTree(T root) throws IOException {
    return drawGraph(createTree(root));
  }

  private static void buildGraph(TreeNode node, Graph<TreeNode, DefaultEdge> graph) {
    if (node.getChildren() == null) {
      return;
    }
    for (TreeNode child : node.getChildren()) {
      graph.addVertex(child);
      graph.addEdge(node, child);
      buildGraph(child, graph);
    }
  }
  */

  public interface TreeNode {
    public List<? extends TreeNode> getChildren();

    public String getLabel();
  }
}
