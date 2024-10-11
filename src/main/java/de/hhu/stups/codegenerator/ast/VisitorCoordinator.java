package de.hhu.stups.codegenerator.ast;

import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.codegenerator.ast.adapter.MachineVisitor;
import de.prob.parser.ast.nodes.MachineNode;

public class VisitorCoordinator {

    public MachineNode convertMachineNode(Start start){
        MachineVisitor visitor = new MachineVisitor();
        start.apply(visitor);
        return visitor.getResult();
    }
}
