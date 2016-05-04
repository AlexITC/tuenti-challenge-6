import java.io.*;
import java.util.*;
import java.math.*;

public class Tiles implements Runnable {
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
    new Thread(null, new Tiles(), "Tiles", 1<<26).start();
  }

  public void run() {
    int cases = Integer.parseInt( next() );
    for (int k = 0; k < cases; k++) {
      String ans = solveCase();
      out.println("Case #" + (k+1) + ": " + ans);
    }
    //
    out.flush();
    System.exit(0);
  }

  String solveCase()  {
    int N = Integer.parseInt( next() );
    int M = Integer.parseInt( next() );
    int[][] board = new int[N][M];
    for (int r = 0; r < N; r++) {
      String s = next();
      for (int c = 0; c < M; c++) {
        char ch = s.charAt(c);
        if (ch == '.') {
          board[r][c] = 0;
        } else if (Character.isUpperCase(ch)) {
          board[r][c] = ch - 'A' + 1;
        } else {
          board[r][c] = -(ch - 'a' + 1);
        }
      }
    }

    if (isInfinite(board)) {
      return "INFINITY";
    }

    int max = maxSum(board);
    return max+"";
  }

  int[][] replicate(int[][] board)  {
    int N = board.length;
    int M = board[0].length;
    int[][] newBoard = new int[2*N][2*M];
    copy(newBoard, 0, 0, board);
    copy(newBoard, N, 0, board);
    copy(newBoard, 0, M, board);
    copy(newBoard, N, M, board);
    return newBoard;
  }
  void copy(int[][] dest, int sr, int sc, int[][] src)  {
    for (int r = 0; r < src.length; r++)  for (int c = 0; c < src[0].length; c++)
      dest[sr + r][sc + c] = src[r][c];
  }
  int maxSum(int[][] board) {
    board = replicate(board);
    int N = board.length;
    int M = board[0].length;

    // prefixSum[r][c] = sum board[r][k], k <= c
    int[][] prefixSum = new int[N][M];
    for (int r = 0; r < N; r++) for (int c = 0; c < M; c++) {
      prefixSum[r][c] = board[r][c];
      if (c > 0) {
        prefixSum[r][c] += prefixSum[r][c - 1];
      }
    }

    int max = 0;
    for (int L = 0; L < M; L++) for (int R = L; R < M; R++) {
      // [row][L..R]
      int currentSum = 0;
      for (int row = 0; row < N; row++) {
        int rowSum = prefixSum[row][R];
        if (L > 0) {
          rowSum -= prefixSum[row][L - 1];
        }

        currentSum += rowSum;
        max = Math.max(currentSum, max);
        currentSum = Math.max(currentSum, 0);
      }
    }
    return max;
  }

  boolean isInfinite(int[][] board) {
    int N = board.length;
    int M = board[0].length;

    // check positive sum in a row
    for (int r = 0; r < N; r++) {
      int sum = 0;
      for (int c = 0; c < M; c++)
        sum += board[r][c];

      if (sum > 0) {
        return true;
      }
    }

    // check positive sum in a column
    for (int c = 0; c < M; c++) {
      int sum = 0;
      for (int r = 0; r < N; r++)
        sum += board[r][c];

      if (sum > 0) {
        return true;
      }
    }
    return false;
  }
}
