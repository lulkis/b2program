package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.antlr.Util;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.expression.ExprNode;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.nodes.substitution.*;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class SubstitutionVisitor extends AbstractVisitor{

    private SubstitutionNode resultSubstitutionNode;
    private VisitorCoordinator coordinator = new VisitorCoordinator();
    private MachineNode machineNode;

    public SubstitutionVisitor(MachineNode machineNode){
        this.machineNode = machineNode;
    }

    public SubstitutionNode getResult(){
        return resultSubstitutionNode;
    }

    @Override
    public void caseAAssignSubstitution(AAssignSubstitution node){
        List<ExprNode> left = coordinator.convertExpressionNode(node.getLhsExpression());
        List<ExprNode> right = coordinator.convertExpressionNode(node.getRhsExpressions());
        resultSubstitutionNode = new AssignSubstitutionNode(getSourceCodePosition(node), left, right);
    }

    @Override
    public void caseAPreconditionSubstitution(APreconditionSubstitution node){
        SubstitutionNode substitution = coordinator.convertSubstitutionNode(node.getSubstitution(), machineNode);
        PredicateNode condition = coordinator.convertPredicateNode(node.getPredicate(), machineNode);
        resultSubstitutionNode = new ConditionSubstitutionNode(getSourceCodePosition(node),
                ConditionSubstitutionNode.Kind.PRECONDITION,
                condition,
                substitution);
    }

    @Override
    public void caseASelectSubstitution(ASelectSubstitution node){
        resultSubstitutionNode = new IfOrSelectSubstitutionsNode(getSourceCodePosition(node),
                IfOrSelectSubstitutionsNode.Operator.SELECT,
                List.of(coordinator.convertPredicateNode(node.getCondition(), machineNode)),
                List.of(coordinator.convertSubstitutionNode(node.getThen(), machineNode)),
                coordinator.convertSubstitutionNode(node.getElse(), machineNode));
    }

    @Override
    public void caseAParallelSubstitution(AParallelSubstitution node){
        resultSubstitutionNode = new ListSubstitutionNode(getSourceCodePosition(node),
                ListSubstitutionNode.ListOperator.Parallel,
                coordinator.convertSubstitutionNode(node.getSubstitutions(), machineNode));
    }

    @Override
    public void caseAChoiceOrSubstitution(AChoiceOrSubstitution node){
        List<SubstitutionNode> choiceList = new ArrayList<>();
        choiceList.add(coordinator.convertSubstitutionNode(node.getSubstitution(), machineNode));
        resultSubstitutionNode = new ChoiceSubstitutionNode(getSourceCodePosition(node),
                choiceList);
    }

    @Override
    public void caseASkipSubstitution(ASkipSubstitution node){
        resultSubstitutionNode = new SkipSubstitutionNode(getSourceCodePosition(node));
    }

    @Override
    public void caseAAnySubstitution(AAnySubstitution node){
        PredicateNode predicate = coordinator.convertPredicateNode(node.getWhere(), machineNode);
        SubstitutionNode substitution = coordinator.convertSubstitutionNode(node.getThen(), machineNode);
        List<DeclarationNode> identifierList = new ArrayList<>();

        for (PExpression expression : node.getIdentifiers()) {
            String name = expression.toString().replace(" ", "");
            DeclarationNode decl = new DeclarationNode(getSourceCodePosition(expression),
                    name,
                    DeclarationNode.Kind.SUBSTITUION_IDENTIFIER,
                    null);
            identifierList.add(decl);
        }
        resultSubstitutionNode = new AnySubstitutionNode(getSourceCodePosition(node), identifierList, predicate, substitution);
    }

    @Override
    public void caseAChoiceSubstitution(AChoiceSubstitution node){
        resultSubstitutionNode = new ChoiceSubstitutionNode(getSourceCodePosition(node),
                coordinator.convertSubstitutionNode(node.getSubstitutions(), machineNode));
    }

    @Override
    public void caseAIfSubstitution(AIfSubstitution node){
        List<PredicateNode> conditions = new ArrayList<>();
        conditions.add(coordinator.convertPredicateNode(node.getCondition(), machineNode));
        List<SubstitutionNode> thenSub = new ArrayList<>();
        thenSub.add(coordinator.convertSubstitutionNode(node.getThen(), machineNode));
        resultSubstitutionNode = new IfOrSelectSubstitutionsNode(getSourceCodePosition(node),
                IfOrSelectSubstitutionsNode.Operator.IF,
                conditions,
                thenSub,
                coordinator.convertSubstitutionNode(node.getElse(), machineNode));
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
