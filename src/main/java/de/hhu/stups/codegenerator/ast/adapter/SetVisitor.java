package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.ADeferredSetSet;
import de.be4.classicalb.core.parser.node.AEnumeratedSetSet;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PExpression;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.EnumeratedSetDeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;

import java.util.ArrayList;
import java.util.List;

public class SetVisitor extends AbstractVisitor{

    private de.prob.parser.ast.nodes.Node resultSetDeclarationNode;
    private MachineNode machineNode;

    public SetVisitor(MachineNode machineNode) {
        this.machineNode = machineNode;
    }

    public de.prob.parser.ast.nodes.Node getResult(){
        return resultSetDeclarationNode;
    }

    @Override
    public void caseAEnumeratedSetSet(AEnumeratedSetSet node){
        SourceCodePosition position = getSourceCodePosition(node);
        DeclarationNode declarationNode = new DeclarationNode(position,
                node.getIdentifier().get(0).toString().replace(" ", ""),
                DeclarationNode.Kind.ENUMERATED_SET, machineNode);
        resultSetDeclarationNode = new EnumeratedSetDeclarationNode(position,
                declarationNode,
                createDeclarationList(node.getElements(), DeclarationNode.Kind.ENUMERATED_SET_ELEMENT));
    }

    @Override
    public void caseADeferredSetSet(ADeferredSetSet node){
        resultSetDeclarationNode = new DeclarationNode(getSourceCodePosition(node),
                node.toString().replace(" ", ""),
                DeclarationNode.Kind.DEFERRED_SET,
                machineNode);
    }

    private List<DeclarationNode> createDeclarationList(List<PExpression> list, DeclarationNode.Kind kind) {
        List<DeclarationNode> declarationList = new ArrayList<>();
        for (PExpression terminalNode : list) {
            DeclarationNode declNode = new DeclarationNode(getSourceCodePosition(terminalNode),
                    terminalNode.toString().replace(" ", ""), kind, machineNode);
            declarationList.add(declNode);
        }
        return declarationList;
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
