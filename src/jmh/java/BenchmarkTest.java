package de.hhu.stups.codegenerator;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.antlr.Antlr4BParser;
import de.prob.parser.antlr.BProject;
import de.prob.parser.ast.nodes.MachineNode;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
public class BenchmarkTest {

    @Benchmark
    public void sableBenchmarkLift(Blackhole blackhole) throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get(ClassLoader.getSystemResource("Lift.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode = coordinator.convertMachineNode(start);
        blackhole.consume(machineNode);
    }

    @Benchmark
    public void antlrBenchmarkLift(Blackhole blackhole) throws Exception {
        Path mchPath = Paths.get(ClassLoader.getSystemResource("Lift.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));
        MachineNode machineNode;
        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = antlrMachineNode.getMachineNode("Lift");
        blackhole.consume(machineNode);
    }

    @Benchmark
    public void sableBenchmarkNota(Blackhole blackhole) throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get(ClassLoader.getSystemResource("nota.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode = coordinator.convertMachineNode(start);
        blackhole.consume(machineNode);
    }

    @Benchmark
    public void antlrBenchmarkNota(Blackhole blackhole) throws Exception {
        Path mchPath = Paths.get(ClassLoader.getSystemResource("nota.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));
        MachineNode machineNode;
        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = antlrMachineNode.getMachineNode("nota");
        blackhole.consume(machineNode);
    }
}
