package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.AAssignSubstitution;
import de.be4.classicalb.core.parser.node.APreconditionSubstitution;
import de.be4.classicalb.core.parser.node.Node;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.expression.ExprNode;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.nodes.substitution.AssignSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.ConditionSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.SubstitutionNode;

import java.util.List;

public class SubstitutionVisitor extends AbstractVisitor{

    private SubstitutionNode resultSubstitutionNode;
    private VisitorCoordinator coordinator = new VisitorCoordinator();

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
        SubstitutionNode substitution = coordinator.convertSubstitutionNode(node.getSubstitution());
        PredicateNode condition = coordinator.convertPredicateNode(node.getPredicate());
        resultSubstitutionNode = new ConditionSubstitutionNode(getSourceCodePosition(node),
                ConditionSubstitutionNode.Kind.PRECONDITION,
                condition,
                substitution);
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
