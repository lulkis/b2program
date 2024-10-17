package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.node.*;
import de.be4.classicalb.core.parser.node.Node;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.hhu.stups.codegenerator.ast.nodes.MachineNodeWithDefinitions;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.*;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.nodes.substitution.SubstitutionNode;

import java.util.ArrayList;
import java.util.List;

public class MachineVisitor extends AbstractVisitor{

    private MachineNodeWithDefinitions resultMachineNode;
    private VisitorCoordinator coordinator = new VisitorCoordinator();

    private String name;
    private List<DeclarationNode> variables = new ArrayList<>();
    private PredicateNode invariant;
    private SubstitutionNode initialisation;
    private List<OperationNode> operations;
    private List<de.prob.parser.ast.nodes.Node> setEnumerations = new ArrayList<>();
    private List<DefinitionNode> definitions = new ArrayList<>();

    private IDefinitions iDefinitions;

    public MachineNode getResult(){
        return resultMachineNode;
    }

    public MachineVisitor(IDefinitions iDefinitions){
        this.iDefinitions = iDefinitions;
    }

    @Override
    public void caseAMachineHeader(AMachineHeader node){
        this.resultMachineNode = new MachineNodeWithDefinitions(getSourceCodePosition(node));
        resultMachineNode.setIDefinitions(iDefinitions);
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
        initialisation = coordinator.convertSubstitutionNode(node.getSubstitutions(), resultMachineNode);
    }

    @Override
    public void caseAInvariantMachineClause(AInvariantMachineClause node){
        invariant = coordinator.convertPredicateNode(node.getPredicates(), resultMachineNode);
    }

    @Override
    public void caseAOperationsMachineClause(AOperationsMachineClause node){
        operations = coordinator.convertOperationNode(node.getOperations(), resultMachineNode);
    }

    @Override
    public void caseASetsMachineClause(ASetsMachineClause node){
        setEnumerations = coordinator.convertSetNode(node.getSetDefinitions(), resultMachineNode);
    }

    @Override
    public void caseADefinitionsMachineClause(ADefinitionsMachineClause node){
        definitions = coordinator.convertDefinitionNode(node.getDefinitions(), resultMachineNode);
    }

    @Override
    public void caseEOF(EOF node){
        resultMachineNode.setName(name);
        if(initialisation!=null){
            resultMachineNode.setInitialisation(initialisation);
        }
        if(invariant != null){
            resultMachineNode.setInvariant(invariant);
        }
        resultMachineNode.setOperations(operations);
        for(de.prob.parser.ast.nodes.Node set : setEnumerations){
            if(set instanceof EnumeratedSetDeclarationNode){
                resultMachineNode.addSetEnumeration((EnumeratedSetDeclarationNode) set);
            } else {
                resultMachineNode.addDeferredSet((DeclarationNode) set);
            }
        }
        for (DefinitionNode definition : definitions){
            resultMachineNode.addDefinition(definition);
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
