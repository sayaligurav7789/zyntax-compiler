package minijava.ir;

/**
 * Generator for unique sequential compiler temporaries (_t0, _t1, ...).
 */
public class TempGenerator {
    private int counter = 0;

    public Operand.Temporary nextTemp() {
        return new Operand.Temporary(counter++);
    }

    public void reset() {
        counter = 0;
    }

    public int count() {
        return counter;
    }
}
