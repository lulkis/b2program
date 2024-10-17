package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.expression.ExprNode;
import de.prob.parser.ast.nodes.expression.ExpressionOperatorNode;
import de.prob.parser.ast.nodes.expression.IdentifierExprNode;
import de.prob.parser.ast.nodes.expression.NumberNode;
import de.prob.parser.ast.nodes.predicate.CastPredicateExpressionNode;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ExpressionVisitor extends AbstractVisitor{

    private ExprNode resultExpressionNode;
    private VisitorCoordinator coordinator = new VisitorCoordinator();
    private MachineNode machineNode;

    public ExpressionVisitor(MachineNode machineNode){
        this.machineNode = machineNode;
    }

    public ExprNode getResult() {
        return resultExpressionNode;
    }

    //START: Booleans
    @Override
    public void caseABooleanTrueExpression(ABooleanTrueExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.TRUE);
    }

    @Override
    public void caseABooleanFalseExpression(ABooleanFalseExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.FALSE);
    }

    @Override
    public void caseABoolSetExpression(ABoolSetExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.BOOL);
    }

    @Override
    public void caseAConvertBoolExpression(AConvertBoolExpression node){
        resultExpressionNode = new CastPredicateExpressionNode(getSourceCodePosition(node),
                coordinator.convertPredicateNode(node.getPredicate(), null));
    }
    //END: Booleans

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

    @Override
    public void caseAAddExpression(AAddExpression node){
        List<ExprNode> addList = new ArrayList<>();
        addList.add(coordinator.convertExpressionNode(node.getLeft()));
        addList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                addList,
                ExpressionOperatorNode.ExpressionOperator.PLUS);
    }

    @Override
    public void caseAMinusOrSetSubtractExpression(AMinusOrSetSubtractExpression node){
        List<ExprNode> subtractList = new ArrayList<>();
        subtractList.add(coordinator.convertExpressionNode(node.getLeft()));
        subtractList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                subtractList,
                ExpressionOperatorNode.ExpressionOperator.MINUS);
    }

    @Override
    public void caseAUnionExpression(AUnionExpression node){
        List<ExprNode> unionList = new ArrayList<>();
        unionList.add(coordinator.convertExpressionNode(node.getLeft()));
        unionList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                unionList,
                ExpressionOperatorNode.ExpressionOperator.UNION);
    }

    @Override
    public void caseAPowSubsetExpression(APowSubsetExpression node){
        List<ExprNode> powList = new ArrayList<>();
        powList.add(coordinator.convertExpressionNode(node.getExpression()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                powList,
                ExpressionOperatorNode.ExpressionOperator.POW);
    }

    @Override
    public void caseAFunctionExpression(AFunctionExpression node){
        List<ExprNode> exprNodeList = new ArrayList<>();
        exprNodeList.add(coordinator.convertExpressionNode(node.getIdentifier()));
        exprNodeList.addAll(coordinator.convertExpressionNode(node.getParameters()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprNodeList,
                ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
    }

    @Override
    public void caseASetExtensionExpression(ASetExtensionExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                coordinator.convertExpressionNode(node.getExpressions()),
                ExpressionOperatorNode.ExpressionOperator.SET_ENUMERATION);
    }

    @Override
    public void caseADomainExpression(ADomainExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getExpression()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.DOMAIN);
    }

    @Override
    public void caseATotalFunctionExpression(ATotalFunctionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft()));
        domainList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.TOTAL_FUNCTION);
    }

    @Override
    public void caseAEmptySetExpression(AEmptySetExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.SET_ENUMERATION);
    }

    @Override
    public void caseAIntersectionExpression(AIntersectionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft()));
        domainList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.INTERSECTION);
    }

    @Override
    public void caseAPartialInjectionExpression(APartialInjectionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft()));
        domainList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.PARTIAL_INJECTION);
    }

    @Override
    public void caseAPartialFunctionExpression(APartialFunctionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft()));
        domainList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.PARTIAL_FUNCTION);
    }

    @Override
    public void caseACoupleExpression(ACoupleExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                coordinator.convertExpressionNode(node.getList()),
                ExpressionOperatorNode.ExpressionOperator.COUPLE);
    }

    @Override
    public void caseACardExpression(ACardExpression node){
        List<ExprNode> exprNodeList = new ArrayList<>();
        exprNodeList.add(coordinator.convertExpressionNode(node.getExpression()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprNodeList,
                ExpressionOperatorNode.ExpressionOperator.CARD);
    }

    @Override
    public void caseANatSetExpression(ANatSetExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.NAT);
    }

    @Override
    public void caseAUnaryMinusExpression(AUnaryMinusExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.UNARY_MINUS);
    }

    @Override
    public void caseAIntSetExpression(AIntSetExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.INT);
    }

    @Override
    public void caseAPow1SubsetExpression(APow1SubsetExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.POW1);
    }

    @Override
    public void caseAFinSubsetExpression(AFinSubsetExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.FIN);
    }

    @Override
    public void caseAFin1SubsetExpression(AFin1SubsetExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.FIN1);
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
