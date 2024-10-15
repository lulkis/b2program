package de.hhu.stups.codegenerator.sablecc;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.codegenerator.CodeGenerator;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.hhu.stups.codegenerator.ast.nodes.MachineNodeWithDefinitions;
import de.prob.parser.antlr.Antlr4BParser;
import de.prob.parser.antlr.BProject;
import de.prob.parser.ast.nodes.MachineNode;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestSableccAst {

    @Test
    public void testCorrectMachineNameLift() throws Exception{
        BParser parser = new BParser();
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/Lift.mch").toURI());
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode;

        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = coordinator.convertMachineNode(start);

        assertEquals(antlrMachineNode.getMachineNode("Lift").getName(),
                machineNode.getName());
    }

    @Test
    public void testVariablesLift() throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/Lift.mch").toURI());
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode;

        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = coordinator.convertMachineNode(start);

        assertEquals(antlrMachineNode.getMachineNode("Lift").getVariables().size(),
                machineNode.getVariables().size());
    }

    @Test
    public void testInitialisationLift() throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/Lift.mch").toURI());
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode;

        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = coordinator.convertMachineNode(start);

        assertEquals(antlrMachineNode.getMachineNode("Lift").getInitialisation().toString(),
                machineNode.getInitialisation().toString());
    }

    @Test
    public void testInvariantLift() throws Exception {
        BParser parser = new BParser();
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/Lift.mch").toURI());
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode;

        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = coordinator.convertMachineNode(start);

        assertEquals(antlrMachineNode.getMachineNode("Lift").getInvariant().toString(),
                machineNode.getInvariant().toString());
    }

    @Test
    public void testOperationsLift() throws Exception{
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

    @Test
    public void testCorrectMachineNameNota() throws Exception{
        BParser parser = new BParser();
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/nota.mch").toURI());
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode;

        //BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = coordinator.convertMachineNode(start);

        assertEquals("nota",
                machineNode.getName());
    }

    @Test
    public void testInvariantNota() throws Exception{
        BParser parser = new BParser();
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/nota.mch").toURI());
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();
        MachineNode machineNode;

        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = coordinator.convertMachineNode(start);

        assertEquals(antlrMachineNode.getMachineNode("nota").getInvariant().toString(),
                machineNode.getInvariant().toString());
    }

    @Test
    public void testCorrectMachineNameDefTest() throws Exception{
        BParser parser = new BParser();
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/DefTest.mch").toURI());
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();

        MachineNodeWithDefinitions machineNode;

        //BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        machineNode = (MachineNodeWithDefinitions) coordinator.convertMachineNode(start, parser.getDefinitions());

        assertEquals("DefTest",
                machineNode.getName());
    }

    @Test
    public void testCorrectMachineNameDefTestExchange() throws Exception{
        BParser parser = new BParser();
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/DefTest.mch").toURI());
        Path mchPathCompare = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/DefTestComp.mch").toURI());
        Start start = parser.parseFile(mchPath.toFile());
        VisitorCoordinator coordinator = new VisitorCoordinator();

        MachineNodeWithDefinitions machineNode;

        BProject antlrMachineNode  = Antlr4BParser.createBProjectFromMainMachineFile(mchPathCompare.toFile());
        machineNode = (MachineNodeWithDefinitions) coordinator.convertMachineNode(start, parser.getDefinitions());

        assertEquals(antlrMachineNode.getMachineNode("DefTestComp").getOperations().get(1).toString(),
                machineNode.getOperations().get(1).toString());
    }
}
