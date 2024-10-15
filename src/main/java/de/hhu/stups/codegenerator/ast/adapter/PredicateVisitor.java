package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.expression.ExprNode;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.nodes.predicate.PredicateOperatorNode;
import de.prob.parser.ast.nodes.predicate.PredicateOperatorWithExprArgsNode;
import de.prob.parser.ast.nodes.predicate.QuantifiedPredicateNode;

import java.util.ArrayList;
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

    @Override
    public void caseAImplicationPredicate(AImplicationPredicate node){
        List<PredicateNode> implicationList = new ArrayList<PredicateNode>();
        implicationList.add(coordinator.convertPredicateNode(node.getLeft(), machineNode));
        implicationList.add(coordinator.convertPredicateNode(node.getRight(), machineNode));
        resultPredicateNode = new PredicateOperatorNode(getSourceCodePosition(node),
                PredicateOperatorNode.PredicateOperator.IMPLIES,
                implicationList);
    }

    @Override
    public void caseAForallPredicate(AForallPredicate node){
        List<DeclarationNode> list = new ArrayList<>();
        for(PExpression expression : node.getIdentifiers()){
            list.add(new DeclarationNode(getSourceCodePosition(node),
                    expression.toString().replace(" ", ""),
                    DeclarationNode.Kind.OP_INPUT_PARAMETER,
                    machineNode));
        }
        resultPredicateNode = new QuantifiedPredicateNode(getSourceCodePosition(node),
                list,
                coordinator.convertPredicateNode(node.getImplication(), machineNode),
                QuantifiedPredicateNode.QuantifiedPredicateOperator.UNIVERSAL_QUANTIFICATION);
    }

    @Override
    public void caseAConjunctPredicate(AConjunctPredicate node){

        List<PredicateNode> list = new ArrayList<PredicateNode>();
        list.add(coordinator.convertPredicateNode(node.getLeft(), machineNode));
        list.add(coordinator.convertPredicateNode(node.getRight(), machineNode));
        resultPredicateNode = new PredicateOperatorNode(getSourceCodePosition(node),
                PredicateOperatorNode.PredicateOperator.AND,
                list);
        
        /*
        List<PredicateNode> list = new ArrayList<PredicateNode>();

        //ystem.out.println(node.getLeft().getClass());
        //System.out.println(node.getRight().getClass());
        AConjunctPredicate test = node;
        if(test.getLeft() instanceof AConjunctPredicate){
            test = (AConjunctPredicate) test.getLeft();
            list.add(coordinator.convertPredicateNode(node.getLeft(), machineNode));
            list.add(coordinator.convertPredicateNode(node.getRight(), machineNode));
        }
        if(test.getRight() instanceof AConjunctPredicate){
            test = (AConjunctPredicate) test.getRight();
            list.add(coordinator.convertPredicateNode(node.getLeft(), machineNode));
            list.add(coordinator.convertPredicateNode(node.getRight(), machineNode));
        }

        list.add(coordinator.convertPredicateNode(node.getLeft(), machineNode));
        list.add(coordinator.convertPredicateNode(node.getRight(), machineNode));
        resultPredicateNode = new PredicateOperatorNode(getSourceCodePosition(node),
                PredicateOperatorNode.PredicateOperator.AND,
                list);
         */
    }

    @Override
    public void caseANotEqualPredicate(ANotEqualPredicate node){
        List<ExprNode> list = new ArrayList<>();
        list.add(coordinator.convertExpressionNode(node.getLeft()));
        list.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_EQUAL,
                list);
    }

    @Override
    public void caseAEqualPredicate(AEqualPredicate node){
        List<ExprNode> list = new ArrayList<>();
        list.add(coordinator.convertExpressionNode(node.getLeft()));
        list.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.EQUAL,
                list);
    }

    @Override
    public void caseANegationPredicate(ANegationPredicate node){
        List<PredicateNode> list = new ArrayList<PredicateNode>();
        list.add(coordinator.convertPredicateNode(node.getPredicate(), machineNode));
        resultPredicateNode = new PredicateOperatorNode(getSourceCodePosition(node),
                PredicateOperatorNode.PredicateOperator.NOT,
                list);
    }

    @Override
    public void caseANotMemberPredicate(ANotMemberPredicate node){
        List<ExprNode> list = new ArrayList<>();
        list.add(coordinator.convertExpressionNode(node.getLeft()));
        list.add(coordinator.convertExpressionNode(node.getRight()));
        resultPredicateNode = new PredicateOperatorWithExprArgsNode(getSourceCodePosition(node),
                PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING,
                list);
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
