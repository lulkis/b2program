import de.hhu.stups.btypes.BInteger;
import de.hhu.stups.btypes.BBoolean;

public class LiftExec {



    private Lift _Lift = new Lift();



    private BInteger counter;

    public LiftExec() {
        counter = (BInteger) new BInteger("0");
    }

    public void simulate() {
        while((counter.less(new BInteger("3000"))).booleanValue()) {
            BInteger i = null;
            i = (BInteger) new BInteger("0");
            while((i.less(new BInteger("100"))).booleanValue()) {
                this._Lift.inc();
                i = (BInteger) i.plus(new BInteger("1"));
            }
            BInteger _i = null;
            _i = (BInteger) new BInteger("0");
            while((_i.less(new BInteger("100"))).booleanValue()) {
                this._Lift.dec();
                _i = (BInteger) _i.plus(new BInteger("1"));
            }
            counter = (BInteger) counter.plus(new BInteger("1"));
        }
    }

    public static void main(String[] args) {
        LiftExec exec = new LiftExec();
        long start = System.nanoTime();
        exec.simulate();
        long end = System.nanoTime();
        System.out.println(exec.getClass().toString() + " Execution: " + (end - start));
    }

}