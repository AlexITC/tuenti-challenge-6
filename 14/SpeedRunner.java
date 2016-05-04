
import java.io.*;
import java.util.*;
import java.math.*;

public class SpeedRunner implements Runnable {
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
    new Thread(null, new SpeedRunner(), "SpeedRunner", 1<<26).start();
  }

  public void run() {
    int cases = Integer.parseInt( next() );
    for (int k = 1; k <= cases; k++) {
      readInput();
      String ans = solveCase();
      out.println("Case #" + k + ": " + ans);
    }
    //
    out.flush();
    System.exit(0);
  }

  void readInput() {
    N = Integer.parseInt( next() );
    M = Integer.parseInt( next() );
    start = null;
    exit = null;
    keys = new ArrayList<>();
    board = new char[N][M];
    for (int r = 0; r < N; r++) {
      String s = next();
      for (int c = 0; c < M; c++) {
        board[r][c] = s.charAt(c);
        if (board[r][c] == START) {
          start = new Location(r, c);
          board[r][c] = EMPTY;
        } else if (board[r][c] == EXIT) {
          exit = new Location(r, c);
          board[r][c] = EMPTY;
        } else if (board[r][c] == KEY) {
          keys.add(new Location(r, c));
          board[r][c] = EMPTY;
        }
      }
    }
  }
  String solveCase() {
    pathTreeCache = new HashMap<>();
    bestPathCache = new HashMap<>();
    mem = new PathResult[keys.size() + 1][1 << keys.size()];
    computed = new boolean[keys.size() + 1][1 << keys.size()];
    PathResult result = solve(-1, 0);
    if (result == null) {
      return "IMPOSSIBLE";
    }

    int frames = result.state.frames;
    return frames + " " + result.path;
  }

  // use memoization
  PathResult[][] mem;
  boolean[][] computed;
  PathResult solve(int lastKey, int usedKeys) {
    // an invalid key index means we are starting
    Location lastLocation = start;
    if (lastKey >= 0) {
      lastLocation = keys.get(lastKey);
    }

    if (Integer.bitCount(usedKeys) == keys.size()) {
      // got all keys
      return bestPath(lastLocation, exit);
    }
    if (computed[lastKey + 1][usedKeys]) {
      return mem[lastKey + 1][usedKeys];
    }

    PathResult best = null;
    for (int key = 0; key < keys.size(); key++) if ( ((1<<key)&usedKeys) == 0 ) {
      // go to key after lastKey
      PathResult first = bestPath(lastLocation, keys.get(key));
      if (first == null) {
        continue;
      }

      PathResult second = solve(key, usedKeys | (1<<key));
      if (second == null) {
        continue;
      }

      PathResult current = mergePaths(first, second);
      if (current.compareTo(best) < 0) {
        best = current;
      }
    }
    computed[lastKey + 1][usedKeys] = true;
    return mem[lastKey + 1][usedKeys] = best;
  }

  PathResult mergePaths(PathResult first, PathResult second) {
    State newState = new State(
      second.state.location,
      first.state.frames + second.state.frames,
      first.state.keysPressed + second.state.keysPressed
    );

    PathResult result = new PathResult(newState, first.path + second.path);
    return result;
  }

  // compute the distnace from begin to any node
  Map<Location, State[][]> pathTreeCache;
  State[][] pathTree(Location begin) {
    if (pathTreeCache.containsKey(begin)) {
      return pathTreeCache.get(begin);
    }
    PriorityQueue<State> queue = new PriorityQueue<>();

    // compute disntance from every node to end
    State[][] dist = new State[N][M];
    queue.add( new State(begin, 0, 0) );
    while (!queue.isEmpty())  {
      State current = queue.poll();
      Location location = current.location;

      // already computed
      if (dist[location.row][location.col] != null) {
        continue;
      }

      // mark it
      dist[location.row][location.col] = current;

      // is there a tile that can reach current using a jump?
      for (int delta = 0; delta < DELTA_ROW.length; delta++) {
        int newRow = (location.row + DELTA_ROW[delta] + N) % N;
        int newCol = (location.col + DELTA_COL[delta] + M) % M;

        // already computed
        if (dist[newRow][newCol] != null) {
          continue;
        }

        // we can't stay on a wall
        if (board[newRow][newCol] == WALL) {
          continue;
        }

        // this tile is empty or a ladder
        Location newLocation = new Location(newRow, newCol);
        if (canJump(newLocation)) {
          State newState = new State(newLocation, current.frames + 1, current.keysPressed + 1);
          queue.add(newState);
        }
      }

      // can we get current falling down?
      int newRow = location.row - 1;
      int newCol = location.col;
      Location newLocation = new Location(newRow, newCol);

      // we can't is we are at the top or the top is a wall
      if (newRow < 0 || board[newRow][newCol] == WALL || dist[newRow][newCol] != null) {
        continue;
      }

      if (canJump(newLocation)) {
        continue;
      }

      State newState = new State(newLocation, current.frames + 1, current.keysPressed);
      queue.add(newState);
    }

    // verify distances
    for (State[] array : dist) for (State state : array) if (state != null && state.frames < state.keysPressed) {
      out.println("distances to " + begin);
      printMatrix(dist);
      throw new RuntimeException("Invalid distances");
    }

    pathTreeCache.put(begin, dist);
    return dist;
  }

  void printMatrix(Object[][] matrix) {
    int len = 30;
    for (int r = 0; r+1 < N; r++) {
      for (int c = 0; c < M; c++) {
        out.printf("%30s", matrix[r][c]);
      }
      out.println();
    }
  }

  // compute the best path from begin to end
  Map<Location, Map<Location, PathResult>> bestPathCache;
  PathResult bestPath(Location begin, Location end) {
    if (bestPathCache.containsKey(begin) && bestPathCache.get(begin).containsKey(end)) {
      return bestPathCache.get(begin).get(end);
    }

    State[][] dist = pathTree(end);
    if (dist[begin.row][begin.col] == null) {
      // unreachable
      return null;
    }

    // build lexicographically smallest path
    String path = "";
    Location current = begin;
    int frames = 0;
    int keysPressed = 0;
    while (!current.equals(end)) {
      Location location = current;
      if (!canJump(location)) {
        // is falling down
        frames++;
        int newRow = location.row + 1;
        int newCol = location.col;
        Location newLocation = new Location(newRow, newCol);
        current = newLocation;
        continue;
      }

      Location bestLocation = null;
      char bestCommand = '\0';
      keysPressed++;
      frames++;
      for (int delta = 0; delta < DELTA_ROW.length; delta++) {
        int newRow = (location.row + DELTA_ROW[delta] + N) % N;
        int newCol = (location.col + DELTA_COL[delta] + M) % M;
        char newCmd = COMMANDS[delta];
        if (board[newRow][newCol] != EMPTY && board[newRow][newCol] != LADDER)
          continue;

        if (dist[newRow][newCol] == null) {
          continue;
        }

        Location newLocation = new Location(newRow, newCol);
        boolean inBestPath = true;
        inBestPath &= dist[begin.row][begin.col].frames - frames == dist[newRow][newCol].frames;
        inBestPath &= dist[begin.row][begin.col].keysPressed - keysPressed == dist[newRow][newCol].keysPressed;

        if (inBestPath) {
          if (bestLocation == null || newCmd < bestCommand) {
            bestLocation = newLocation;
            bestCommand = newCmd;
          }
        }
      }

      if (bestLocation == null || bestCommand == '\0') {
        throw new RuntimeException("Invalid state");
      }
      current = bestLocation;
      path += bestCommand;
    }

    PathResult result = new PathResult(dist[begin.row][begin.col], path);
    if (!bestPathCache.containsKey(begin)) {
      bestPathCache.put(begin, new HashMap<>());
    }
    bestPathCache.get(begin).put(end, result);
    return result;
  }

  boolean canJump(Location location)  {
    if (board[location.row][location.col] == WALL) {
      return false;
    }
    boolean canJump = false;
    canJump |= board[location.row][location.col] == LADDER; // on a ladder
    canJump |= board[location.row + 1][location.col] == LADDER; // upside a ladder
    canJump |= board[location.row + 1][location.col] == WALL; // upside a wall
    return canJump;
  }

  final static int[] DELTA_ROW = { 1, -1, 0, 0 };
  final static int[] DELTA_COL = { 0, 0, 1, -1 };
  final static char[] COMMANDS = "SWDA".toCharArray();

  // board data
  int N, M;
  Location start, exit;
  List<Location> keys;
  char[][] board;

  //
  final static char EMPTY = '.';
  final static char WALL = '#';
  final static char LADDER = 'H';
  final static char START = 'S';
  final static char EXIT = 'E';
  final static char KEY = 'K';
}

class PathResult implements Comparable<PathResult> {
  final State state;
  final String path;
  PathResult(State state, String path)  {
    this.state = state;
    this.path = path;
  }

  public int compareTo(PathResult that) {
    if (that == null) {
      return -1;
    }
    if (state.compareTo(that.state) != 0) {
      return state.compareTo(that.state);
    }
    return path.compareTo(that.path);
  }
}

class State implements Comparable<State> {
  final Location location;
  int frames;
  int keysPressed;
  State(Location location, int frames, int keysPressed) {
    this.location = location;
    this.frames = frames;
    this.keysPressed = keysPressed;
  }

  public String toString()  {
    return String.format("State[%s, %d, %d]", location, frames, keysPressed);
  }

  public int compareTo(State that)  {
    if (that == null) {
      return -1;
    }

    if (frames != that.frames) {
      return frames - that.frames;
    }

    if (keysPressed != that.keysPressed) {
      return keysPressed - that.keysPressed;
    }

    return 0;
  }
}

class Location {
  final int row, col;
  Location(int row, int col)  {
    this.row = row;
    this.col = col;
  }

  public int hashCode() {
    return (row << 10) | col;
  }

  public String toString()  {
    return String.format("Location(%d, %d)", row + 1, col + 1);
  }

  public boolean equals(Object o) {
    Location other = (Location)o;
    return row == other.row && col == other.col;
  }
}
