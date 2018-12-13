import de.hhu.stups.btypes.BSet;
import de.hhu.stups.btypes.BInteger;
import de.hhu.stups.btypes.BBoolean;
import de.hhu.stups.btypes.BUtils;

public class RangeUnionBig {






    private BInteger counter;
    private BSet set;

    public RangeUnionBig() {
        counter = (BInteger) new BInteger(0);
        set = (BSet) BSet.range(new BInteger(1),new BInteger(25000));
    }

    public void simulate() {
        while((counter.less(new BInteger(10000))).booleanValue()) {
            set = (BSet) set.union(BSet.range(new BInteger(1),new BInteger(3000)));
            counter = (BInteger) counter.plus(new BInteger(1));
        }
    }

    public static void main(String[] args) {
        RangeUnionBig exec = new RangeUnionBig();
        long start = System.nanoTime();
        exec.simulate();
        long end = System.nanoTime();
        System.out.println(exec.getClass().toString() + " Execution: " + (end - start));
    }

}
