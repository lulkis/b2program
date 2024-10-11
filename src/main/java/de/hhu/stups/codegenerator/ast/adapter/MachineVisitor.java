package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.Node;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.MachineNode;

public class MachineVisitor extends AbstractVisitor{

    private MachineNode resultMachineNode;

    public MachineNode getResult(){
        return resultMachineNode;
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
