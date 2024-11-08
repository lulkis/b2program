package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.AMachineReference;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.MachineReferenceNode;

public class ReferenceVisitor extends AbstractVisitor{

    private MachineReferenceNode resultReference;
    private MachineNode machineNode;

    public ReferenceVisitor(MachineNode machineNode) {
        this.machineNode = machineNode;
    }

    public MachineReferenceNode getResult(){
        return resultReference;
    }

    @Override
    public void caseAMachineReference(AMachineReference node) {
        for(TIdentifierLiteral idf : node.getMachineName()){
            machineNode.addMachineReferenceNode(new MachineReferenceNode(getSourceCodePosition(node),
                    idf.toString().replace(" ", ""),
                    MachineReferenceNode.Kind.INCLUDED,
                    null,
                    false));
        }
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
