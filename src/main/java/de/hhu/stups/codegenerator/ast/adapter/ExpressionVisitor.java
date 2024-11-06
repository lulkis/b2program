package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.expression.*;
import de.prob.parser.ast.nodes.predicate.CastPredicateExpressionNode;
import de.prob.parser.ast.nodes.predicate.PredicateOperatorWithExprArgsNode;
import de.prob.parser.ast.nodes.substitution.AnySubstitutionNode;

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

    //START: Sets
    @Override
    public void caseAEmptySetExpression(AEmptySetExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.SET_ENUMERATION);
    }

    @Override
    public void caseASetExtensionExpression(ASetExtensionExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                coordinator.convertExpressionNode(node.getExpressions()),
                ExpressionOperatorNode.ExpressionOperator.SET_ENUMERATION);
    }

    @Override
    public void caseAComprehensionSetExpression(AComprehensionSetExpression node){
        List<DeclarationNode> declarationList = new ArrayList<>();
        for (PExpression terminalNode : node.getIdentifiers()) {
            DeclarationNode declNode = new DeclarationNode(getSourceCodePosition(terminalNode),
                    terminalNode.toString().replace(" ", ""),
                    DeclarationNode.Kind.VARIABLE,
                    machineNode);
            declarationList.add(declNode);
        }

        resultExpressionNode = new SetComprehensionNode(getSourceCodePosition(node),
                declarationList,
                coordinator.convertPredicateNode(node.getPredicates(), machineNode));
    }

    @Override
    public void caseAPowSubsetExpression(APowSubsetExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.POW);
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

    @Override
    public void caseACardExpression(ACardExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.CARD);
    }

    @Override
    public void caseAMultOrCartExpression(AMultOrCartExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft()));
        exprList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.MULT);
    }

    @Override
    public void caseAUnionExpression(AUnionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft()));
        exprList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.UNION);
    }

    @Override
    public void caseAIntersectionExpression(AIntersectionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft()));
        exprList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.INTERSECTION);
    }

    @Override
    public void caseAMinusOrSetSubtractExpression(AMinusOrSetSubtractExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft()));
        exprList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.MINUS);
    }

    @Override
    public void caseAGeneralUnionExpression(AGeneralUnionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.GENERALIZED_UNION);
    }

    @Override
    public void caseAGeneralIntersectionExpression(AGeneralIntersectionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.GENERALIZED_INTER);
    }
    //END: Sets


    //START: Integer
    @Override
    public void caseAIntegerSetExpression(AIntegerSetExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.INTEGER);
    }

    @Override
    public void caseANaturalSetExpression(ANaturalSetExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.NATURAL);
    }

    @Override
    public void caseANatural1SetExpression(ANatural1SetExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.NATURAL1);
    }

    @Override
    public void caseAIntSetExpression(AIntSetExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.INT);
    }

    @Override
    public void caseANatSetExpression(ANatSetExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.NAT);
    }

    @Override
    public void caseANat1SetExpression(ANat1SetExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.NAT1);
    }

    @Override
    public void caseAMaxIntExpression(AMaxIntExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.MAXINT);
    }

    @Override
    public void caseAMinIntExpression(AMinIntExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.MININT);
    }

    @Override
    public void caseAMaxExpression(AMaxExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.MAX);
    }

    @Override
    public void caseAMinExpression(AMinExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.MIN);
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
    public void caseADivExpression(ADivExpression node){
        List<ExprNode> addList = new ArrayList<>();
        addList.add(coordinator.convertExpressionNode(node.getLeft()));
        addList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                addList,
                ExpressionOperatorNode.ExpressionOperator.DIVIDE);
    }

    @Override
    public void caseAPowerOfExpression(APowerOfExpression node){
        List<ExprNode> addList = new ArrayList<>();
        addList.add(coordinator.convertExpressionNode(node.getLeft()));
        addList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                addList,
                ExpressionOperatorNode.ExpressionOperator.POWER_OF);
    }

    @Override
    public void caseAModuloExpression(AModuloExpression node){
        List<ExprNode> addList = new ArrayList<>();
        addList.add(coordinator.convertExpressionNode(node.getLeft()));
        addList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                addList,
                ExpressionOperatorNode.ExpressionOperator.MOD);
    }
    //END: Integer


    //START: Functions
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
    public void caseATotalFunctionExpression(ATotalFunctionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft()));
        domainList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.TOTAL_FUNCTION);
    }

    @Override
    public void caseAPartialSurjectionExpression(APartialSurjectionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft()));
        domainList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.PARTIAL_SURJECTION);
    }

    @Override
    public void caseATotalSurjectionExpression(ATotalSurjectionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft()));
        domainList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.TOTAL_SURJECTION);
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
    public void caseATotalInjectionExpression(ATotalInjectionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft()));
        domainList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.TOTAL_INJECTION);
    }

    @Override
    public void caseAPartialBijectionExpression(APartialBijectionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft()));
        domainList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.PARTIAL_BIJECTION);
    }

    @Override
    public void caseATotalBijectionExpression(ATotalBijectionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft()));
        domainList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.TOTAL_BIJECTION);
    }

    @Override
    public void caseALambdaExpression(ALambdaExpression node){
        List<DeclarationNode> identifierList = new ArrayList<>();
        for (PExpression expression : node.getIdentifiers()) {
            String name = expression.toString().replace(" ", "");
            DeclarationNode decl = new DeclarationNode(getSourceCodePosition(expression),
                    name,
                    DeclarationNode.Kind.VARIABLE,
                    null);
            identifierList.add(decl);
        }

        resultExpressionNode = new LambdaNode(getSourceCodePosition(node),
                identifierList,
                coordinator.convertPredicateNode(node.getPredicate(), machineNode),
                coordinator.convertExpressionNode(node.getExpression()));
    }
    //END: Functions

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
    public void caseAFunctionExpression(AFunctionExpression node){
        List<ExprNode> exprNodeList = new ArrayList<>();
        ExpressionOperatorNode.ExpressionOperator operator;
        exprNodeList.add(coordinator.convertExpressionNode(node.getIdentifier()));
        exprNodeList.addAll(coordinator.convertExpressionNode(node.getParameters()));

        if(node.getIdentifier() instanceof ASuccessorExpression){
            operator = ExpressionOperatorNode.ExpressionOperator.SUCC;
        }
        else if (node.getIdentifier() instanceof APredecessorExpression) {
            operator = ExpressionOperatorNode.ExpressionOperator.PRED;
        }
        else {
            operator = ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL;
        }

        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprNodeList,
                operator);
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
    public void caseACoupleExpression(ACoupleExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                coordinator.convertExpressionNode(node.getList()),
                ExpressionOperatorNode.ExpressionOperator.COUPLE);
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
    public void caseAStructExpression(AStructExpression node){
        List<DeclarationNode> declarationList = new ArrayList<>();
        List<ExprNode> structList = new ArrayList<>();

        for(PRecEntry expr : node.getEntries()){
            if(expr instanceof ARecEntry){
                declarationList.add(new DeclarationNode(getSourceCodePosition(expr),
                        ((ARecEntry) expr).getIdentifier().toString().replace(" ", ""),
                        DeclarationNode.Kind.VARIABLE,
                        machineNode));
                structList.add(coordinator.convertExpressionNode(((ARecEntry) expr).getValue()));
            }
        }
        resultExpressionNode = new StructNode(getSourceCodePosition(node),
                declarationList,
                structList);
    }

    @Override
    public void caseARecExpression(ARecExpression node){
        List<DeclarationNode> declarationList = new ArrayList<>();
        List<ExprNode> structList = new ArrayList<>();

        for(PRecEntry expr : node.getEntries()){
            if(expr instanceof ARecEntry){
                declarationList.add(new DeclarationNode(getSourceCodePosition(expr),
                        ((ARecEntry) expr).getIdentifier().toString().replace(" ", ""),
                        DeclarationNode.Kind.VARIABLE,
                        machineNode));
                structList.add(coordinator.convertExpressionNode(((ARecEntry) expr).getValue()));
            }
        }
        resultExpressionNode = new StructNode(getSourceCodePosition(node),
                declarationList,
                structList);
    }

    @Override
    public void caseARecordFieldExpression(ARecordFieldExpression node){
        resultExpressionNode = new RecordFieldAccessNode(getSourceCodePosition(node),
                coordinator.convertExpressionNode(node.getRecord()),
                new DeclarationNode(getSourceCodePosition(node.getIdentifier()),
                        node.getIdentifier().toString().replace(" ", ""),
                        DeclarationNode.Kind.VARIABLE,
                        machineNode));
    }

    @Override
    public void caseARelationsExpression(ARelationsExpression node){
        List<ExprNode> relationList = new ArrayList<>();
        relationList.add(coordinator.convertExpressionNode(node.getLeft()));
        relationList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                relationList,
                ExpressionOperatorNode.ExpressionOperator.SET_RELATION);
    }

    @Override
    public void caseATotalRelationExpression(ATotalRelationExpression node){
        List<ExprNode> relationList = new ArrayList<>();
        relationList.add(coordinator.convertExpressionNode(node.getLeft()));
        relationList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                relationList,
                ExpressionOperatorNode.ExpressionOperator.TOTAL_RELATION);
    }

    @Override
    public void caseASurjectionRelationExpression(ASurjectionRelationExpression node){
        List<ExprNode> relationList = new ArrayList<>();
        relationList.add(coordinator.convertExpressionNode(node.getLeft()));
        relationList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                relationList,
                ExpressionOperatorNode.ExpressionOperator.SURJECTION_RELATION);
    }

    @Override
    public void caseATotalSurjectionRelationExpression(ATotalSurjectionRelationExpression node){
        List<ExprNode> relationList = new ArrayList<>();
        relationList.add(coordinator.convertExpressionNode(node.getLeft()));
        relationList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                relationList,
                ExpressionOperatorNode.ExpressionOperator.TOTAL_SURJECTION_RELATION);
    }

    @Override
    public void caseARangeExpression(ARangeExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.RANGE);
    }

    @Override
    public void caseAOverwriteExpression(AOverwriteExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft()));
        exprList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.OVERWRITE_RELATION);
    }

    @Override
    public void caseAImageExpression(AImageExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft()));
        exprList.add(coordinator.convertExpressionNode(node.getRight()));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.RELATIONAL_IMAGE);
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
