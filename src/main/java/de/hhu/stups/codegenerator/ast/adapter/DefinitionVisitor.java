package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.DefinitionNode;
import de.prob.parser.ast.nodes.MachineNode;

import java.util.ArrayList;
import java.util.List;

public class DefinitionVisitor extends AbstractVisitor{

    private DefinitionNode resultDefinitionNode;
    private VisitorCoordinator coordinator = new VisitorCoordinator();
    private MachineNode machineNode;

    public DefinitionVisitor(MachineNode machineNode) {
        this.machineNode = machineNode;
    }

    public DefinitionNode getResult() {
        return resultDefinitionNode;
    }

    @Override
    public void caseAExpressionDefinitionDefinition(AExpressionDefinitionDefinition node) {
        List<DeclarationNode> declarationList = new ArrayList<>();
        for (PExpression terminalNode : node.getParameters()) {
            DeclarationNode declNode = new DeclarationNode(getSourceCodePos(terminalNode),
                    terminalNode.toString().replace(" ", ""),
                    DeclarationNode.Kind.OP_INPUT_PARAMETER,
                    machineNode);
            declarationList.add(declNode);
        }

        resultDefinitionNode = new DefinitionNode(getSourceCodePos(node),
                node.getName().toString().replace(" ", ""),
                declarationList,
                coordinator.convertExpressionNode(node.getRhs()));
    }

    @Override
    public void caseAPredicateDefinitionDefinition(APredicateDefinitionDefinition node){
        List<DeclarationNode> declarationList = new ArrayList<>();
        for (PExpression terminalNode : node.getParameters()) {
            DeclarationNode declNode = new DeclarationNode(getSourceCodePos(terminalNode),
                    terminalNode.toString().replace(" ", ""),
                    DeclarationNode.Kind.OP_INPUT_PARAMETER,
                    machineNode);
            declarationList.add(declNode);
        }

        resultDefinitionNode = new DefinitionNode(getSourceCodePos(node),
                node.getName().toString().replace(" ", ""),
                declarationList,
                coordinator.convertPredicateNode(node.getRhs(), machineNode));
    }

    @Override
    public void caseASubstitutionDefinitionDefinition(ASubstitutionDefinitionDefinition node) {
        //TODO: Translation Substitution Definition Definition
    }

    private SourceCodePosition getSourceCodePos(Node node){
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
