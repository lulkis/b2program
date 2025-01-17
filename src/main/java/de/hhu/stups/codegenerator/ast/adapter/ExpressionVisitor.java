package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.hhu.stups.codegenerator.ast.nodes.MachineNodeWithDefinitions;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.expression.*;
import de.prob.parser.ast.nodes.predicate.CastPredicateExpressionNode;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/*
Der Visitor zum übersetzen der Expressions. Gibt einen Expression Knoten zurück.
 */
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
                coordinator.convertPredicateNode(node.getPredicate(), machineNode));
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
                coordinator.convertExpressionNode(node.getExpressions(), machineNode),
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
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.POW);
    }

    @Override
    public void caseAPow1SubsetExpression(APow1SubsetExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.POW1);
    }

    @Override
    public void caseAFinSubsetExpression(AFinSubsetExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.FIN);
    }

    @Override
    public void caseAFin1SubsetExpression(AFin1SubsetExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.FIN1);
    }

    @Override
    public void caseACardExpression(ACardExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.CARD);
    }

    @Override
    public void caseAMultOrCartExpression(AMultOrCartExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.MULT);
    }

    @Override
    public void caseAUnionExpression(AUnionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.UNION);
    }

    @Override
    public void caseAIntersectionExpression(AIntersectionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.INTERSECTION);
    }

    @Override
    public void caseAMinusOrSetSubtractExpression(AMinusOrSetSubtractExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.MINUS);
    }

    @Override
    public void caseAGeneralUnionExpression(AGeneralUnionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.GENERALIZED_UNION);
    }

    @Override
    public void caseAGeneralIntersectionExpression(AGeneralIntersectionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
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
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.MAX);
    }

    @Override
    public void caseAMinExpression(AMinExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.MIN);
    }

    @Override
    public void caseAAddExpression(AAddExpression node){
        List<ExprNode> addList = new ArrayList<>();
        addList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        addList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                addList,
                ExpressionOperatorNode.ExpressionOperator.PLUS);
    }

    @Override
    public void caseADivExpression(ADivExpression node){
        List<ExprNode> addList = new ArrayList<>();
        addList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        addList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                addList,
                ExpressionOperatorNode.ExpressionOperator.DIVIDE);
    }

    @Override
    public void caseAPowerOfExpression(APowerOfExpression node){
        List<ExprNode> addList = new ArrayList<>();
        addList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        addList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                addList,
                ExpressionOperatorNode.ExpressionOperator.POWER_OF);
    }

    @Override
    public void caseAModuloExpression(AModuloExpression node){
        List<ExprNode> addList = new ArrayList<>();
        addList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        addList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                addList,
                ExpressionOperatorNode.ExpressionOperator.MOD);
    }
    //END: Integer


    //START: Functions
    @Override
    public void caseAPartialFunctionExpression(APartialFunctionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        domainList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.PARTIAL_FUNCTION);
    }

    @Override
    public void caseATotalFunctionExpression(ATotalFunctionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        domainList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.TOTAL_FUNCTION);
    }

    @Override
    public void caseAPartialSurjectionExpression(APartialSurjectionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        domainList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.PARTIAL_SURJECTION);
    }

    @Override
    public void caseATotalSurjectionExpression(ATotalSurjectionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        domainList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.TOTAL_SURJECTION);
    }

    @Override
    public void caseAPartialInjectionExpression(APartialInjectionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        domainList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.PARTIAL_INJECTION);
    }

    @Override
    public void caseATotalInjectionExpression(ATotalInjectionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        domainList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.TOTAL_INJECTION);
    }

    @Override
    public void caseAPartialBijectionExpression(APartialBijectionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        domainList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.PARTIAL_BIJECTION);
    }

    @Override
    public void caseATotalBijectionExpression(ATotalBijectionExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        domainList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
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
                coordinator.convertExpressionNode(node.getExpression(), machineNode));
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
        intervalList.add(coordinator.convertExpressionNode(node.getLeftBorder(), machineNode));
        intervalList.add(coordinator.convertExpressionNode(node.getRightBorder(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                intervalList,
                ExpressionOperatorNode.ExpressionOperator.INTERVAL);
    }

    @Override
    public void caseAFunctionExpression(AFunctionExpression node){
        List<ExprNode> exprNodeList = new ArrayList<>();
        ExpressionOperatorNode.ExpressionOperator operator;

        if(node.getIdentifier() instanceof ASuccessorExpression){
            operator = ExpressionOperatorNode.ExpressionOperator.SUCC;
        }
        else if (node.getIdentifier() instanceof APredecessorExpression) {
            operator = ExpressionOperatorNode.ExpressionOperator.PRED;
        }
        else {
            operator = ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL;
            exprNodeList.add(coordinator.convertExpressionNode(node.getIdentifier(), machineNode));
        }

        exprNodeList.addAll(coordinator.convertExpressionNode(node.getParameters(), machineNode));

        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprNodeList,
                operator);
    }

    @Override
    public void caseADomainExpression(ADomainExpression node){
        List<ExprNode> domainList = new ArrayList<>();
        domainList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                domainList,
                ExpressionOperatorNode.ExpressionOperator.DOMAIN);
    }

    @Override
    public void caseACoupleExpression(ACoupleExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                coordinator.convertExpressionNode(node.getList(), machineNode),
                ExpressionOperatorNode.ExpressionOperator.COUPLE);
    }

    @Override
    public void caseAUnaryMinusExpression(AUnaryMinusExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
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
                structList.add(coordinator.convertExpressionNode(((ARecEntry) expr).getValue(), machineNode));
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
                structList.add(coordinator.convertExpressionNode(((ARecEntry) expr).getValue(), machineNode));
            }
        }
        resultExpressionNode = new RecordNode(getSourceCodePosition(node),
                declarationList,
                structList);
    }

    @Override
    public void caseARecordFieldExpression(ARecordFieldExpression node){
        resultExpressionNode = new RecordFieldAccessNode(getSourceCodePosition(node),
                coordinator.convertExpressionNode(node.getRecord(), machineNode),
                new DeclarationNode(getSourceCodePosition(node.getIdentifier()),
                        node.getIdentifier().toString().replace(" ", ""),
                        DeclarationNode.Kind.VARIABLE,
                        machineNode));
    }

    @Override
    public void caseARelationsExpression(ARelationsExpression node){
        List<ExprNode> relationList = new ArrayList<>();
        relationList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        relationList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                relationList,
                ExpressionOperatorNode.ExpressionOperator.SET_RELATION);
    }

    @Override
    public void caseATotalRelationExpression(ATotalRelationExpression node){
        List<ExprNode> relationList = new ArrayList<>();
        relationList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        relationList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                relationList,
                ExpressionOperatorNode.ExpressionOperator.TOTAL_RELATION);
    }

    @Override
    public void caseASurjectionRelationExpression(ASurjectionRelationExpression node){
        List<ExprNode> relationList = new ArrayList<>();
        relationList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        relationList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                relationList,
                ExpressionOperatorNode.ExpressionOperator.SURJECTION_RELATION);
    }

    @Override
    public void caseATotalSurjectionRelationExpression(ATotalSurjectionRelationExpression node){
        List<ExprNode> relationList = new ArrayList<>();
        relationList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        relationList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                relationList,
                ExpressionOperatorNode.ExpressionOperator.TOTAL_SURJECTION_RELATION);
    }

    @Override
    public void caseARangeExpression(ARangeExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.RANGE);
    }

    @Override
    public void caseAOverwriteExpression(AOverwriteExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.OVERWRITE_RELATION);
    }

    @Override
    public void caseAImageExpression(AImageExpression node){
//        List<ExprNode> exprList = new ArrayList<>();
//        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
//        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
//        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
//                exprList,
//                ExpressionOperatorNode.ExpressionOperator.RELATIONAL_IMAGE);

        List<ExprNode> list = new ArrayList<>();
        if(node.getLeft() instanceof AImageExpression){
            AImageExpression test = node;
            while(test.getLeft() instanceof AImageExpression){
                list.add(coordinator.convertExpressionNode(test.getRight(), machineNode));
                test = (AImageExpression) test.getLeft();
            }
            list.add(coordinator.convertExpressionNode(test.getRight(), machineNode));
            list.add(coordinator.convertExpressionNode(test.getLeft(), machineNode));
        }
        else {
            list.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
            list.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        }
        Collections.reverse(list);
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                list,
                ExpressionOperatorNode.ExpressionOperator.RELATIONAL_IMAGE);
    }

    @Override
    public void caseAArityExpression(AArityExpression node){
        //TODO: Translation Arity Expression
    }

    @Override
    public void caseABinExpression(ABinExpression node){
        //TODO: Translation Bin Expression
    }

    @Override
    public void caseABtreeExpression(ABtreeExpression node){
        //TODO: Translation BTree Expression
    }

    @Override
    public void caseACartesianProductExpression(ACartesianProductExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.CARTESIAN_PRODUCT);
    }

    @Override
    public void caseAClosureExpression(AClosureExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.CLOSURE1);
    }

    @Override
    public void caseACompositionExpression(ACompositionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.COMPOSITION);
    }

    @Override
    public void caseAConcatExpression(AConcatExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.CONCAT);
    }

    @Override
    public void caseAConstExpression(AConstExpression node){
        //TODO: Translation Const Expression
    }

    @Override
    public void caseAConvertIntCeilingExpression(AConvertIntCeilingExpression node){
        //TODO: Translation Convert Int Ceiling Expression
    }

    @Override
    public void caseAConvertIntFloorExpression(AConvertIntFloorExpression node){
        //TODO: Translation Convert Int Floor Expression
    }

    @Override
    public void caseAConvertRealExpression(AConvertRealExpression node){
        //TODO: Translation Convert Real Expression
    }

    @Override
    public void caseADefinitionExpression(ADefinitionExpression node){
        if(machineNode instanceof MachineNodeWithDefinitions){
            PDefinition definition = ((MachineNodeWithDefinitions) machineNode).getIDefinition()
                    .getDefinition(node.toString().replace(" ",""));

            List<PExpression> definitionParameterList = new ArrayList<>();
            for(PExpression expression : ((AExpressionDefinitionDefinition) definition).getParameters()){
                definitionParameterList.add(expression.clone());
            }

            DefinitionParameterVisitor definitionParameterVisitor = new DefinitionParameterVisitor(definitionParameterList,
                    node.getParameters());
            definition.apply(definitionParameterVisitor);

            ((AExpressionDefinitionDefinition) definition).setParameters(node.getParameters());
            resultExpressionNode = coordinator.convertExpressionNode(((AExpressionDefinitionDefinition) definition).getRhs(), machineNode);
        }
    }

    @Override
    public void caseADescriptionExpression(ADescriptionExpression node){
        //TODO: Translation Description Expression
    }

    @Override
    public void caseADirectProductExpression(ADirectProductExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.DIRECT_PRODUCT);
    }

    @Override
    public void caseADomainRestrictionExpression(ADomainRestrictionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.DOMAIN_RESTRICTION);
    }

    @Override
    public void caseADomainSubtractionExpression(ADomainSubtractionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.DOMAIN_SUBTRACTION);
    }

    @Override
    public void caseAEmptySequenceExpression(AEmptySequenceExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.SEQ_ENUMERATION);
    }

    @Override
    public void caseAEventBComprehensionSetExpression(AEventBComprehensionSetExpression node){
        //TODO: Translation EventB Set Comprehension Expression
    }

    @Override
    public void caseAEventBFirstProjectionExpression(AEventBFirstProjectionExpression node){
        //TODO: Translation EventBProjection Expression
    }

    @Override
    public void caseAEventBFirstProjectionV2Expression(AEventBFirstProjectionV2Expression node){
        //TODO: Translation EventB First ProjectionV2 Expression
    }

    @Override
    public void caseAEventBIdentityExpression(AEventBIdentityExpression node){
        //TODO: Translation EventBIdentity Expression
    }

    @Override
    public void caseAEventBSecondProjectionExpression(AEventBSecondProjectionExpression node){
        //TODO: Translation EventB Second Projection Expression
    }

    @Override
    public void caseAEventBSecondProjectionV2Expression(AEventBSecondProjectionV2Expression node) {
        //TODO: Translation EventB Second ProjectionV2 Expression
    }

    @Override
    public void caseAExtendedExprExpression(AExtendedExprExpression node) {
        //TODO: Translation Extended Expr Expression
    }

    @Override
    public void caseAFatherExpression(AFatherExpression node) {
        //TODO: Translation Father Expression
    }

    @Override
    public void caseAFirstExpression(AFirstExpression node) {
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.FIRST);
    }

    @Override
    public void caseAFirstProjectionExpression(AFirstProjectionExpression node) {
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExp1(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getExp2(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.PRJ1);
    }

    @Override
    public void caseAFloatSetExpression(AFloatSetExpression node) {
        //TODO: Translation Float Set Expression
    }

    @Override
    public void caseAFlooredDivExpression(AFlooredDivExpression node) {
        //TODO: Translation Floored Div Expression
    }

    @Override
    public void caseAFrontExpression(AFrontExpression node) {
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.FRONT);
    }

    @Override
    public void caseAGeneralConcatExpression(AGeneralConcatExpression node) {
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.CONC);
    }

    @Override
    public void caseAGeneralProductExpression(AGeneralProductExpression node) {
        List<DeclarationNode> declarationList = new ArrayList<>();
        for(PExpression identifier : node.getIdentifiers()){
            declarationList.add(new DeclarationNode(getSourceCodePosition(node),
                    identifier.toString().replace(" ", ""),
                    DeclarationNode.Kind.VARIABLE,
                    machineNode));
        }
        resultExpressionNode = new QuantifiedExpressionNode(getSourceCodePosition(node),
                QuantifiedExpressionNode.QuantifiedExpressionOperator.PI,
                declarationList,
                coordinator.convertPredicateNode(node.getPredicates(), machineNode),
                coordinator.convertExpressionNode(node.getExpression(), machineNode));
    }

    @Override
    public void caseAGeneralSumExpression(AGeneralSumExpression node) {
        List<DeclarationNode> declarationList = new ArrayList<>();
        for(PExpression identifier : node.getIdentifiers()){
            declarationList.add(new DeclarationNode(getSourceCodePosition(node),
                    identifier.toString().replace(" ", ""),
                    DeclarationNode.Kind.VARIABLE,
                    machineNode));
        }
        resultExpressionNode = new QuantifiedExpressionNode(getSourceCodePosition(node),
                QuantifiedExpressionNode.QuantifiedExpressionOperator.SIGMA,
                declarationList,
                coordinator.convertPredicateNode(node.getPredicates(), machineNode),
                coordinator.convertExpressionNode(node.getExpression(), machineNode));
    }

    @Override
    public void caseAHexIntegerExpression(AHexIntegerExpression node) {
        //TODO: Translation Hex Integer Expression
    }

    @Override
    public void caseAIdentityExpression(AIdentityExpression node) {
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.ID);
    }

    @Override
    public void caseAIfElsifExprExpression(AIfElsifExprExpression node) {
        //TODO: Translation If Elsif Expr Expression
    }

    @Override
    public void caseAIfThenElseExpression(AIfThenElseExpression node) {
        resultExpressionNode = new IfExpressionNode(getSourceCodePosition(node),
                coordinator.convertPredicateNode(node.getCondition(), machineNode),
                coordinator.convertExpressionNode(node.getThen(), machineNode),
                coordinator.convertExpressionNode(node.getElse(), machineNode));
    }

    @Override
    public void caseAInfixExpression(AInfixExpression node) {
        //TODO: Translation Infix Expression
    }

    @Override
    public void caseAInsertFrontExpression(AInsertFrontExpression node) {
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.INSERT_FRONT);
    }

    @Override
    public void caseAInsertTailExpression(AInsertTailExpression node) {
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.INSERT_TAIL);
    }

    @Override
    public void caseAIseq1Expression(AIseq1Expression node) {
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.ISEQ1);
    }

    @Override
    public void caseAIseqExpression(AIseqExpression node) {
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.ISEQ);
    }

    @Override
    public void caseALastExpression(ALastExpression node) {
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.LAST);
    }

    @Override
    public void caseALeftExpression(ALeftExpression node) {
        //TODO: Translation Left Expression
    }

    @Override
    public void caseATransRelationExpression(ATransRelationExpression node) {
        List<ExprNode> addList = new ArrayList<>();
        addList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                addList,
                ExpressionOperatorNode.ExpressionOperator.REL);
    }

    @Override
    public void caseALetExpressionExpression(ALetExpressionExpression node) {
        List<DeclarationNode> identifierList = new ArrayList<>();
        for (PExpression expression : node.getIdentifiers()) {
            String name = expression.toString().replace(" ", "");
            DeclarationNode decl = new DeclarationNode(getSourceCodePosition(expression),
                    name,
                    DeclarationNode.Kind.SUBSTITUION_IDENTIFIER,
                    null);
            identifierList.add(decl);
        }
        resultExpressionNode = new LetExpressionNode(getSourceCodePosition(node),
                identifierList,
                coordinator.convertPredicateNode(node.getAssignment(), machineNode),
                coordinator.convertExpressionNode(node.getExpr(), machineNode));
    }

    @Override
    public void caseAIterationExpression(AIterationExpression node) {
        List<ExprNode> addList = new ArrayList<>();
        addList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        addList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                addList,
                ExpressionOperatorNode.ExpressionOperator.ITERATE);
    }

    @Override
    public void caseAMinusExpression(AMinusExpression node) {
        //TODO: Translation Minus Expression
    }

    @Override
    public void caseAMirrorExpression(AMirrorExpression node) {
        //TODO: Translation Mirror Expression
    }

    @Override
    public void caseAMultilineStringExpression(AMultilineStringExpression node) {
        //TODO: Translation Multiline String Expression
    }

    @Override
    public void caseAMultilineTemplateExpression(AMultilineTemplateExpression node) {
        //TODO: Translation Multiline Template Expression
    }

    @Override
    public void caseAMultiplicationExpression(AMultiplicationExpression node) {
        //TODO: Translation Multiplication Expression
    }

    @Override
    public void caseAOperationCallExpression(AOperationCallExpression node){
        //TODO: Translation Operation Call Expression
    }

    @Override
    public void caseAOperatorExpression(AOperatorExpression node){
        //TODO: Translation Operator Expression
    }

    @Override
    public void caseAParallelProductExpression(AParallelProductExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.PARALLEL_PRODUCT);
    }

    @Override
    public void caseAPermExpression(APermExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.PERM);
    }

    @Override
    public void caseAPostfixExpression(APostfixExpression node){
        //TODO: Translation Postfix Expression
    }

    @Override
    public void caseAPrefixExpression(APrefixExpression node){
        //TODO: Translation Prefix Expression
    }

    @Override
    public void caseAPrimedIdentifierExpression(APrimedIdentifierExpression node){
        //TODO: Translation Primed Identifier Expression
    }

    @Override
    public void caseAQuantifiedIntersectionExpression(AQuantifiedIntersectionExpression node){
        List<DeclarationNode> declarationList = new ArrayList<>();
        for(PExpression identifier : node.getIdentifiers()){
            declarationList.add(new DeclarationNode(getSourceCodePosition(node),
                    identifier.toString().replace(" ", ""),
                    DeclarationNode.Kind.VARIABLE,
                    machineNode));
        }
        resultExpressionNode = new QuantifiedExpressionNode(getSourceCodePosition(node),
                QuantifiedExpressionNode.QuantifiedExpressionOperator.QUANTIFIED_INTER,
                declarationList,
                coordinator.convertPredicateNode(node.getPredicates(), machineNode),
                coordinator.convertExpressionNode(node.getExpression(), machineNode));
    }

    @Override
    public void caseAQuantifiedUnionExpression(AQuantifiedUnionExpression node){
        List<DeclarationNode> declarationList = new ArrayList<>();
        for(PExpression identifier : node.getIdentifiers()){
            declarationList.add(new DeclarationNode(getSourceCodePosition(node),
                    identifier.toString().replace(" ", ""),
                    DeclarationNode.Kind.VARIABLE,
                    machineNode));
        }
        resultExpressionNode = new QuantifiedExpressionNode(getSourceCodePosition(node),
                QuantifiedExpressionNode.QuantifiedExpressionOperator.QUANTIFIED_UNION,
                declarationList,
                coordinator.convertPredicateNode(node.getPredicates(), machineNode),
                coordinator.convertExpressionNode(node.getExpression(), machineNode));
    }

    @Override
    public void caseARangeRestrictionExpression(ARangeRestrictionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.RANGE_RESTRICTION);
    }

    @Override
    public void caseARangeSubtractionExpression(ARangeSubtractionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.RANGE_SUBTRACTION);
    }

    @Override
    public void caseARankExpression(ARankExpression node){
        //TODO: Translation Rank Expression
    }

    @Override
    public void caseARealExpression(ARealExpression node){
        //TODO: Translation Real Expression
    }

    @Override
    public void caseARealSetExpression(ARealSetExpression node){
        //TODO: Translation Real Set Expression
    }

    @Override
    public void caseAReflexiveClosureExpression(AReflexiveClosureExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.CLOSURE);
    }

    @Override
    public void caseARestrictFrontExpression(ARestrictFrontExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.RESTRICT_FRONT);
    }

    @Override
    public void caseARestrictTailExpression(ARestrictTailExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.RESTRICT_TAIL);
    }

    @Override
    public void caseAReverseExpression(AReverseExpression node){
        List<ExprNode> exprNodes = new ArrayList<>();
        exprNodes.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprNodes,
                ExpressionOperatorNode.ExpressionOperator.INVERSE_RELATION);
    }

    @Override
    public void caseARevExpression(ARevExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.REV);
    }

    @Override
    public void caseARightExpression(ARightExpression node){
        //TODO: Translation Right Expression
    }

    @Override
    public void caseARingExpression(ARingExpression node){
        //TODO: Translation Ring Expression
    }

    @Override
    public void caseASecondProjectionExpression(ASecondProjectionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExp1(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getExp2(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.PRJ2);
    }

    @Override
    public void caseASeqExpression(ASeqExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.SEQ);
    }

    @Override
    public void caseASeq1Expression(ASeq1Expression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.SEQ1);
    }

    @Override
    public void caseASequenceExtensionExpression(ASequenceExtensionExpression node){
        List<ExprNode> exprList = new ArrayList<>(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.SEQ_ENUMERATION);
    }

    @Override
    public void caseASetSubtractionExpression(ASetSubtractionExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft(), machineNode));
        exprList.add(coordinator.convertExpressionNode(node.getRight(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.SET_SUBTRACTION);
    }

    @Override
    public void caseASizeExpression(ASizeExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.SIZE);
    }

    @Override
    public void caseASizetExpression(ASizetExpression node){
        //TODO: Translation Sizet Expression
    }

    @Override
    public void caseASonExpression(ASonExpression node){
        //TODO: Translation Son Expression
    }

    @Override
    public void caseASonsExpression(ASonsExpression node){
        //TODO: Translation Sons Expression
    }

    @Override
    public void caseAStringExpression(AStringExpression node){
        resultExpressionNode = new StringNode(getSourceCodePosition(node),
                "\"" + node.toString().replace(" ","") + "\"");
    }

    @Override
    public void caseAStringSetExpression(AStringSetExpression node){
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                ExpressionOperatorNode.ExpressionOperator.STRING);
    }

    @Override
    public void caseASubtreeExpression(ASubtreeExpression node){
        //TODO: Translation Subtree Expression
    }

    @Override
    public void caseASymbolicCompositionExpression(ASymbolicCompositionExpression node){
        //TODO: Translation Expression
    }

    @Override
    public void caseASymbolicComprehensionSetExpression(ASymbolicComprehensionSetExpression node){
        //TODO: Translation Expression
    }

    @Override
    public void caseASymbolicLambdaExpression(ASymbolicLambdaExpression node){
        //TODO: Translation Expression
    }

    @Override
    public void caseASymbolicQuantifiedUnionExpression(ASymbolicQuantifiedUnionExpression node){
        //TODO: Translation Expression
    }

    @Override
    public void caseATailExpression(ATailExpression node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.TAIL);
    }

    @Override
    public void caseATopExpression(ATopExpression node){
        //TODO: Translation Top Expression
    }

    @Override
    public void caseATransFunctionExpression(ATransFunctionExpression node){
        //TODO: Translation Trans Function Expression
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getExpression(), machineNode));
        resultExpressionNode = new ExpressionOperatorNode(getSourceCodePosition(node),
                exprList,
                ExpressionOperatorNode.ExpressionOperator.FNC);
    }

    @Override
    public void caseATreeExpression(ATreeExpression node){
        //TODO: Translation Tree Expression
    }

    @Override
    public void caseATypeofExpression(ATypeofExpression node){
        //TODO: Translation Typeof Expression
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition(node.getStartPos() != null ? node.getStartPos().getLine(): 0, node.getStartPos() != null ? node.getStartPos().getPos() : 0, node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
