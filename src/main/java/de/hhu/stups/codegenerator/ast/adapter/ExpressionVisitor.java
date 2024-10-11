package de.hhu.stups.codegenerator.ast.adapter;

import de.prob.parser.ast.nodes.expression.ExprNode;

public class ExpressionVisitor extends AbstractVisitor{

    private ExprNode resultExpressionNode;

    public ExprNode getResult() {
        return resultExpressionNode;
    }
}
