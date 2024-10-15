package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.AGreaterPredicate;
import de.be4.classicalb.core.parser.node.ALessPredicate;
import de.be4.classicalb.core.parser.node.AMemberPredicate;
import de.be4.classicalb.core.parser.node.Node;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.expression.ExprNode;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.nodes.predicate.PredicateOperatorWithExprArgsNode;

import java.util.ArrayList;
import java.util.List;

public class PredicateVisitor  extends AbstractVisitor{

    private PredicateNode resultPredicateNode;
    private VisitorCoordinator coordinator = new VisitorCoordinator();

    public PredicateNode getResult(){
        return resultPredicateNode;
    }

    @Override
    public void caseAMemberPredicate(AMemberPredicate node) {
        List<ExprNode> list = new ArrayList<>();
        list.add(coordinator.convertExpressionNode(node.getLeft()));
        list.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(
                getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF,
                list);
    }

    @Override
    public void caseALessPredicate(ALessPredicate node) {
        List<ExprNode> lessList = new ArrayList<>();
        lessList.add(coordinator.convertExpressionNode(node.getLeft()));
        lessList.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.LESS,
                lessList);
    }

    @Override
    public void caseAGreaterPredicate(AGreaterPredicate node) {
        List<ExprNode> greaterList = new ArrayList<>();
        greaterList.add(coordinator.convertExpressionNode(node.getLeft()));
        greaterList.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.GREATER,
                greaterList);
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
