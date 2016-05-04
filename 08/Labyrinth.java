import java.io.*;
import java.util.*;
import java.math.*;
import java.net.*;

public class Labyrinth {
  static Socket pingSocket = null;
  static PrintWriter out = null;
  static BufferedReader in = null;
  public static void main(String[] args) throws Exception {
    if (args.length != 0) {
      // merge files
      mergeResults();
      return;
    }
    try {
      pingSocket = new Socket("52.49.91.111", 1986);
      out = new PrintWriter(pingSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(pingSocket.getInputStream()));
    } catch (IOException e) {
      return;
    }

    Thread t = new Thread(new Runnable() {
      public void run() {
          Location mazeLocation = new Location(100, 100);
        Maze dynMaze = new Maze(200, 200, mazeLocation);
        try {
          char[][] maze = new char[MAZE_SIZE][MAZE_SIZE];
          int orientation = 2;
          int moves = 0;
          do  {
            // read maze
            maze = new char[MAZE_SIZE][MAZE_SIZE];
            for (int row = 0; row < MAZE_SIZE; row++)  {
              String s = in.readLine();
              for (int col = 0; col < MAZE_SIZE; col++)  {
                maze[row][col] = s.charAt(col);
                if (maze[row][col] == MY_LOCATION) {
                  mazeLocation = new Location(row, col);
                }
              }
            }
            dynMaze.apply(maze, mazeLocation);

          //  for (int delta = 1; delta > -3; delta--)  // move always to the left
            for (int delta = -1; delta < 3; delta++)  // move always to the right
            {
              int newOrientation = (orientation + delta + 4) % 4;
              int newRow = mazeLocation.row + deltaRow[newOrientation];
              int newCol = mazeLocation.col + deltaCol[newOrientation];
              Location newLocation = new Location(newRow, newCol);
              if  ( isCorrectLocation(maze, newLocation) ) {
                char command = orientationCommand[newOrientation];
                orientation = newOrientation;
                moves++;
                out.println(command);
                dynMaze.move(deltaRow[newOrientation], deltaCol[newOrientation]);
                break;
              }
            }
          } while (true);
        } catch (Exception ex)  {
          ex.printStackTrace();
          dynMaze.print();
        }
      }
    });
    t.start();

    
    Scanner stdin = new Scanner(System.in);
    while (true)  {
      String s = stdin.next();
      if ("quit".equals(s)) {
        break;
      }
      out.println(s);
    }
    t.join();

    out.close();
    in.close();
    pingSocket.close();
  }

  static boolean DEBUG_ENABLED = true;
  static void debug(Object s) {
    if (DEBUG_ENABLED) {
      System.out.println(s);
    }
  }

  static boolean isMazeRow(String s)  {
    for (char ch : s.toCharArray())
      if (ch != 'x' && ch != ' ' && ch != 'x' && ch != '#') {
        return false;
      }
    return true;
  }

  final static int[] deltaRow = { -1, +0, +1, +0 };
  final static int[] deltaCol = { +0, +1, +0, -1 };
  final static char[] orientationCommand = "urdl".toCharArray();
  
  static boolean isCorrectLocation(char[][] maze, Location location)  {
    int N = maze.length;
    int M = maze[0].length;
    int r = location.row;
    int c = location.col;
    return  r >= 0 && r < N && c >= 0 && c < M && maze[r][c] == EMPTY;
  }

  final static char BLOCKED = '#';
  final static char EMPTY = ' ';
  final static char MY_LOCATION = 'x';
  final static int MAZE_SIZE = 7;

  static void mergeResults()  {
    String[] fileList = {"resultLeft", "resultRight"};
    int N = 200;
    int M = 200;
    char[][] board = new char[N][M];
    for (char[] v : board)
      Arrays.fill(v, '-');

    for (String name : fileList)  {
      try (BufferedReader br = new BufferedReader(new FileReader(name))) {
        br.readLine();
        br.readLine();
        for (int r = 0; r < N; r++) {
          String line = br.readLine();
          for (int c = 0; c < M; c++) {
            char ch = line.charAt(c);
            if (ch == '-') {
              continue;
            }
            board[r][c] = ch;
          }
        }
      } catch (IOException ex)  {
        ex.printStackTrace();
      }
    }

    int minRow = N, minCol = M;
    int maxRow = 0, maxCol = 0;
    for (int r = 0; r < N; r++) for (int c = 0; c < M; c++) if (board[r][c] != '-') {
      minRow = Math.min(minRow, r);
      minCol = Math.min(minCol, c);
      maxRow = Math.max(maxRow, r);
      maxCol = Math.max(maxCol, c);
    }
    System.out.printf("(%d, %d) - (%d, %d)\n", minRow, minCol, maxRow, maxCol);
    for (int r = minRow; r <= maxRow; r++) {
      System.out.println(new String(board[r], minCol, maxCol - minCol + 1));
    }
  }
}

class Location {
  final int row, col;
  Location(int row, int col)  {
    this.row = row;
    this.col = col;
  }
  Location move(int deltaRow, int deltaCol) {
    return new Location(row + deltaRow, col + deltaCol);
  }
  public String toString()  {
    return String.format("Location(%d, %d)", row + 1, col + 1);
  }
}

class Maze {
  char[][] board;
  Location location;
  Maze(int N, int M, Location location)  {
    board = new char[N][M];
    for (char[] v : board)
      Arrays.fill(v, UNKNOWN);

    this.location = location;
  }

  void move(int deltaRow, int deltaCol) {
    location = location.move(deltaRow, deltaCol);
  }

  void apply(char[][] partialBoard, Location partialLocation) {
    for (int r = 0; r < partialBoard.length; r++) for (int c = 0; c < partialBoard[0].length; c++)  {
      int deltaRow = r - partialLocation.row;
      int deltaCol = c - partialLocation.col;

      // int row = location.row + deltaRow;
      // if (row < 0) {
      //   // extend up
      //   extendUp();
      // } else if (row >= board.length) {
      //   // extend down
      //   extendDown();
      // }

      // int col = location.col + deltaCol;
      // if (col < 0) {
      //   // extend left
      //   extendLeft();
      // } else if (col >= board[0].length)  {
      //   // extend right
      //   extendRight();
      // }

      // copy
      int row = location.row + deltaRow;
      int col = location.col + deltaCol;
      char ch = partialBoard[r][c];
      board[row][col] = ch;
    }
  }

  final char UNKNOWN = '-';
  void extendUp() {
    int N = board.length;
    int M = board[0].length;
    char[][] newBoard = new char[N + 1][M];
    Arrays.fill(newBoard[0], UNKNOWN);
    for (int r = 0; r < N; r++) for (int c = 0; c < M; c++) {
      newBoard[r + 1][c] = board[r][c];
    }
    board = newBoard;
    location = location.move(1, 0);
    board = newBoard;
  }
  void extendDown() {
    int N = board.length;
    int M = board[0].length;
    char[][] newBoard = new char[N + 1][M];
    Arrays.fill(newBoard[N], UNKNOWN);
    for (int r = 0; r < N; r++) for (int c = 0; c < M; c++) {
      newBoard[r][c] = board[r][c];
    }
    board = newBoard;
  }
  void extendLeft() {
    int N = board.length;
    int M = board[0].length;
    char[][] newBoard = new char[N][M + 1];
    for (int r = 0; r < N; r++)  newBoard[r][0] = UNKNOWN;
    for (int r = 0; r < N; r++) for (int c = 0; c < M; c++) {
      newBoard[r][c + 1] = board[r][c];
    }
    board = newBoard;
    location = location.move(0, 1);
  }
  void extendRight() {
    int N = board.length;
    int M = board[0].length;
    char[][] newBoard = new char[N][M + 1];
    for (int r = 0; r < N; r++)  newBoard[r][M] = UNKNOWN;
    for (int r = 0; r < N; r++) for (int c = 0; c < M; c++) {
      newBoard[r][c] = board[r][c];
    }
    board = newBoard;
  }

  void print() {
    System.out.println();
    System.out.println("*** MAZE ***");
    System.out.println(location);
    for (char[] v : board)
      System.out.println( new String(v) );
    System.out.println("*** END ***");
  }
}
