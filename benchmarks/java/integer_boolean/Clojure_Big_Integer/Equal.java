import de.hhu.stups.btypes.BInteger;
import de.hhu.stups.btypes.BBoolean;

public class Equal {






    private BInteger counter;

    public Equal() {
        counter = (BInteger) new BInteger("0");
    }

    public void simulate() {
        while((counter.less(new BInteger("5000000")).and(new BInteger("1").equal(new BInteger("1")))).booleanValue()) {
            counter = (BInteger) counter.plus(new BInteger("1"));
        }
    }

    public static void main(String[] args) {
        Equal exec = new Equal();
        long start = System.nanoTime();
        exec.simulate();
        long end = System.nanoTime();
        System.out.println(exec.getClass().toString() + " Execution: " + (end - start));
    }

}
