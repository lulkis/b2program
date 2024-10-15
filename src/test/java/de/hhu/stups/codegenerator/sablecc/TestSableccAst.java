package de.hhu.stups.codegenerator.sablecc;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.codegenerator.CodeGenerator;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.antlr.Antlr4BParser;
import de.prob.parser.antlr.BProject;
import de.prob.parser.ast.nodes.MachineNode;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestSableccAst {

    @Test
    public void testCorrectMachineName() throws Exception{
        BParser parser = new BParser();
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/Lift.mch").toURI());
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode;

        machineNode = coordinator.convertMachineNode(start);

        assertEquals("Lift", machineNode.getName());
    }

    @Test
    public void testVariables() throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/Lift.mch").toURI());
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode;

        machineNode = coordinator.convertMachineNode(start);

        assertEquals(1, machineNode.getVariables().size());
    }

    @Test
    public void testInitialisation() throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/Lift.mch").toURI());
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode;

        machineNode = coordinator.convertMachineNode(start);

        assertEquals("counter:=0", machineNode.getInitialisation().toString());
    }

    @Test
    public void testInvariant() throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/Lift.mch").toURI());
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode;

        machineNode = coordinator.convertMachineNode(start);

        assertEquals("ELEMENT_OF(counter,INTERVAL(0,100))", machineNode.getInvariant().toString());
    }

    @Test
    public void testOperations() throws Exception{
        BParser parser = new BParser();
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/Lift.mch").toURI());
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode;

        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = coordinator.convertMachineNode(start);

        assertEquals(antlrMachineNode.getMachineNode("Lift").getOperations().get(0).toString(),
                machineNode.getOperations().get(0).toString());
    }
}
