package minijava.ir;

import minijava.ast.BinaryOp;
import minijava.ast.UnaryOp;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Three-Address Code (TAC) instruction hierarchy.
 */
public sealed interface TacInstruction {

    /**
     * Target variable or temporary updated by this instruction, if any.
     */
    Optional<Operand.Dest> destination();

    /**
     * All operands read/used by this instruction.
     */
    List<Operand> uses();

    /**
     * Replaces operands used in this instruction according to the substitution function.
     */
    TacInstruction replaceUses(java.util.function.Function<Operand, Operand> mapper);

    /**
     * Formats the instruction as readable TAC assembly.
     */
    String toTacString();

    /**
     * Copy assignment: {@code dest = src}.
     */
    record Assign(Operand.Dest dest, Operand src) implements TacInstruction {
        public Assign {
            Objects.requireNonNull(dest, "Destination cannot be null");
            Objects.requireNonNull(src, "Source operand cannot be null");
        }

        @Override
        public Optional<Operand.Dest> destination() {
            return Optional.of(dest);
        }

        @Override
        public List<Operand> uses() {
            return List.of(src);
        }

        @Override
        public TacInstruction replaceUses(java.util.function.Function<Operand, Operand> mapper) {
            Operand newSrc = mapper.apply(src);
            return (newSrc.equals(src)) ? this : new Assign(dest, newSrc);
        }

        @Override
        public String toTacString() {
            return dest + " = " + src;
        }

        @Override
        public String toString() {
            return toTacString();
        }
    }

    /**
     * Binary arithmetic instruction: {@code dest = left op right}.
     */
    record Binary(Operand.Dest dest, BinaryOp op, Operand left, Operand right) implements TacInstruction {
        public Binary {
            Objects.requireNonNull(dest, "Destination cannot be null");
            Objects.requireNonNull(op, "Operator cannot be null");
            Objects.requireNonNull(left, "Left operand cannot be null");
            Objects.requireNonNull(right, "Right operand cannot be null");
        }

        @Override
        public Optional<Operand.Dest> destination() {
            return Optional.of(dest);
        }

        @Override
        public List<Operand> uses() {
            return List.of(left, right);
        }

        @Override
        public TacInstruction replaceUses(java.util.function.Function<Operand, Operand> mapper) {
            Operand newLeft = mapper.apply(left);
            Operand newRight = mapper.apply(right);
            if (newLeft.equals(left) && newRight.equals(right)) {
                return this;
            }
            return new Binary(dest, op, newLeft, newRight);
        }

        @Override
        public String toTacString() {
            return dest + " = " + left + " " + op + " " + right;
        }

        @Override
        public String toString() {
            return toTacString();
        }
    }

    /**
     * Unary arithmetic instruction: {@code dest = op operand}.
     */
    record Unary(Operand.Dest dest, UnaryOp op, Operand operand) implements TacInstruction {
        public Unary {
            Objects.requireNonNull(dest, "Destination cannot be null");
            Objects.requireNonNull(op, "Operator cannot be null");
            Objects.requireNonNull(operand, "Operand cannot be null");
        }

        @Override
        public Optional<Operand.Dest> destination() {
            return Optional.of(dest);
        }

        @Override
        public List<Operand> uses() {
            return List.of(operand);
        }

        @Override
        public TacInstruction replaceUses(java.util.function.Function<Operand, Operand> mapper) {
            Operand newOp = mapper.apply(operand);
            return (newOp.equals(operand)) ? this : new Unary(dest, op, newOp);
        }

        @Override
        public String toTacString() {
            return dest + " = " + op + operand;
        }

        @Override
        public String toString() {
            return toTacString();
        }
    }

    /**
     * Print instruction: {@code print operand}.
     */
    record Print(Operand operand) implements TacInstruction {
        public Print {
            Objects.requireNonNull(operand, "Operand cannot be null");
        }

        @Override
        public Optional<Operand.Dest> destination() {
            return Optional.empty();
        }

        @Override
        public List<Operand> uses() {
            return List.of(operand);
        }

        @Override
        public TacInstruction replaceUses(java.util.function.Function<Operand, Operand> mapper) {
            Operand newOp = mapper.apply(operand);
            return (newOp.equals(operand)) ? this : new Print(newOp);
        }

        @Override
        public String toTacString() {
            return "print " + operand;
        }

        @Override
        public String toString() {
            return toTacString();
        }
    }
}
