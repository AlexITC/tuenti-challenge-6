import java.io.*;
import java.util.*;
import java.math.*;

public class TeamLunch implements Runnable {
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
    new Thread(null, new TeamLunch(), "TeamLunch", 1<<26).start();
  }

  public void run() {
    int cases = Integer.parseInt( next() );
    for (int k = 0; k < cases; k++) {
      int N = Integer.parseInt( next() );
      int ans = solveCase(N);
      out.println("Case #" + (k+1) + ": " + ans);
    }
    //
    out.flush();
    System.exit(0);
  }

  int solveCase(int N)  {
    if (N == 0) {
      return 0;
    }
    int lo = 1;
    int hi = 10000000;
    int ans = 0;
    while (lo <= hi)  {
      int tables = (lo + hi) >> 1;
      int capacity = (tables << 1) + 2;
      if (capacity >= N) {
        ans = tables;
        hi = tables - 1;
      } else {
        lo = tables + 1;
      }
    }
    return ans;
  }

}
