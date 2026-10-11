package minijava.ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates a sequence of Three-Address Code instructions representing a program.
 */
public class TacProgram {
    private final List<TacInstruction> instructions;

    public TacProgram(List<TacInstruction> instructions) {
        Objects.requireNonNull(instructions, "Instructions cannot be null");
        this.instructions = Collections.unmodifiableList(new ArrayList<>(instructions));
    }

    public List<TacInstruction> instructions() {
        return instructions;
    }

    public int size() {
        return instructions.size();
    }

    public boolean isEmpty() {
        return instructions.isEmpty();
    }

    public TacInstruction get(int index) {
        return instructions.get(index);
    }

    public String toPrettyString() {
        if (instructions.isEmpty()) {
            return "// (empty TAC program)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < instructions.size(); i++) {
            sb.append(String.format("%3d:  %s\n", i + 1, instructions.get(i).toTacString()));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TacProgram that)) return false;
        return instructions.equals(that.instructions);
    }

    @Override
    public int hashCode() {
        return instructions.hashCode();
    }

    @Override
    public String toString() {
        return toPrettyString();
    }
}
