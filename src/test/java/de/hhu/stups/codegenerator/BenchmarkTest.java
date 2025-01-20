package de.hhu.stups.codegenerator;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.antlr.Antlr4BParser;
import de.prob.parser.antlr.BProject;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.visitors.MachineScopeChecker;
import de.prob.parser.ast.visitors.TypeChecker;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3)
@Measurement(iterations = 11)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
public class BenchmarkTest {

    //Translation of NoTa
    @Benchmark
    public void sableBenchmarkNota(Blackhole blackhole) throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get("src/jmh/resources/Cruise_finite1.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("nota.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode = coordinator.convertMachineNode(start);
        new MachineScopeChecker(machineNode);
        new TypeChecker(machineNode);
        blackhole.consume(machineNode);
    }

    @Benchmark
    public void antlrBenchmarkNota(Blackhole blackhole) throws Exception {
        Path mchPath = Paths.get("src/jmh/resources/nota.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("nota.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        MachineNode machineNode;
        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = antlrMachineNode.getMachineNode("nota");
        blackhole.consume(machineNode);
    }

    @Benchmark
    public void sableBenchmarkNotaBare(Blackhole blackhole) throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get("src/jmh/resources/nota.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("nota.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        Start start = parser.parseFile(mchPath.toFile());
        blackhole.consume(start);
    }

    //Translation of Landing Gear
    @Benchmark
    public void sableBenchmarkLandingGear(Blackhole blackhole) throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get("src/jmh/resources/LandingGear_R6.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("LandingGear_R6.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode = coordinator.convertMachineNode(start);
        new MachineScopeChecker(machineNode);
        new TypeChecker(machineNode);
        blackhole.consume(machineNode);
    }

    @Benchmark
    public void antlrBenchmarkLandingGear(Blackhole blackhole) throws Exception {
        Path mchPath = Paths.get("src/jmh/resources/LandingGear_R6.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("LandingGear_R6.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        MachineNode machineNode;
        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = antlrMachineNode.getMachineNode("LandingGear_R6");
        blackhole.consume(machineNode);
    }

    @Benchmark
    public void sableBenchmarkLandingGearBare(Blackhole blackhole) throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get("src/jmh/resources/LandingGear_R6.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("LandingGear_R6.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        Start start = parser.parseFile(mchPath.toFile());
        blackhole.consume(start);
    }

    //Translation of Cruise Control
    @Benchmark
    public void sableBenchmarkCruise(Blackhole blackhole) throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get("src/jmh/resources/Cruise_finite1.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("Cruise_finite1.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode = coordinator.convertMachineNode(start);
        new MachineScopeChecker(machineNode);
        new TypeChecker(machineNode);
        blackhole.consume(machineNode);
    }

    @Benchmark
    public void antlrBenchmarkCruise(Blackhole blackhole) throws Exception {
        Path mchPath = Paths.get("src/jmh/resources/Cruise_finite1.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("Cruise_finite1.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        MachineNode machineNode;
        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = antlrMachineNode.getMachineNode("Cruise_finite1");
        blackhole.consume(machineNode);
    }

    @Benchmark
    public void sableBenchmarkCruiseBare(Blackhole blackhole) throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get("src/jmh/resources/Cruise_finite1.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("Cruise_finite1.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        Start start = parser.parseFile(mchPath.toFile());
        blackhole.consume(start);
    }


    //Translation of Generated 1000
    @Benchmark
    public void sableBenchmarkGenerated1000(Blackhole blackhole) throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get("src/jmh/resources/generated_1000.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("generated_1000.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode = coordinator.convertMachineNode(start);
        new MachineScopeChecker(machineNode);
        new TypeChecker(machineNode);
        blackhole.consume(machineNode);
    }

    @Benchmark
    public void antlrBenchmarkGenerated1000(Blackhole blackhole) throws Exception {
        Path mchPath = Paths.get("src/jmh/resources/generated_1000.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("generated_1000.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        MachineNode machineNode;
        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = antlrMachineNode.getMachineNode("generated_1000");
        blackhole.consume(machineNode);
    }


    //Translation of Generated 2000
    @Benchmark
    public void sableBenchmarkGenerated2000(Blackhole blackhole) throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get("src/jmh/resources/generated_2000.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("generated_2000.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode = coordinator.convertMachineNode(start);
        new MachineScopeChecker(machineNode);
        new TypeChecker(machineNode);
        blackhole.consume(machineNode);
    }

    @Benchmark
    public void antlrBenchmarkGenerated2000(Blackhole blackhole) throws Exception {
        Path mchPath = Paths.get("src/jmh/resources/generated_2000.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("generated_2000.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        MachineNode machineNode;
        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = antlrMachineNode.getMachineNode("generated_2000");
        blackhole.consume(machineNode);

    }

    @Benchmark
    public void antlrBenchmarkExplicitComputations(Blackhole blackhole) throws Exception {
        Path mchPath = Paths.get("src/jmh/resources/ExplicitComputations.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("generated_2000.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        MachineNode machineNode;
        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = antlrMachineNode.getMachineNode("ExplicitComputations");
        blackhole.consume(machineNode);

    }

    @Benchmark
    public void sableBenchmarkExplicitComputations(Blackhole blackhole) throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get("src/jmh/resources/ExplicitComputations.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("generated_2000.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode = coordinator.convertMachineNode(start);
        new MachineScopeChecker(machineNode);
        new TypeChecker(machineNode);
        blackhole.consume(machineNode);
    }

    @Benchmark
    public void sableBenchmarkExplicitComputationsBare(Blackhole blackhole) throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get("src/jmh/resources/ExplicitComputations.mch");
        /*Path mchPath = Paths.get(ClassLoader.getSystemResource("nota.mch").toString()
                .replace("jar:file:/", "")
                .replace("build/libs/B2Program-0.1.0-SNAPSHOT-jmh.jar!", "src/jmh/resources"));*/
        Start start = parser.parseFile(mchPath.toFile());
        blackhole.consume(start);
    }
}
