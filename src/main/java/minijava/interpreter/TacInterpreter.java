package minijava.interpreter;

import minijava.ir.Operand;
import minijava.ir.TacInstruction;
import minijava.ir.TacProgram;

import java.util.*;

/**
 * Direct virtual execution engine for Three-Address Code (TAC) programs.
 */
public class TacInterpreter {
    private final Map<String, Integer> registerStore = new LinkedHashMap<>();
    private final List<String> outputLines = new ArrayList<>();

    public ExecutionResult execute(TacProgram program) {
        registerStore.clear();
        outputLines.clear();

        long startTime = System.nanoTime();
        int stepCount = 0;

        for (TacInstruction inst : program.instructions()) {
            stepCount++;
            executeInstruction(inst);
        }
        long duration = System.nanoTime() - startTime;

        // Filter user variables from compiler temporaries for the clean user-facing environment
        Map<String, Integer> userVariables = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : registerStore.entrySet()) {
            if (!entry.getKey().startsWith("t") || entry.getKey().length() <= 1 || !Character.isDigit(entry.getKey().charAt(1))) {
                userVariables.put(entry.getKey(), entry.getValue());
            }
        }

        return new ExecutionResult(outputLines, userVariables, stepCount, duration);
    }

    private void executeInstruction(TacInstruction inst) {
        if (inst instanceof TacInstruction.Assign assign) {
            int value = resolve(assign.src());
            store(assign.dest(), value);
        } else if (inst instanceof TacInstruction.Binary binary) {
            int left = resolve(binary.left());
            int right = resolve(binary.right());
            int result = switch (binary.op()) {
                case ADD -> left + right;
                case SUB -> left - right;
                case MUL -> left * right;
                case DIV -> {
                    if (right == 0) {
                        throw new ArithmeticException("Division by zero in TAC instruction: " + binary.toTacString());
                    }
                    yield left / right;
                }
                case MOD -> {
                    if (right == 0) {
                        throw new ArithmeticException("Modulo by zero in TAC instruction: " + binary.toTacString());
                    }
                    yield left % right;
                }
            };
            store(binary.dest(), result);
        } else if (inst instanceof TacInstruction.Unary unary) {
            int operand = resolve(unary.operand());
            int result = switch (unary.op()) {
                case NEG -> -operand;
                case POS -> operand;
            };
            store(unary.dest(), result);
        } else if (inst instanceof TacInstruction.Print print) {
            int value = resolve(print.operand());
            outputLines.add(String.valueOf(value));
        } else {
            throw new IllegalArgumentException("Unknown TAC instruction type: " + inst);
        }
    }

    private int resolve(Operand operand) {
        if (operand instanceof Operand.Constant(int val)) {
            return val;
        }
        if (operand instanceof Operand.Variable(String name)) {
            Integer val = registerStore.get(name);
            if (val == null) {
                throw new IllegalStateException("Runtime error: Variable '" + name + "' read before assignment");
            }
            return val;
        }
        if (operand instanceof Operand.Temporary(int id)) {
            String tempKey = "t" + id;
            Integer val = registerStore.get(tempKey);
            if (val == null) {
                throw new IllegalStateException("Runtime error: Temporary register '" + tempKey + "' read before assignment");
            }
            return val;
        }
        throw new IllegalArgumentException("Unsupported operand type: " + operand);
    }

    private void store(Operand.Dest dest, int value) {
        if (dest instanceof Operand.Variable(String name)) {
            registerStore.put(name, value);
        } else if (dest instanceof Operand.Temporary(int id)) {
            registerStore.put("t" + id, value);
        } else {
            throw new IllegalArgumentException("Unsupported destination: " + dest);
        }
    }

    public Map<String, Integer> getFullStore() {
        return Collections.unmodifiableMap(registerStore);
    }
}
