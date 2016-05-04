
import java.io.*;
import java.util.*;
import java.math.*;

public class ImmiscibleNumbers implements Runnable {
  static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
  static PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out), true);
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
    new Thread(null, new ImmiscibleNumbers(), "ImmiscibleNumbers", 1<<26).start();
  }

  public void run() {
    int cases = Integer.parseInt( next() );
    for (int k = 0; k < cases; k++) {
      long N = Long.parseLong( next() );
      String ans = solveCase(N);
      out.println("Case #" + (k+1) + ": " + ans);
    }
    //
    out.flush();
    System.exit(0);
  }


  String solveCase(long N)  {
    // remove prime factors 2 and 5
    int fives = 0;
    while (N % 5 == 0) {
      fives++;
      N /= 5;
    }

    int twos = 0;
    while (N % 2 == 0) {
      twos++;
      N /= 2;
    }

    // count the number of zeros required
    int zeros = Math.max(twos, fives);
    int ones = countOnes(N);
    return ones + " " + zeros;
  }

  // count the min number of 1's
  int countOnes(long x) {
    if (x % 2 == 0 || x % 5 == 0) {
      throw new RuntimeException("x should not be divisible by 2 or 5");
    }

    int ones = 0;
    long remainding = 0;
    do {
      remainding *= 10;
      remainding++;
      remainding %= x;
      ones++;
    } while(remainding != 0);
    return ones;
  }
}
