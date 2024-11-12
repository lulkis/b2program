package de.hhu.stups.codegenerator.sablecc;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.codegenerator.CodeGenerator;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.hhu.stups.codegenerator.ast.SableCCBParser;
import de.hhu.stups.codegenerator.ast.nodes.MachineNodeWithDefinitions;
import de.prob.parser.antlr.Antlr4BParser;
import de.prob.parser.antlr.BProject;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.OperationNode;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestSableccAst {

    @Test
    public void testLift() throws Exception{
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/Lift.mch").toURI());


        BProject antlrProject  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        BProject sableProject = SableCCBParser.createBProjectFromMainMachineFile(mchPath.toFile());
        MachineNode antlrNode = antlrProject.getMainMachine();
        MachineNode sableNode = sableProject.getMainMachine();


        assertEquals(antlrNode.getName(), sableNode.getName());
        int c = 0;
        for(DeclarationNode p : antlrNode.getVariables()){
            assertEquals(p.toString(), sableNode.getVariables().get(c).toString());
            c++;
        }
        assertEquals(antlrNode.getInitialisation().toString(), sableNode.getInitialisation().toString());
        assertEquals(antlrNode.getInvariant().toString(), sableNode.getInvariant().toString());
        c = 0;
        for(OperationNode p : antlrNode.getOperations()){
            assertEquals(p.toString(), sableNode.getOperations().get(c).toString());
            c++;
        }
    }

    @Test
    public void testNota() throws Exception{
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/nota.mch").toURI());

        BProject antlrProject  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        BProject sableProject = SableCCBParser.createBProjectFromMainMachineFile(mchPath.toFile());
        MachineNode antlrNode = antlrProject.getMainMachine();
        MachineNode sableNode = sableProject.getMainMachine();

        assertEquals(antlrNode.getName(), sableNode.getName());
        assertEquals(antlrNode.getVariables().size(), sableNode.getVariables().size());
        assertEquals(antlrNode.getEnumeratedSets().size(), sableNode.getEnumeratedSets().size());
        assertEquals(antlrNode.getInitialisation().toString(), sableNode.getInitialisation().toString());
        assertEquals(antlrNode.getInvariant().toString(), sableNode.getInvariant().toString());
        assertEquals(antlrNode.getOperations().size(), sableNode.getOperations().size());
        assertEquals(antlrNode.getDefinitions().size(), sableNode.getDefinitions().size());
    }

    @Test
    public void testDefinitionTest() throws Exception{
        Path mchPathSableCC = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/DefTest.mch").toURI());
        Path mchPathANTLR = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/DefTestComp.mch").toURI());

        BProject antlrProject = Antlr4BParser.createBProjectFromMainMachineFile(mchPathANTLR.toFile());
        BProject sableProject = SableCCBParser.createBProjectFromMainMachineFile(mchPathSableCC.toFile());
        MachineNode antlrNode = antlrProject.getMainMachine();
        MachineNode sableNode = sableProject.getMainMachine();

        assertEquals(antlrNode.getVariables().size(), sableNode.getVariables().size());
        assertEquals(antlrNode.getEnumeratedSets().size(), sableNode.getEnumeratedSets().size());
        assertEquals(antlrNode.getInitialisation().toString(), sableNode.getInitialisation().toString());
        assertEquals(antlrNode.getInvariant().toString(), sableNode.getInvariant().toString());
        assertEquals(antlrNode.getOperations().size(), sableNode.getOperations().size());
    }

    @Test
    public void testLandingGear() throws Exception{
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/LandingGear_R6.mch").toURI());

        BProject antlrProject  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        BProject sableProject = SableCCBParser.createBProjectFromMainMachineFile(mchPath.toFile());
        MachineNode antlrNode = antlrProject.getMainMachine();
        MachineNode sableNode = sableProject.getMainMachine();

        assertEquals(antlrNode.getName(), sableNode.getName());
        assertEquals(antlrNode.getVariables().size(), sableNode.getVariables().size());
        assertEquals(antlrNode.getEnumeratedSets().size(), sableNode.getEnumeratedSets().size());
        assertEquals(antlrNode.getInitialisation().toString(), sableNode.getInitialisation().toString());
        assertEquals(antlrNode.getInvariant().toString(), sableNode.getInvariant().toString());
        assertEquals(antlrNode.getOperations().size(), sableNode.getOperations().size());
        assertEquals(antlrNode.getDefinitions().size(), sableNode.getDefinitions().size());
    }

    @Test
    public void testCruiseControl() throws Exception{
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/Cruise_finite1.mch").toURI());

        BProject antlrProject  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        BProject sableProject = SableCCBParser.createBProjectFromMainMachineFile(mchPath.toFile());
        MachineNode antlrNode = antlrProject.getMainMachine();
        MachineNode sableNode = sableProject.getMainMachine();

        assertEquals(antlrNode.getName(), sableNode.getName());
        assertEquals(antlrNode.getVariables().size(), sableNode.getVariables().size());
        assertEquals(antlrNode.getEnumeratedSets().size(), sableNode.getEnumeratedSets().size());
        assertEquals(antlrNode.getInitialisation().toString(), sableNode.getInitialisation().toString());
        assertEquals(antlrNode.getInvariant().toString(), sableNode.getInvariant().toString());
        assertEquals(antlrNode.getOperations().size(), sableNode.getOperations().size());
        assertEquals(antlrNode.getDefinitions().size(), sableNode.getDefinitions().size());
    }

    @Test
    public void testGenerated100() throws Exception{
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/Generated100.mch").toURI());

        BProject antlrProject  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        BProject sableProject = SableCCBParser.createBProjectFromMainMachineFile(mchPath.toFile());
        MachineNode antlrNode = antlrProject.getMainMachine();
        MachineNode sableNode = sableProject.getMainMachine();

        assertEquals(antlrNode.getName(), sableNode.getName());
        assertEquals(antlrNode.getVariables().size(), sableNode.getVariables().size());
        assertEquals(antlrNode.getEnumeratedSets().size(), sableNode.getEnumeratedSets().size());
        assertEquals(antlrNode.getInitialisation().toString(), sableNode.getInitialisation().toString());
        assertEquals(antlrNode.getInvariant().toString(), sableNode.getInvariant().toString());
        assertEquals(antlrNode.getOperations().size(), sableNode.getOperations().size());
        assertEquals(antlrNode.getDefinitions().size(), sableNode.getDefinitions().size());
    }

    @Test
    public void testTrafficLight() throws Exception{
        Path mchPath = Paths.get(CodeGenerator.class.getClassLoader()
                .getResource("de/hhu/stups/codegenerator/sablecc/TrafficLight.mch").toURI());

        BProject antlrProject  = Antlr4BParser.createBProjectFromMainMachineFile(mchPath.toFile());
        BProject sableProject = SableCCBParser.createBProjectFromMainMachineFile(mchPath.toFile());
        MachineNode antlrNode = antlrProject.getMainMachine();
        MachineNode sableNode = sableProject.getMainMachine();

        assertEquals(antlrNode.getName(), sableNode.getName());
        assertEquals(antlrNode.getVariables().size(), sableNode.getVariables().size());
        assertEquals(antlrNode.getEnumeratedSets().size(), sableNode.getEnumeratedSets().size());
        assertEquals(antlrNode.getInitialisation().toString(), sableNode.getInitialisation().toString());
        assertEquals(antlrNode.getInvariant().toString(), sableNode.getInvariant().toString());
        assertEquals(antlrNode.getOperations().size(), sableNode.getOperations().size());
        assertEquals(antlrNode.getDefinitions().size(), sableNode.getDefinitions().size());
    }
}
