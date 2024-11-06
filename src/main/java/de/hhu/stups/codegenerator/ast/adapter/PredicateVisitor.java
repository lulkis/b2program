package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.hhu.stups.codegenerator.ast.nodes.MachineNodeWithDefinitions;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.expression.ExprNode;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.nodes.predicate.PredicateOperatorNode;
import de.prob.parser.ast.nodes.predicate.PredicateOperatorWithExprArgsNode;
import de.prob.parser.ast.nodes.predicate.QuantifiedPredicateNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PredicateVisitor  extends AbstractVisitor{

    private PredicateNode resultPredicateNode;
    private VisitorCoordinator coordinator = new VisitorCoordinator();
    private MachineNode machineNode;

    public PredicateVisitor(MachineNode machineNode){
        this.machineNode = machineNode;
    }

    public PredicateNode getResult(){
        return resultPredicateNode;
    }

    //START: Logical Predicates
    @Override
    public void caseAConjunctPredicate(AConjunctPredicate node){
        /*
        List<PredicateNode> list = new ArrayList<PredicateNode>();

        System.out.println("Right: " + node.getRight().getClass());
        System.out.println("Left: " + node.getLeft().getClass());

        list.add(coordinator.convertPredicateNode(node.getLeft(), machineNode));
        list.add(coordinator.convertPredicateNode(node.getRight(), machineNode));
        resultPredicateNode = new PredicateOperatorNode(getSourceCodePosition(node),
                PredicateOperatorNode.PredicateOperator.AND,
                list);
        List<PredicateNode> list = new ArrayList<PredicateNode>();
         */

        List<PredicateNode> predicateList = new ArrayList<>();
        //list.add(coordinator.convertPredicateNode(node.getLeft(), machineNode));
        if(node.getLeft() instanceof AConjunctPredicate){
            AConjunctPredicate test = node;
            while(test.getLeft() instanceof AConjunctPredicate){
                predicateList.add(coordinator.convertPredicateNode(test.getRight(), machineNode));
                test = (AConjunctPredicate) test.getLeft();
            }
            predicateList.add(coordinator.convertPredicateNode(test.getRight(), machineNode));
            predicateList.add(coordinator.convertPredicateNode(test.getLeft(), machineNode));
        }
        else {
            predicateList.add(coordinator.convertPredicateNode(node.getRight(), machineNode));
            predicateList.add(coordinator.convertPredicateNode(node.getLeft(), machineNode));
        }
        Collections.reverse(predicateList);
        resultPredicateNode = new PredicateOperatorNode(getSourceCodePosition(node),
                PredicateOperatorNode.PredicateOperator.AND,
                predicateList);
    }

    @Override
    public void caseADisjunctPredicate(ADisjunctPredicate node){
//        List<PredicateNode> predicateList = new ArrayList<>();
//        predicateList.add(coordinator.convertPredicateNode(node.getLeft(), machineNode));
//        predicateList.add(coordinator.convertPredicateNode(node.getRight(), machineNode));
//        resultPredicateNode = new PredicateOperatorNode(getSourceCodePosition(node),
//                PredicateOperatorNode.PredicateOperator.OR,
//                predicateList);

        List<PredicateNode> predicateList = new ArrayList<>();
        //list.add(coordinator.convertPredicateNode(node.getLeft(), machineNode));
        if(node.getLeft() instanceof ADisjunctPredicate){
            ADisjunctPredicate test = node;
            while(test.getLeft() instanceof ADisjunctPredicate){
                predicateList.add(coordinator.convertPredicateNode(test.getRight(), machineNode));
                test = (ADisjunctPredicate) test.getLeft();
            }
            predicateList.add(coordinator.convertPredicateNode(test.getRight(), machineNode));
            predicateList.add(coordinator.convertPredicateNode(test.getLeft(), machineNode));
        }
        else {
            predicateList.add(coordinator.convertPredicateNode(node.getRight(), machineNode));
            predicateList.add(coordinator.convertPredicateNode(node.getLeft(), machineNode));
        }
        Collections.reverse(predicateList);
        resultPredicateNode = new PredicateOperatorNode(getSourceCodePosition(node),
                PredicateOperatorNode.PredicateOperator.OR,
                predicateList);
    }

    @Override
    public void caseAImplicationPredicate(AImplicationPredicate node){
        List<PredicateNode> predicateList = new ArrayList<>();
        predicateList.add(coordinator.convertPredicateNode(node.getLeft(), machineNode));
        predicateList.add(coordinator.convertPredicateNode(node.getRight(), machineNode));
        resultPredicateNode = new PredicateOperatorNode(getSourceCodePosition(node),
                PredicateOperatorNode.PredicateOperator.IMPLIES,
                predicateList);
    }

    @Override
    public void caseAEquivalencePredicate(AEquivalencePredicate node) {
        List<PredicateNode> predicateList = new ArrayList<>();
        predicateList.add(coordinator.convertPredicateNode(node.getLeft(), machineNode));
        predicateList.add(coordinator.convertPredicateNode(node.getRight(), machineNode));
        resultPredicateNode = new PredicateOperatorNode(getSourceCodePosition(node),
                PredicateOperatorNode.PredicateOperator.EQUIVALENCE,
                predicateList);
    }

    @Override
    public void caseANegationPredicate(ANegationPredicate node){
        List<PredicateNode> predicateList = new ArrayList<>();
        predicateList.add(coordinator.convertPredicateNode(node.getPredicate(), machineNode));
        resultPredicateNode = new PredicateOperatorNode(getSourceCodePosition(node),
                PredicateOperatorNode.PredicateOperator.NOT,
                predicateList);
    }

    @Override
    public void caseAForallPredicate(AForallPredicate node){
        List<DeclarationNode> declarationList = new ArrayList<>();
        for(PExpression expression : node.getIdentifiers()){
            declarationList.add(new DeclarationNode(getSourceCodePosition(node),
                    expression.toString().replace(" ", ""),
                    DeclarationNode.Kind.OP_INPUT_PARAMETER,
                    machineNode));
        }
        resultPredicateNode = new QuantifiedPredicateNode(getSourceCodePosition(node),
                declarationList,
                coordinator.convertPredicateNode(node.getImplication(), machineNode),
                QuantifiedPredicateNode.QuantifiedPredicateOperator.UNIVERSAL_QUANTIFICATION);
    }

    @Override
    public void caseAExistsPredicate (AExistsPredicate node){
        List<DeclarationNode> declarationList = new ArrayList<>();
        for(PExpression expression : node.getIdentifiers()){
            declarationList.add(new DeclarationNode(getSourceCodePosition(node),
                    expression.toString().replace(" ", ""),
                    DeclarationNode.Kind.OP_INPUT_PARAMETER,
                    machineNode));
        }
        resultPredicateNode = new QuantifiedPredicateNode(getSourceCodePosition(node),
                declarationList,
                coordinator.convertPredicateNode(node.getPredicate(), machineNode),
                QuantifiedPredicateNode.QuantifiedPredicateOperator.EXISTENTIAL_QUANTIFICATION);
    }

    @Override
    public void caseATruthPredicate(ATruthPredicate node){
        resultPredicateNode = new PredicateOperatorNode(getSourceCodePosition(node),
                PredicateOperatorNode.PredicateOperator.TRUE,
                null);
    }

    @Override
    public void caseAFalsityPredicate(AFalsityPredicate node){
        resultPredicateNode = new PredicateOperatorNode(getSourceCodePosition(node),
                PredicateOperatorNode.PredicateOperator.FALSE,
                null);
    }
    //END: Logical Predicates

    //START: Equality
    @Override
    public void caseAEqualPredicate(AEqualPredicate node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft()));
        exprList.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.EQUAL,
                exprList);
    }

    @Override
    public void caseANotEqualPredicate(ANotEqualPredicate node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft()));
        exprList.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_EQUAL,
                exprList);
    }
    //END: Equality

    //START: Sets
    @Override
    public void caseAMemberPredicate(AMemberPredicate node) {
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft()));
        exprList.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(
                getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF,
                exprList);
    }

    @Override
    public void caseANotMemberPredicate(ANotMemberPredicate node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft()));
        exprList.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING,
                exprList);
    }

    @Override
    public void caseASubsetPredicate(ASubsetPredicate node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft()));
        exprList.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.INCLUSION,
                exprList);
    }

    @Override
    public void caseANotSubsetPredicate(ANotSubsetPredicate node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft()));
        exprList.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NON_INCLUSION,
                exprList);
    }

    @Override
    public void caseASubsetStrictPredicate(ASubsetStrictPredicate node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft()));
        exprList.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.STRICT_INCLUSION,
                exprList);
    }

    @Override
    public void caseANotSubsetStrictPredicate(ANotSubsetStrictPredicate node){
        List<ExprNode> exprList = new ArrayList<>();
        exprList.add(coordinator.convertExpressionNode(node.getLeft()));
        exprList.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.STRICT_NON_INCLUSION,
                exprList);
    }
    //END: Sets

    //START Integers
    @Override
    public void caseAGreaterPredicate(AGreaterPredicate node) {
        List<ExprNode> greaterList = new ArrayList<>();
        greaterList.add(coordinator.convertExpressionNode(node.getLeft()));
        greaterList.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.GREATER,
                greaterList);
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
    public void caseAGreaterEqualPredicate(AGreaterEqualPredicate node) {
        List<ExprNode> lessList = new ArrayList<>();
        lessList.add(coordinator.convertExpressionNode(node.getLeft()));
        lessList.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.GREATER_EQUAL,
                lessList);
    }

    @Override
    public void caseALessEqualPredicate(ALessEqualPredicate node) {
        List<ExprNode> lessList = new ArrayList<>();
        lessList.add(coordinator.convertExpressionNode(node.getLeft()));
        lessList.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.LESS_EQUAL,
                lessList);
    }
    //END: Integers

    @Override
    public void caseADescriptionPredicate(ADescriptionPredicate node){
        //TODO: Translation Description Predicate
    }

    @Override
    public void caseAExtendedPredPredicate(AExtendedPredPredicate node){
        //TODO: Translation Extended Pred Predicate
    }

    @Override
    public void caseAFinitePredicate(AFinitePredicate node){
        //TODO: Translation Finite Predicate
    }

    @Override
    public void caseAIfElsifPredicatePredicate(AIfElsifPredicatePredicate node){
        //TODO: Translation If Elsif Predicate Predicate
    }

    @Override
    public void caseAIfPredicatePredicate(AIfPredicatePredicate node){
        //TODO: Translation If Predicate Predicate
    }

    @Override
    public void caseALabelPredicate(ALabelPredicate node){
        //TODO: Translation Label Predicate
    }

    @Override
    public void caseALetPredicatePredicate(ALetPredicatePredicate node){
        //TODO: Translation Let Predicate Predicate
    }

    @Override
    public void caseAOperatorPredicate(AOperatorPredicate node){
        //TODO: Translation Operator Predicate
    }

    @Override
    public void caseAPartitionPredicate(APartitionPredicate node){
        //TODO: Translation Partition Predicate
    }

    @Override
    public void caseASubstitutionPredicate(ASubstitutionPredicate node){
        //TODO: Translation Substitution Predicate
    }

    //Tests for Predicate Definitions
    @Override
    public void caseADefinitionPredicate(ADefinitionPredicate node){
        if(machineNode instanceof MachineNodeWithDefinitions){
            PDefinition definition = ((MachineNodeWithDefinitions) machineNode).getIDefinition().getDefinition(node.getDefLiteral().toString().replaceAll(" ", ""));

            List<PExpression> definitionParameterList = new ArrayList<>();
            for(PExpression expression : ((APredicateDefinitionDefinition) definition).getParameters()){
                definitionParameterList.add(expression.clone());
            }

            DefinitionParameterVisitor definitionParameterVisitor = new DefinitionParameterVisitor(definitionParameterList,
                    node.getParameters());
            definition.apply(definitionParameterVisitor);

            ((APredicateDefinitionDefinition) definition).setParameters(node.getParameters());
            resultPredicateNode = coordinator.convertPredicateNode(((APredicateDefinitionDefinition) definition).getRhs(), machineNode);
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
