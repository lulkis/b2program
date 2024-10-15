package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.AIntervalExpression;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.node.TIntegerLiteral;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.expression.ExprNode;
import de.prob.parser.ast.nodes.expression.ExpressionOperatorNode;
import de.prob.parser.ast.nodes.expression.IdentifierExprNode;
import de.prob.parser.ast.nodes.expression.NumberNode;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ExpressionVisitor extends AbstractVisitor{

    private ExprNode resultExpressionNode;
    private VisitorCoordinator coordinator = new VisitorCoordinator();

    public ExprNode getResult() {
        return resultExpressionNode;
    }

    @Override
    public void caseTIdentifierLiteral(TIdentifierLiteral node){
        resultExpressionNode = new IdentifierExprNode(getSourceCodePosition(node), node.getText(), false);
    }

    @Override
    public void caseTIntegerLiteral(TIntegerLiteral node){
        resultExpressionNode = new NumberNode(getSourceCodePosition(node), new BigInteger(node.getText()));
    }

    @Override
    public void caseAIntervalExpression(AIntervalExpression node){
        List<ExprNode> intervalList = new ArrayList<>();
        intervalList.add(coordinator.convertExpressionNode(node.getLeftBorder()));
        intervalList.add(coordinator.convertExpressionNode(node.getRightBorder()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                intervalList,
                ExpressionOperatorNode.ExpressionOperator.INTERVAL);
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
