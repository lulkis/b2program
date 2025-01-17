package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.DefinitionNode;
import de.prob.parser.ast.nodes.MachineNode;
import java.util.ArrayList;
import java.util.List;


/*
Der Visitor zum übersetzen der Definitions. Gibt einen Definition Knoten zurück.
 */
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
            DeclarationNode declNode = new DeclarationNode(getSourceCodePosition(terminalNode),
                    terminalNode.toString().replace(" ", ""),
                    DeclarationNode.Kind.OP_INPUT_PARAMETER,
                    machineNode);
            declarationList.add(declNode);
        }

        resultDefinitionNode = new DefinitionNode(getSourceCodePosition(node),
                node.getName().toString().replace(" ", ""),
                declarationList,
                coordinator.convertExpressionNode(node.getRhs(), machineNode));
    }

    @Override
    public void caseAPredicateDefinitionDefinition(APredicateDefinitionDefinition node){
        List<DeclarationNode> declarationList = new ArrayList<>();
        for (PExpression terminalNode : node.getParameters()) {
            DeclarationNode declNode = new DeclarationNode(getSourceCodePosition(terminalNode),
                    terminalNode.toString().replace(" ", ""),
                    DeclarationNode.Kind.OP_INPUT_PARAMETER,
                    machineNode);
            declarationList.add(declNode);
        }

        resultDefinitionNode = new DefinitionNode(getSourceCodePosition(node),
                node.getName().toString().replace(" ", ""),
                declarationList,
                coordinator.convertPredicateNode(node.getRhs(), machineNode));
    }

    @Override
    public void caseASubstitutionDefinitionDefinition(ASubstitutionDefinitionDefinition node) {
        //TODO: Translation Substitution Definition Definition
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition(node.getStartPos() != null ? node.getStartPos().getLine(): 0, node.getStartPos() != null ? node.getStartPos().getPos() : 0, node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
