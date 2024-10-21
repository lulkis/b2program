import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 1)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
@OutputTimeUnit(TimeUnit.SECONDS)
public class SableCCBenchmark {

    @Benchmark
    public static void firstBenchmark() {
        // do something
    }
}
