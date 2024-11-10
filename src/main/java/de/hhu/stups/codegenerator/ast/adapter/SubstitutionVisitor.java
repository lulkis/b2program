package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.codegenerator.ast.VisitorCoordinator;
import de.prob.parser.antlr.Util;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.expression.ExprNode;
import de.prob.parser.ast.nodes.expression.IdentifierExprNode;
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
        List<ExprNode> left = coordinator.convertExpressionNode(node.getLhsExpression(), machineNode);
        List<ExprNode> right = coordinator.convertExpressionNode(node.getRhsExpressions(), machineNode);
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
        List<PredicateNode> predcateList = new ArrayList<>();
        predcateList.add(coordinator.convertPredicateNode(node.getCondition(), machineNode));
        List<SubstitutionNode> substitutionList = new ArrayList<>();
        substitutionList.add(coordinator.convertSubstitutionNode(node.getThen(), machineNode));
        resultSubstitutionNode = new IfOrSelectSubstitutionsNode(getSourceCodePosition(node),
                IfOrSelectSubstitutionsNode.Operator.SELECT,
                predcateList,
                substitutionList,
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

        for(PSubstitution elsif : node.getElsifSubstitutions()){
            if(elsif instanceof AIfElsifSubstitution){
                conditions.add(coordinator.convertPredicateNode(((AIfElsifSubstitution) elsif).getCondition(), machineNode));
                thenSub.add(coordinator.convertSubstitutionNode(((AIfElsifSubstitution) elsif).getThenSubstitution(), machineNode));
            }
        }

        resultSubstitutionNode = new IfOrSelectSubstitutionsNode(getSourceCodePosition(node),
                IfOrSelectSubstitutionsNode.Operator.IF,
                conditions,
                thenSub,
                coordinator.convertSubstitutionNode(node.getElse(), machineNode));
    }

    @Override
    public void caseALetSubstitution(ALetSubstitution node){
        List<DeclarationNode> identifierList = new ArrayList<>();
        for (PExpression expression : node.getIdentifiers()) {
            String name = expression.toString().replace(" ", "");
            DeclarationNode decl = new DeclarationNode(getSourceCodePosition(expression),
                    name,
                    DeclarationNode.Kind.SUBSTITUION_IDENTIFIER,
                    null);
            identifierList.add(decl);
        }

        resultSubstitutionNode = new LetSubstitutionNode(getSourceCodePosition(node),
                identifierList,
                coordinator.convertPredicateNode(node.getPredicate(), machineNode),
                coordinator.convertSubstitutionNode(node.getSubstitution(), machineNode));
    }

    @Override
    public void caseAAssertionSubstitution(AAssertionSubstitution node){
        SubstitutionNode substitution = coordinator.convertSubstitutionNode(node.getSubstitution(), machineNode);
        PredicateNode condition = coordinator.convertPredicateNode(node.getPredicate(), machineNode);
        resultSubstitutionNode = new ConditionSubstitutionNode(getSourceCodePosition(node),
                ConditionSubstitutionNode.Kind.ASSERT,
                condition,
                substitution);
    }

    @Override
    public void caseABecomesElementOfSubstitution(ABecomesElementOfSubstitution node){
        List<IdentifierExprNode> exprList = new ArrayList<>();
        for (PExpression expression : node.getIdentifiers()){
            exprList.add(new IdentifierExprNode(getSourceCodePosition(node),
                    expression.toString().replace(" ", ""),
                    false));
        }
        resultSubstitutionNode = new BecomesElementOfSubstitutionNode(getSourceCodePosition(node),
                exprList,
                coordinator.convertExpressionNode(node.getSet(), machineNode));
    }

    @Override
    public void caseABecomesSuchSubstitution(ABecomesSuchSubstitution node){
        List<IdentifierExprNode> exprList = new ArrayList<>();
        for (PExpression expression : node.getIdentifiers()){
            exprList.add(new IdentifierExprNode(getSourceCodePosition(node),
                    expression.toString().replace(" ", ""),
                    false));
        }
        resultSubstitutionNode = new BecomesSuchThatSubstitutionNode(getSourceCodePosition(node),
                exprList,
                coordinator.convertPredicateNode(node.getPredicate(), machineNode));
    }

    @Override
    public void caseABlockSubstitution(ABlockSubstitution node){
        node.getSubstitution().apply(this);
        //TODO: Translation Block Substitution
    }

    @Override
    public void caseACaseOrSubstitution(ACaseOrSubstitution node){
        //TODO: Translation Case Or Substitution
    }

    @Override
    public void caseACaseSubstitution(ACaseSubstitution node){
        //TODO: Translation Case Substitution
    }

    @Override
    public void caseADefineSubstitution(ADefineSubstitution node){
        //TODO: Translation Define Substitution
    }

    @Override
    public void caseADefinitionSubstitution(ADefinitionSubstitution node){
        //TODO: Translation Definition Substitution
    }

    @Override
    public void caseAForallSubMessageSubstitution(AForallSubMessageSubstitution node){
        //TODO: Translation Forall Sub Message Substitution
    }

    @Override
    public void caseAForLoopSubstitution(AForLoopSubstitution node){
        //TODO: Translation For Loop Substitution
    }

    @Override
    public void caseAFuncOpSubstitution(AFuncOpSubstitution node){
        //TODO: Translation Func Op Substitution
    }

    @Override
    public void caseAInvalidSubstitution(AInvalidSubstitution node){
        //TODO: Translation Invalid Substitution
    }

    @Override
    public void caseAOperationCallSubstitution(AOperationCallSubstitution node){
        //TODO: Translation Operation Call Substitution
    }

    @Override
    public void caseAOperatorSubstitution(AOperatorSubstitution node){
        //TODO: Translation Operator Substitution
    }

    @Override
    public void caseAOpSubstitution(AOpSubstitution node){
        List<ExprNode> arguments = new ArrayList<>();
        arguments.addAll(coordinator.convertExpressionNode(node.getParameters(), machineNode));
        List<String> names = new ArrayList<>();
        names.add(node.getName().toString().replace(" ", ""));
        resultSubstitutionNode = new OperationCallSubstitutionNode(getSourceCodePosition(node),
                names,
                arguments);
    }

    @Override
    public void caseARuleFailSubSubstitution(ARuleFailSubSubstitution node){
        //TODO: Translation Rule Fail Substitution
    }

    @Override
    public void caseASelectWhenSubstitution(ASelectWhenSubstitution node) {
        //TODO: Translation Select When Substitution
    }

    @Override
    public void caseASequenceSubstitution(ASequenceSubstitution node) {
        //TODO: Translation Sequence Substitution
        resultSubstitutionNode = new ListSubstitutionNode(getSourceCodePosition(node),
                ListSubstitutionNode.ListOperator.Sequential,
                coordinator.convertSubstitutionNode(node.getSubstitutions(), machineNode));
    }

    @Override
    public void caseAVarSubstitution(AVarSubstitution node) {
        List<DeclarationNode> declarationNode = new ArrayList<>();
        for (PExpression expression : node.getIdentifiers()) {
            String name = expression.toString().replace(" ", "");
            DeclarationNode decl = new DeclarationNode(getSourceCodePosition(expression),
                    name,
                    DeclarationNode.Kind.SUBSTITUION_IDENTIFIER,
                    null);
            declarationNode.add(decl);
        }

        resultSubstitutionNode = new VarSubstitutionNode(getSourceCodePosition(node),
                declarationNode,
                coordinator.convertSubstitutionNode(node.getSubstitution(), machineNode));
    }

    @Override
    public void caseAWhileSubstitution(AWhileSubstitution node) {
        resultSubstitutionNode = new WhileSubstitutionNode(getSourceCodePosition(node),
                coordinator.convertPredicateNode(node.getCondition(), machineNode),
                coordinator.convertSubstitutionNode(node.getDoSubst(), machineNode),
                coordinator.convertPredicateNode(node.getInvariant(), machineNode),
                coordinator.convertExpressionNode(node.getVariant(), machineNode));
    }


    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
