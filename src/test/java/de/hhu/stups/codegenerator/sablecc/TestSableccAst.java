package de.hhu.stups.codegenerator.sablecc;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.codegenerator.CodeGenerator;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.ast.nodes.MachineNode;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestSableccAst {

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
}
