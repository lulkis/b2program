package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.AOperation;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PExpression;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.OperationNode;

import java.util.ArrayList;
import java.util.List;

public class OperationVisitor extends AbstractVisitor{

    private OperationNode resultOperationNode;
    private VisitorCoordinator coordinator = new VisitorCoordinator();
    private MachineNode machineNode;

    public OperationVisitor(MachineNode machineNode) {
        this.machineNode = machineNode;
    }
    
    public OperationNode getResult(){
        return resultOperationNode;
    }

    @Override
    public void caseAOperation(AOperation node){
        List<DeclarationNode> paramNodes = new ArrayList<>();
        for(PExpression expression : node.getParameters()){
            paramNodes.add(new DeclarationNode(getSourceCodePosition(expression),
                    expression.toString().replace(" ", ""),
                    DeclarationNode.Kind.OP_INPUT_PARAMETER,
                    machineNode));
        }
        resultOperationNode = new OperationNode(getSourceCodePosition(node),
                node.getOpName().get(0).getText(),
                new ArrayList<>(),
                coordinator.convertSubstitutionNode(node.getOperationBody(), machineNode),
                paramNodes);
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
