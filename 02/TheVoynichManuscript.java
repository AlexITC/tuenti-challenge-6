import java.io.*;
import java.util.*;
import java.math.*;

public class TheVoynichManuscript implements Runnable {
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
    new Thread(null, new TheVoynichManuscript(), "TheVoynichManuscript", 1<<26).start();
  }

  public void run() {
    prepare();
    int cases = Integer.parseInt( next() );
    for (int k = 0; k < cases; k++) {
      int L = Integer.parseInt( next() );
      int R = Integer.parseInt( next() );
      String ans = solveCase(L, R);
      out.println("Case #" + (k+1) + ": " + ans);
    }
    //
    out.flush();
    System.exit(0);
  }

  final static String FILE_NAME = "corpus.txt";
  LinkedHashMap<String, List<Integer> > words;
  void prepare()  {
    words = new LinkedHashMap<>();
    int index = 1;
    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
      String line;
      while ( (line = reader.readLine()) != null )  {
        StringTokenizer tokenizer = new StringTokenizer(line);
        while (tokenizer.hasMoreTokens()) {
          String token = tokenizer.nextToken();
          List<Integer> set = words.get(token);
          if (set == null) {
            set = new ArrayList<>();
            words.put(token, set);
          }
          set.add(index++);
        }
      }
    } catch(IOException ex) {
      ex.printStackTrace();
      System.exit(0);
    }
  }

  final static int MAX_WORDS = 3;
  String solveCase(int L, int R)  {
    PriorityQueue<Item> queue = new PriorityQueue<>();
    for (String word : words.keySet())  {
      List<Integer> set = words.get(word);
      int times = count(set, L, R);
      Item item = new Item(word, times);
      if (times > 0) {
        queue.add(item);
      }
      if (queue.size() > MAX_WORDS) {
        queue.poll();
      }
    }
    if (queue.size() != MAX_WORDS) {
      throw new RuntimeException("Not so many words");
    }
    String ans = queue.poll().toString();
    while (!queue.isEmpty()) {
      ans = queue.poll() + "," + ans;
    }
    return ans;
  }

  int count(List<Integer> list, int L, int R) {
    return count(list, R) - count(list, L - 1);
  }

  // return the number of items <= key
  int count(List<Integer> list, int key)  {
    int lo = 0;
    int hi = list.size() - 1;
    int ans = -1;
    while (lo <= hi)  {
      int mid = (lo + hi) >> 1;
      if (list.get(mid) <= key) {
        ans = mid;
        lo = mid + 1;
      } else  {
        hi = mid - 1;
      }
    }
    return ans + 1;
  }
}

class Item implements Comparable<Item> {
  String word;
  int count;
  public Item(String s, int x)  {
    this.word = s;
    this.count = x;
  }
  public int compareTo(Item that) {
    return count - that.count;
  }
  public String toString()  {
    return word + " " + count;
  }
}
