package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.AMachineReference;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.MachineReferenceNode;


/*
Der Visitor zum übersetzen der References. Gibt einen Reference Knoten zurück.
 */
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
        SourceCodePosition sourceCodePosition = new SourceCodePosition(node.getStartPos() != null ? node.getStartPos().getLine(): 0, node.getStartPos() != null ? node.getStartPos().getPos() : 0, node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
