
import java.io.*;
import java.util.*;
import java.math.*;

public class PikaVirus implements Runnable {
  static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
  static PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));
  static StringTokenizer st = new StringTokenizer("");

  public static String next() {
    try {
    while (!st.hasMoreTokens()) {
      String s = br.readLine();
      if (s == null)
        return null;
      st = new StringTokenizer(s);
    }
    return st.nextToken();
    } catch(Exception e)  {
      return  null;
    }
  }

  public static void main(String[] asda) throws Exception {
    new Thread(null, new PikaVirus(), "PikaVirus", 1<<26).start();
  }

  public void run() {
    // tree size
    int N = Integer.parseInt( next() );
    Tree mainTree = readTree(N);
    int M = Integer.parseInt( next() );
    for (int k = 1; k <= M; k++) {
      Tree otherTree = readTree(N);
      boolean ok = isIsomorphic(mainTree, otherTree);
      String ans = " NO";
      if (ok) {
        StringBuilder builder = new StringBuilder();
        Item[] result = recoverTree(mainTree, otherTree);

        for (Item item : result) {
          builder.append(" ").append(item);
        }
        ans = builder.toString();
      }

      out.println("Case #" + k + ":" + ans);
    }
    //
    out.flush();
    System.exit(0);
  }

  boolean isIsomorphic(Tree x, Tree y) {
    if (x.children.size() != y.children.size() || x.height != y.height ||
        x.maxHeight != y.maxHeight || x.size != y.size
      ) {
      return false;
    }

    Set<String> used = new HashSet<>();
    boolean ok = true;
    for (Tree child : x.children) {
      boolean childFound = false;
      for (Tree other : y.children) {
        String s = other.name;
        if (!used.contains(s) && isIsomorphic(child, other)) {
          used.add(s);
          childFound = true;
          break;
        }
      }

      ok &= childFound;
    }

    return ok;
  }

  Tree readTree(int N)  {
    // node -> children
    LinkedHashMap<String, List<String> > map = new LinkedHashMap<>();
    Map<String, Integer> degreeMap = new HashMap<>();
    for (int k = 1; k < N; k++) {
      String source = next();
      String dest = next();
      List<String> children = map.get(source);
      if (children == null) {
        children = new ArrayList<>();
        map.put(source, children);
      }
      if (map.get(dest) == null) {
        map.put(dest, new ArrayList<>());
      }
      children.add(dest);

      // update in degree for dest
      Integer degree = degreeMap.get(dest);
      if (degree == null) {
        degree = 0;
      }
      degreeMap.put(dest, degree + 1);
    }

    // find root
    String root = null;
    for (String node : map.keySet())  {
      Integer degree = degreeMap.get(node);
      if (degree == null) {
        if (root != null)
          throw new RuntimeException("There are several roots");
        root = node;
      }
    }
    if (root == null) {
      throw new RuntimeException("No root found");
    }

    return buildTree(root, map, 0, null);
  }

  Tree buildTree(String node, LinkedHashMap<String, List<String> > map, int height, Tree parent) {
    Tree tree = new Tree(node, height, parent);
    List<String> children = map.get(node);
    for (String son : map.get(node))  {
      Tree other = buildTree(son, map, height + 1, parent);
      tree.addChild(other);
    }

    return tree;
  }

  Item[] recoverTree(Tree x, Tree y) {
    Map<String, Tree> first = new TreeMap<>();
    mapTree(x, first);

    Map<String, Tree> second = new TreeMap<>();
    mapTree(y, second);

    int N = first.size();
    Item[] ans = new Item[N];
    Set<String> used = new HashSet<>();
    int next = 0;
    for (String s : first.keySet()) {
      for (String t : second.keySet()) if (!used.contains(t)) {
        x = first.get(s);
        y = second.get(t);
        if (isIsomorphic(x, y)) {
          ans[next++] = new Item(s, t);
          used.add(t);
          break;
        }
      }
    }
    return ans;
  }

  void mapTree(Tree node, Map<String, Tree> map)  {
    map.put(node.name, node);
    for (Tree child : node.children) {
      mapTree(child, map);
    }
  }
}

class Item implements Comparable<Item> {
  String x, y;
  Item(String x, String y)  {
    this.x = x;
    this.y = y;
  }

  public String toString()  {
    return x + "/" + y;
  }

  public int compareTo(Item that)  {
    if (x.compareTo(that.x) != 0) {
      return x.compareTo(that.x);
    }
    return y.compareTo(that.y);
  }
}

class Tree implements Comparable<Tree> {
  final String name;
  final List<Tree> children;
  final Tree parent;
  final int height;
  int size;
  int maxHeight;
  Tree(String name, int height, Tree parent) {
    this.name = name;
    children = new ArrayList<>();
    this.height = height;
    maxHeight = height;
    size = 1;
    this.parent = parent;
  }

  void addChild(Tree child) {
    children.add(child);
    size += child.size;
    maxHeight = Math.max(maxHeight, child.maxHeight);
  }

  public int compareTo(Tree other)  {
    return name.compareTo(other.name);
  }

  public String toString() {
    return "tree(" + name + ")";
  }

}

