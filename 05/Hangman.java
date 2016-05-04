import java.io.*;
import java.util.*;
import java.math.*;
import java.net.*;

// 9cb4afde731e9eadcda4506ef7c65fa2
public class Hangman {
  static Socket pingSocket = null;
  static PrintWriter out = null;
  static BufferedReader in = null;
  public static void main(String[] args) throws Exception {
    try {
      pingSocket = new Socket("52.49.91.111", 9988);
      out = new PrintWriter(pingSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(pingSocket.getInputStream()));
    } catch (IOException e) {
      return;
    }

    final String LEVEL = "Level: ";
    final String CLEARED = "cleared!";
    Thread t = new Thread(new Runnable() {
      public void run() {
        String line;
        try {
          boolean playing = false;
          int remaindingLines = 0;
          int unknownLetters = 0;
          int currentLevel = 0;
          char lastLetter = 0;
          Manager manager = null;

          while (true)  {
            line = in.readLine();
            if (line == null) {
              Thread.sleep(100);
              continue;
            }

            System.out.println(line);

            // find level header
            int index = line.indexOf(LEVEL);
            if (line.contains(LEVEL)) {
              remaindingLines = 9;
              continue;
            }
            if (playing && line.contains(CLEARED)) {
              // WON
              System.out.println("WON");
              playing = false;
              manager = null;
              continue;
            }

            if (--remaindingLines != 0) {
              continue;
            }

            if (!playing) {
              unknownLetters = count(line, '_');
              currentLevel = unknownLetters;
              playing = true;
              manager = new Manager(currentLevel);
              lastLetter = manager.getBetter();
              out.println(lastLetter);
              continue;
            }

            System.out.println("processing:" + line + ", with: " + lastLetter);
            int newUnknownLetters = count(line, '_');
            if (unknownLetters == newUnknownLetters) {
              // wrong letter
              manager.removeLetter(lastLetter);
            } else {
              // correct letter
              String s = line.replace(" ", "");
              for (int k = 0; k < s.length(); k++) if (s.charAt(k) == lastLetter) {
                manager.correctLetter(lastLetter, k);
              }

              unknownLetters = newUnknownLetters;
            }
            lastLetter = manager.getBetter();
            out.println(lastLetter);
          }
        } catch (Exception ex)  {
          ex.printStackTrace();
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

    out.close();
    in.close();
    pingSocket.close();
  }
  static int count(String s, int ch)  {
    int ans = 0;
    for (char x : s.toCharArray())
      ans += x == ch ? 1 : 0;
    return ans;
  }
}


class Manager {
  final static String FILE_NAME = "words.txt";
  List<String> words;
  String wrongLetters;
  String okLetters;
  List<Integer> usedPositions;
  Manager(int len)  {
    words = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
      String line;
      while ( (line = reader.readLine()) != null )  {
        if (line.length() == len) {
          words.add(line);
        }
      }
    } catch(IOException ex) {
      ex.printStackTrace();
      System.exit(0);
    }

    System.out.println("LOADED: " + words.size());
    wrongLetters = "";
    okLetters = "";
    usedPositions = new ArrayList<>();
  }

  void correctLetter(char letter, int pos)  {
    usedPositions.add(pos);
    okLetters += letter;
    List<String> result = new ArrayList<>();
    for (String s : words) {
      if (s.charAt(pos) == letter) {
        result.add(s);
      }
    }
    words = result;
  }

  void removeLetter(char letter)  {
    wrongLetters += letter;
    if (okLetters.contains(letter+"")) {
      return;
    }
    List<String> result = new ArrayList<>();
    for (String s : words) {
      if (!s.contains(letter+"")) {
        result.add(s);
      }
    }
    words = result;
  }

  char getBetter()  {
    int[] cnt = new int[255];
    for (String s : words) {
      for (int k = 0; k < s.length(); k++) {
        if (usedPositions.contains(k)) {
          continue;
        }
        char ch = s.charAt(k);
        cnt[ch]++;
      }
    }

    // remove wrong letters
    for (char ch : wrongLetters.toCharArray()) {
      cnt[ch] = 0;
    }

    int best = 0;
    for (int k = 0; k < cnt.length; k++) {
      if (cnt[k] > cnt[best]) {
        best = k;
      }
    }
    return (char)best;
  }
}