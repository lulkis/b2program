package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.EnumeratedSetDeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.OperationNode;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.nodes.substitution.SubstitutionNode;

import java.util.ArrayList;
import java.util.List;

public class MachineVisitor extends AbstractVisitor{

    private MachineNode resultMachineNode;
    private VisitorCoordinator coordinator = new VisitorCoordinator();

    private String name;
    private List<DeclarationNode> variables = new ArrayList<>();
    private PredicateNode invariant;
    private SubstitutionNode initialisation;
    private List<OperationNode> operations;
    private List<EnumeratedSetDeclarationNode> setEnumerations = new ArrayList<>();

    public MachineNode getResult(){
        return resultMachineNode;
    }

    @Override
    public void caseAMachineHeader(AMachineHeader node){
        this.resultMachineNode = new MachineNode(getSourceCodePosition(node));
        name = node.getName().get(0).toString().replace(" ", "");
    }

    @Override
    public void caseAConstantsMachineClause(AConstantsMachineClause node){
        List<DeclarationNode> constantList = new ArrayList<>();
        for(PExpression expression : node.getIdentifiers()){
            constantList.add(new DeclarationNode(getSourceCodePosition(node),
                    expression.toString().replace(" ", ""),
                    DeclarationNode.Kind.CONSTANT,
                    resultMachineNode));
        }
        resultMachineNode.addConstants(constantList);
    }

    @Override
    public void caseAVariablesMachineClause(AVariablesMachineClause node){
        List<DeclarationNode> constantList = new ArrayList<>();
        for(PExpression expression : node.getIdentifiers()){
            constantList.add(new DeclarationNode(getSourceCodePosition(node),
                    expression.toString().replace(" ", ""),
                    DeclarationNode.Kind.VARIABLE,
                    resultMachineNode));
        }
        resultMachineNode.addVariables(constantList);
    }

    @Override
    public void caseAInitialisationMachineClause(AInitialisationMachineClause node){
        initialisation = coordinator.convertSubstitutionNode(node.getSubstitutions());
    }

    @Override
    public void caseAInvariantMachineClause(AInvariantMachineClause node){
        invariant = coordinator.convertPredicateNode(node.getPredicates());
    }

    @Override
    public void caseEOF(EOF node){
        resultMachineNode.setName(name);
        resultMachineNode.setInitialisation(initialisation);
        resultMachineNode.setInvariant(invariant);
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
