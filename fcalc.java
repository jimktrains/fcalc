import java.util.*;

class FStack<T> extends Stack<T> {
  static final long serialVersionUID = 1;
  public void roll(int n) {
    Stack<T> tmp = new Stack<T>();
    T first = pop();
    for(int i = 1; i < n; i++) {
      tmp.push(pop());
    }
    T last = pop();
    push(first);
    for(int i = 1; i < n; i++) {
      push(tmp.pop());
    }
    push(last);
  }
}

interface StackObj {
  int getType();
}
class SODouble implements StackObj {
  private double val;
  public SODouble(String x) { val = Double.valueOf(x); }
  public SODouble(Double x) { val = x; }
  public SODouble(SODouble x) { val = x.val; }
  public double getValue() { return val; }
  public int getType() { return FCalc.DOUBLE; }
  public String toString() { return String.valueOf(val); }
}
class SOBool implements StackObj {
  private boolean val;
  public SOBool(String x) { val = Boolean.valueOf(x); }
  public SOBool(boolean x) { val = x; }
  public boolean getValue() { return val; }
  public int getType() { return FCalc.BOOL; }
  public String toString() { return String.valueOf(val); }
}

class SORat implements StackObj {
  private double num, den;
  public SORat(double x, double y) { num = y; den = x; }
  public SORat(SODouble x, SODouble y) { num = y.getValue(); den = x.getValue(); }
  public double getNumerator() { return num; }
  public double getDenominator() { return den; }
  public int getType() { return FCalc.RAT; }
  public String toString() { return String.valueOf(num)+"/"+String.valueOf(den); }
}
interface Executable {
  // List of argument types in the order they will be popped off the stack
  // before execution
  int[] args();
  // List of return types in the order they will be pushed on to the stack
  // after execution. Types are identified by negative numbers, so a positive
  // number here represents the type of that index in the argument list
  int[] rets();
  StackObj[] run(StackObj[] so);
}




class FCalc
{
    static Map<String, Executable[]> fcns;
    static final int ANY = -1;
    static final int DOUBLE = -2;
    static final int BOOL = -3;
    static final int RAT = -4;

    public static void main(String[] args)
    {
        init();

        String prog = "1 DUP ADD .. 3 4 5 .. 3ROL .. " +
                      "DIV . PI MUL . DUP INV PM MUL . " +
                      "DRP ..  LT .. DUP NOT .. 2DUP XOR .. " + 
                      "1 2 rat 3 4 rat .. add .";
        exec(parseProg(prog));
    }

    public static String[] parseProg(String prog) {
      String[] eprog = prog.toLowerCase().split("\\s+");
      return eprog;
    }

    public static void exec(String[] prog) {
      FStack<StackObj> s = new FStack<StackObj>();
      
      for(String cmd : prog) {

        if(cmd.equals(".")) {
          System.out.println(s.peek()); 
        }
        else if(cmd.equals("..")) { 
          for(StackObj i : s) { System.out.print(i.toString() + " "); } 
          System.out.println();
        }
        else {
          Executable torun = execLookup(cmd, s);
          if (torun == null) {
            s.push(new SODouble(cmd));
          }
          else {
            int argc = torun.args().length;
            StackObj[] args = new StackObj[argc];
            for(int i = 0; i < argc; i++){ args[i] = s.pop(); }
            StackObj[] ret = torun.run(args);
            for(StackObj r : ret) { s.push(r); }
          }
        }
      }
    }

    // Resolves an function based upon what is currently on the stack
    // and the argument types and count that the function asks for
    public static Executable execLookup(String cmd, FStack<StackObj> s) {
      Executable[] totest = fcns.get(cmd);

      if(totest == null) return null;
      for(int i = totest.length - 1; i > -1; i--) {
        int[] args = totest[i].args();
        if (args.length > s.size()) continue;

        Stack<StackObj> tmp = new Stack<StackObj>();
        boolean good = true;
        int j;
        for(j = 0; j < args.length; j++) {
          StackObj t = s.pop();
          tmp.push(t);

          if (args[j] != FCalc.ANY 
              && args[j] != t.getType()) 
          { 
            good = false; 
          }
        }
        for(int k = j; j > 0; j--) s.push(tmp.pop());
        if (good) return totest[i];
      }

      return null;
    }

    public static void init() {
      fcns = new HashMap<String, Executable[]>();

      fcns.put("dup",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.ANY}; }
        public int[] rets() { return new int[] {0, 0}; }
        public StackObj[] run(StackObj[] so) {
          StackObj[] ret = new StackObj[] { so[0], so[0] };
          return ret;
        }
      }});
      fcns.put("2dup",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.ANY, FCalc.ANY}; }
        public int[] rets() { return new int[] {1, 0, 1, 0}; }
        public StackObj[] run(StackObj[] so) {
          StackObj[] ret = new StackObj[] { so[1], so[0], so[1], so[0] };
          return ret;
        }
      }});
      fcns.put("swp",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.ANY, FCalc.ANY}; }
        public int[] rets() { return new int[] {0, 1}; }
        public StackObj[] run(StackObj[] so) {
          StackObj[] ret = new StackObj[] { so[0], so[1] };
          return ret;
        }
      }});
      fcns.put("rot",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.ANY, FCalc.ANY, FCalc.ANY}; }
        public int[] rets() { return new int[] {0, 1, 2}; }
        public StackObj[] run(StackObj[] so) {
          StackObj[] ret = new StackObj[] { so[0], so[1], so[2] };
          return ret;
        }
      }});
      fcns.put("3rol",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.ANY, FCalc.ANY, FCalc.ANY, FCalc.ANY}; }
        public int[] rets() { return new int[] {0, 2, 1, 3}; }
        public StackObj[] run(StackObj[] so) {
          StackObj[] ret = new StackObj[] { so[0], so[2], so[1], so[3] };
          return ret;
        }
      }});
      fcns.put("drp",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.ANY}; }
        public int[] rets() { return new int[] {}; }
        public StackObj[] run(StackObj[] so) {
          StackObj[] ret = new StackObj[] { };
          return ret;
        }
      }});
      fcns.put("inc",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.DOUBLE}; }
        public int[] rets() { return new int[] {FCalc.DOUBLE}; }
        public StackObj[] run(StackObj[] so) {
          SODouble x = (SODouble)so[0];
          StackObj[] ret = new StackObj[] {new SODouble(x.getValue() + 1) };
          return ret;
        }
      }});
      fcns.put("dec",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.DOUBLE}; }
        public int[] rets() { return new int[] {FCalc.DOUBLE}; }
        public StackObj[] run(StackObj[] so) {
          SODouble x = (SODouble)so[0];
          StackObj[] ret = new StackObj[] {new SODouble(x.getValue() - 1) };
          return ret;
        }
      }});
      fcns.put("add",  new Executable[] { 
        new Executable() {
          public int[] args() { return new int[] {FCalc.DOUBLE, FCalc.DOUBLE}; }
          public int[] rets() { return new int[] {FCalc.DOUBLE}; }
          public StackObj[] run(StackObj[] so) {
            SODouble x = (SODouble)so[0];
            SODouble y = (SODouble)so[1];
            StackObj[] ret = new StackObj[] {new SODouble(x.getValue() + y.getValue()) };
            return ret;
          }
        },
        new Executable() {
          public int[] args() { return new int[] {FCalc.RAT, FCalc.RAT}; }
          public int[] rets() { return new int[] {FCalc.RAT}; }
          public StackObj[] run(StackObj[] so) {
            SORat x = (SORat)so[0];
            SORat y = (SORat)so[1];
            double num = (x.getNumerator() * y.getDenominator()) + (y.getNumerator() * x.getDenominator()),
                   den = x.getDenominator() * y.getDenominator();
            StackObj[] ret = new StackObj[] {new SORat(den, num) };
            return ret;
          }
        }
      });
      fcns.put("sub",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.DOUBLE, FCalc.DOUBLE}; }
        public int[] rets() { return new int[] {FCalc.DOUBLE}; }
        public StackObj[] run(StackObj[] so) {
          SODouble x = (SODouble)so[0];
          SODouble y = (SODouble)so[1];
          StackObj[] ret = new StackObj[] {new SODouble(x.getValue() - y.getValue()) };
          return ret;
        }
      }});
      fcns.put("mul",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.DOUBLE, FCalc.DOUBLE}; }
        public int[] rets() { return new int[] {FCalc.DOUBLE}; }
        public StackObj[] run(StackObj[] so) {
          SODouble x = (SODouble)so[0];
          SODouble y = (SODouble)so[1];
          StackObj[] ret = new StackObj[] {new SODouble(x.getValue() * y.getValue()) };
          return ret;
        }
      }});
      fcns.put("div",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.DOUBLE, FCalc.DOUBLE}; }
        public int[] rets() { return new int[] {FCalc.DOUBLE}; }
        public StackObj[] run(StackObj[] so) {
          SODouble x = (SODouble)so[0];
          SODouble y = (SODouble)so[1];
          StackObj[] ret = new StackObj[] {new SODouble(x.getValue() / y.getValue()) };
          return ret;
        }
      }});
      fcns.put("pm",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.DOUBLE }; }
        public int[] rets() { return new int[] {FCalc.DOUBLE}; }
        public StackObj[] run(StackObj[] so) {
          SODouble x = (SODouble)so[0];
          StackObj[] ret = new StackObj[] {new SODouble(-x.getValue()) };
          return ret;
        }
      }});
      fcns.put("inv",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.DOUBLE }; }
        public int[] rets() { return new int[] {FCalc.DOUBLE}; }
        public StackObj[] run(StackObj[] so) {
          SODouble x = (SODouble)so[0];
          StackObj[] ret = new StackObj[] {new SODouble(1/x.getValue()) };
          return ret;
        }
      }});
      fcns.put("eq",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.DOUBLE, FCalc.DOUBLE}; }
        public int[] rets() { return new int[] {FCalc.BOOL}; }
        public StackObj[] run(StackObj[] so) {
          SODouble x = (SODouble)so[0];
          SODouble y = (SODouble)so[1];
          StackObj[] ret = new StackObj[] {new SOBool(x.getValue() == y.getValue()) };
          return ret;
        }
      }});
      fcns.put("lt",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.DOUBLE, FCalc.DOUBLE}; }
        public int[] rets() { return new int[] {FCalc.BOOL}; }
        public StackObj[] run(StackObj[] so) {
          SODouble x = (SODouble)so[0];
          SODouble y = (SODouble)so[1];
          StackObj[] ret = new StackObj[] {new SOBool(x.getValue() < y.getValue()) };
          return ret;
        }
      }});
      fcns.put("gt",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.DOUBLE, FCalc.DOUBLE}; }
        public int[] rets() { return new int[] {FCalc.BOOL}; }
        public StackObj[] run(StackObj[] so) {
          SODouble x = (SODouble)so[0];
          SODouble y = (SODouble)so[1];
          StackObj[] ret = new StackObj[] {new SOBool(x.getValue() > y.getValue()) };
          return ret;
        }
      }});
      fcns.put("not",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.BOOL}; }
        public int[] rets() { return new int[] {FCalc.BOOL}; }
        public StackObj[] run(StackObj[] so) {
          SOBool x = (SOBool)so[0];
          StackObj[] ret = new StackObj[] {new SOBool(! x.getValue()) };
          return ret;
        }
      }});
      fcns.put("and",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.BOOL, FCalc.BOOL}; }
        public int[] rets() { return new int[] {FCalc.BOOL}; }
        public StackObj[] run(StackObj[] so) {
          SOBool x = (SOBool)so[0];
          SOBool y = (SOBool)so[1];
          StackObj[] ret = new StackObj[] {new SOBool(x.getValue() && y.getValue()) };
          return ret;
        }
      }});
      fcns.put("or",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.BOOL, FCalc.BOOL}; }
        public int[] rets() { return new int[] {FCalc.BOOL}; }
        public StackObj[] run(StackObj[] so) {
          SOBool x = (SOBool)so[0];
          SOBool y = (SOBool)so[1];
          StackObj[] ret = new StackObj[] {new SOBool(x.getValue() || y.getValue()) };
          return ret;
        }
      }});
      fcns.put("xor",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.BOOL, FCalc.BOOL}; }
        public int[] rets() { return new int[] {FCalc.BOOL}; }
        public StackObj[] run(StackObj[] so) {
          SOBool x = (SOBool)so[0];
          SOBool y = (SOBool)so[1];
          StackObj[] ret = new StackObj[] {new SOBool(x.getValue() ^ y.getValue()) };
          return ret;
        }
      }});
      fcns.put("rat",  new Executable[] { new Executable() {
        public int[] args() { return new int[] {FCalc.DOUBLE, FCalc.DOUBLE}; }
        public int[] rets() { return new int[] {FCalc.RAT}; }
        public StackObj[] run(StackObj[] so) {
          SODouble x = (SODouble)so[0];
          SODouble y = (SODouble)so[1];
          StackObj[] ret = new StackObj[] {new SORat(x, y) };
          return ret;
        }
      }});
      fcns.put("pi",  new Executable[] { new Executable() {
        public int[] args() { return new int[] { }; }
        public int[] rets() { return new int[] {FCalc.DOUBLE}; }
        public StackObj[] run(StackObj[] so) {
          StackObj[] ret = new StackObj[] { new SODouble(Math.PI) };
          return ret;
        }
      }});
    }
}
