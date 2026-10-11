package minijava.ir;

import java.util.Objects;

/**
 * Operands used in Three-Address Code (TAC) instructions.
 */
public sealed interface Operand {

    /**
     * Marker interface for operands that can serve as assignment targets.
     */
    sealed interface Dest extends Operand permits Variable, Temporary {
    }

    /**
     * Compile-time 32-bit integer constant operand.
     */
    record Constant(int value) implements Operand {
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    /**
     * Program variable operand defined in MiniJava source code.
     */
    record Variable(String name) implements Dest {
        public Variable {
            Objects.requireNonNull(name, "Variable name cannot be null");
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Compiler-generated temporary register operand (e.g. t0, t1, ...).
     */
    record Temporary(int id) implements Dest {
        public Temporary {
            if (id < 0) {
                throw new IllegalArgumentException("Temporary id must be non-negative: " + id);
            }
        }

        @Override
        public String toString() {
            return "t" + id;
        }
    }
}
