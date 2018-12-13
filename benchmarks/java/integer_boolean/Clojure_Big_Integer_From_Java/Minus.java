import de.hhu.stups.btypes.BInteger;
import de.hhu.stups.btypes.BBoolean;

public class Minus {






    private BInteger counter;
    private BInteger value;

    public Minus() {
        counter = (BInteger) new BInteger("0");
        value = (BInteger) new BInteger("0");
    }

    public void simulate() {
        while((counter.less(new BInteger("5000000"))).booleanValue()) {
            counter = (BInteger) counter.plus(new BInteger("1"));
            value = (BInteger) value.minus(new BInteger("1"));
        }
    }

    public static void main(String[] args) {
        Minus exec = new Minus();
        long start = System.nanoTime();
        exec.simulate();
        long end = System.nanoTime();
        System.out.println(exec.getClass().toString() + " Execution: " + (end - start));
    }

}
