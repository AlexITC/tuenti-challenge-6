
import java.io.*;
import java.util.*;
import java.math.*;

public class Hadouken implements Runnable {
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
    new Thread(null, new Hadouken(), "Hadouken", 1<<26).start();
  }

  public void run() {
    int cases = Integer.parseInt( next() );
    for (int k = 0; k < cases; k++) {
      String s = next();
      int ans = solveCase(s);
      out.println("Case #" + (k+1) + ": " + ans);
    }
    //
    out.flush();
    System.exit(0);
  }

  final String[][] combos = {
  //  "L-LD-D-RD-R-P".split("-"),
  //  "R-RD-D-LD-L-K".split("-"),
    "R-D-RD-P".split("-"),
    "D-RD-R-P".split("-"),
    "D-LD-L-K".split("-")
  };


  int solveCase(String s)  {
    String[] array = s.split("-");
    int N = array.length;
    int ans = 0;
    loop: for (int index = 0; index < N; index++)  {
      for (String[] combo : combos) {
        if (canTry(array, index, combo)) {
          ans++;
          break;
        }
      }
    }
    return ans;
  }

  boolean canTry(String[] array, int index, String[] combo) {
    int matches = 0;
    for (int c = 0; c < combo.length && index+c < array.length; c++)  {
      if (array[index+c].equals(combo[c])) {
        matches++;
      } else  {
        break;
      }
    }
    return matches == combo.length - 1;
  }

}
