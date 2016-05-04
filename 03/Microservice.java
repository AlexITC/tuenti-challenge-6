import java.io.*;
import java.util.*;
import java.math.*;

public class Microservice implements Runnable {
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
    new Thread(null, new Microservice(), "Microservice", 1<<26).start();
  }

  public void run() {
    List<String> lines = readLines();

    int startCode = lines.indexOf(CODE_LINE);

    // here tapes list starts
    int startTapes = lines.lastIndexOf(TAPES_LINE);

    // this is the line index where code ends
    int endTapes = lines.lastIndexOf(LAST_LINE);

    Parser parser = new Parser(lines, startCode + 1, startTapes - 1);
    List<Function> functions = parser.parseFunctions();
    Machine m = new Machine(functions);
    List<Tape> tapes = parser.parseTapes(startTapes + 1);
    for (Tape tape : tapes) {
      String result = m.processTape(tape);
      System.out.printf("Tape #%s: %s\n", tape.id, result);
    }
    out.flush();
    out.close();
  }

  final static String CODE_LINE = "code:";
  final static String TAPES_LINE = "tapes:";
  final static String LAST_LINE = "...";
  List<String> readLines()  {
    List<String> list = new ArrayList<>();
    String line;
    try {
      while ( (line = br.readLine()) != null )  {
        list.add(line);
      }
    } catch (IOException ex)  {
      ex.printStackTrace();
      System.exit(0);
    }
    return list;
  }
}

class Machine {
  Map<String, Function> map;
  Machine(List<Function> functions) {
    map = new HashMap<>();
    for (Function f : functions)  {
      map.put(f.name, f);
    }
  }

  char[] result;
  int index;
  String processTape(Tape tape) {
    String s = tape.value;
    int len = 100000;
    result = new char[len];
    Arrays.fill(result, ' ');
    for (int k = 0; k < s.length(); k++)
      result[k] = s.charAt(k);

    index = 0;
    try {
      processFunction( map.get("start") );
      while (true)
        processFunction(state);
    } catch (RuntimeException ex) {

    }
    return new String(result).trim();
  }

  Function state;
  void processFunction(Function f)  {
    state = f;
    if (f == null) {
      throw new RuntimeException("Finish");
    }
    String current = result[index] + "";
    for (Option option : f.options) {
      if (current.equals(option.type)) {
        processOption(option);
      //  processFunction(f);
        break;
      }
    }
  }

  void processOption(Option option) {
    for (Instruction instruction : option.instructions) {
      processInstruction(instruction);
    }
  }

  void processInstruction(Instruction instruction)  {
    debug(index + " - " + instruction.toString());
    switch (instruction.type) {
      case "move":
        if ("left".equals(instruction.value)) {
          index--;
        } else  {
          index++;
        }
      break;

      case "write":
        result[index] = instruction.value.charAt(1);
      break;

      case "state":
        processFunction( map.get(instruction.value) );
      break;

      default:
        throw new RuntimeException("Invalid instruction: " + instruction);
    }
  }
  private void debug(String s)  {
  //  System.out.println(s);
  }
}
class Parser {
  List<String> lines;
  int firstLine, lastLine;
  Parser(List<String> lines, int firstLine, int lastLine) {
    this.lines = lines;
    this.firstLine = firstLine;
    this.lastLine = lastLine;
  }

  List<Tape> parseTapes(int lineIndex) {
    List<Tape> tapes = new ArrayList<>();
    while (true)  {
      String line = lines.get(lineIndex++);
      if (line.charAt(1) != ' ') {
        break;
      }
      String[] array = line.trim().replace("'", "").split(":");
      String id = array[0].trim();
      String value = array[1].trim();
      Tape tape = new Tape(id, value);
      debug("parsing tape: " + tape);
      tapes.add(tape);
    }
    return tapes;
  }

  List<Function> parseFunctions()  {
    List<Function> result = new ArrayList<>();
    int skip = 2;
    int functionStartName = 0 + skip;
    for (int index = firstLine; index <= lastLine; index++)  {
      if ( lines.get(index).charAt(functionStartName) == ' ' )
        continue;

      Function function = parseFunction(index);
      result.add(function);
    }
    return result;
  }

  Function parseFunction(int lineIndex) {
    String name = lines.get(lineIndex).replace(":", "").trim();
    debug("parsing function: " + name);

    // looking for options
    List<Option> options = new ArrayList<>();
    int skip = 4;
    while (++lineIndex <= lastLine)  {
      char ch = lines.get(lineIndex).charAt(skip);
      if (ch == ' ') {
        continue;
      }
      if (ch != '\'') {
        // function ends
        break;
      }

      Option option = parseOption(lineIndex);
      options.add(option);
    }
    return new Function(name, options);
  }

  Option parseOption(int lineIndex) {
    String type = lines.get(lineIndex).replace("'", "").replace(":", "").trim();
    if (type.isEmpty()) {
      type = " ";
    }
    debug("  parsing option: " + type);

    List<Instruction> instructions = new ArrayList<>();
    int skip = 6;
    while (++lineIndex <= lastLine) {
      String line = lines.get(lineIndex);
      if (!isInstruction(line)) {
        break;
      }

      Instruction instruction = parseInstruction(line);
      instructions.add(instruction);
    }
    return new Option(type, instructions);
  }

  boolean isInstruction(String line)  {
    int index = 4;
    return line.charAt(index) == ' ';
  }

  Instruction parseInstruction(String line) {
    String[] array = line.trim().split(": ");
    String type = array[0];
    String value = array[1];
    debug("      parsing instruction: " + type + " - " + value);
    return new Instruction(type, value);
  }

  private void debug(String s)  {
  //  System.out.println(s);
  }
}

class Function {
  final String name;
  final List<Option> options;

  Function(String name, List<Option> options) {
    this.name = name;
    this.options = options;
  }
}

class Option {
  final String type; // #, 0, 1
  final List<Instruction> instructions;

  Option(String type, List<Instruction> instructions) {
    this.type = type;
    this.instructions = instructions;
  }
}

class Instruction {
  String type; // move, write, state
  String value; // (left / right), (0 / 1 / #), function name
  Instruction(String type, String value) {
    this.type = type;
    this.value = value;
  }
  public String toString()  {
    return type + ": " + value;
  }
}

class Tape {
  final String id;
  final String value;
  Tape(String id, String value) {
    this.id = id;
    this.value = value;
  }

  public String toString()  {
    return id + " - " + value;
  }
}
