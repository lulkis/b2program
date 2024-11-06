package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.*;
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

        List<DeclarationNode> outputParams = new ArrayList<>();
        for(PExpression expression : node.getReturnValues()){
            paramNodes.add(new DeclarationNode(getSourceCodePosition(expression),
                    expression.toString().replace(" ", ""),
                    DeclarationNode.Kind.OP_OUTPUT_PARAMETER,
                    machineNode));
        }

        resultOperationNode = new OperationNode(getSourceCodePosition(node),
                node.getOpName().get(0).getText(),
                outputParams,
                coordinator.convertSubstitutionNode(node.getOperationBody(), machineNode),
                paramNodes);
    }

    @Override
    public void caseAComputationOperation(AComputationOperation node){
        //TODO: Translation Computation Operation
    }

    @Override
    public void caseADescriptionOperation(ADescriptionOperation node){
        //TODO: Translation Description Operation
    }

    @Override
    public void caseAFunctionOperation(AFunctionOperation node){
        //TODO: Translation Function Operation
    }

    @Override
    public void caseAMissingSemicolonOperation(AMissingSemicolonOperation node){
        //TODO: Translation Missing Semicolon Operation
    }

    @Override
    public void caseARefinedOperation(ARefinedOperation node){
        //TODO: Translation Refined Operation
    }

    @Override
    public void caseARuleOperation(ARuleOperation node){
        //TODO: Translation Rule Operation
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
