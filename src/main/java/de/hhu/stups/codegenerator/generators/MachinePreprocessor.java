package de.hhu.stups.codegenerator.generators;

import de.prob.parser.antlr.MachineASTCreator;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.Node;
import de.prob.parser.ast.nodes.OperationNode;
import de.prob.parser.ast.nodes.expression.ExprNode;
import de.prob.parser.ast.nodes.expression.ExpressionOperatorNode;
import de.prob.parser.ast.nodes.expression.IdentifierExprNode;
import de.prob.parser.ast.nodes.expression.IfExpressionNode;
import de.prob.parser.ast.nodes.expression.LambdaNode;
import de.prob.parser.ast.nodes.expression.LetExpressionNode;
import de.prob.parser.ast.nodes.expression.NumberNode;
import de.prob.parser.ast.nodes.expression.QuantifiedExpressionNode;
import de.prob.parser.ast.nodes.expression.RealNumberNode;
import de.prob.parser.ast.nodes.expression.RecordFieldAccessNode;
import de.prob.parser.ast.nodes.expression.RecordNode;
import de.prob.parser.ast.nodes.expression.SetComprehensionNode;
import de.prob.parser.ast.nodes.expression.StringNode;
import de.prob.parser.ast.nodes.expression.StructNode;
import de.prob.parser.ast.nodes.ltl.LTLBPredicateNode;
import de.prob.parser.ast.nodes.ltl.LTLInfixOperatorNode;
import de.prob.parser.ast.nodes.ltl.LTLKeywordNode;
import de.prob.parser.ast.nodes.ltl.LTLPrefixOperatorNode;
import de.prob.parser.ast.nodes.predicate.CastPredicateExpressionNode;
import de.prob.parser.ast.nodes.predicate.IdentifierPredicateNode;
import de.prob.parser.ast.nodes.predicate.IfPredicateNode;
import de.prob.parser.ast.nodes.predicate.LetPredicateNode;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.nodes.predicate.PredicateOperatorNode;
import de.prob.parser.ast.nodes.predicate.PredicateOperatorWithExprArgsNode;
import de.prob.parser.ast.nodes.predicate.QuantifiedPredicateNode;
import de.prob.parser.ast.nodes.substitution.AnySubstitutionNode;
import de.prob.parser.ast.nodes.substitution.AssignSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.BecomesElementOfSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.BecomesSuchThatSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.ChoiceSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.ConditionSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.IfOrSelectSubstitutionsNode;
import de.prob.parser.ast.nodes.substitution.LetSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.ListSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.OperationCallSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.SkipSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.SubstitutionNode;
import de.prob.parser.ast.nodes.substitution.VarSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.WhileSubstitutionNode;
import de.prob.parser.ast.types.BType;
import de.prob.parser.ast.types.BoolType;
import de.prob.parser.ast.types.CoupleType;
import de.prob.parser.ast.types.RecordType;
import de.prob.parser.ast.types.SetType;
import de.prob.parser.ast.types.UntypedType;
import de.prob.parser.ast.visitors.AbstractVisitor;
import de.prob.parser.ast.visitors.TypeChecker;
import de.prob.parser.ast.visitors.TypeErrorException;
import org.antlr.v4.runtime.CharStreams;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.prob.parser.antlr.Antlr4BParser.parseExpression;

public class MachinePreprocessor implements AbstractVisitor<Node, Void> {

    private boolean inEnumeration;

    private TypeChecker typeChecker;

    private MachineNode machineNode;

    private int optimizationVariableCounter;

    public MachinePreprocessor() {
        this.inEnumeration = false;
        optimizationVariableCounter = 0;
    }

    public MachineNode visitMachineNode(MachineNode machineNode) {
        this.machineNode = machineNode;
        try {
            typeChecker = new TypeChecker(machineNode);
        } catch (TypeErrorException e) {
            throw new RuntimeException(e.getMessage());
        }
        
        // TODO: Process other constructs
        if(machineNode.getProperties() != null) {
            machineNode.setProperties(visitPredicateNode(machineNode.getProperties()));
        }
        if(machineNode.getInvariant() != null) {
            machineNode.setInvariant(visitPredicateNode(machineNode.getInvariant()));
        }
        machineNode.setOperations(machineNode.getOperations().stream()
                .map(this::visitOperationNode)
                .collect(Collectors.toList()));
        return machineNode;
    }

    public OperationNode visitOperationNode(OperationNode operationNode) {
        SubstitutionNode substitution = (SubstitutionNode) visitSubstitutionNode(operationNode.getSubstitution(), null);
        return new OperationNode(operationNode.getSourceCodePosition(), operationNode.getName(), operationNode.getOutputParams(), substitution, operationNode.getParams());
    }

    public PredicateNode optimizePredicateNode(PredicateNode predicateNode) {
        if(predicateNode instanceof PredicateOperatorWithExprArgsNode) {
            return (PredicateNode) visitPredicateOperatorWithExprArgs((PredicateOperatorWithExprArgsNode) predicateNode, null);
        } else if(predicateNode instanceof PredicateOperatorNode) {
            return (PredicateNode) visitPredicateOperatorNode((PredicateOperatorNode) predicateNode, null);
        } else if(predicateNode instanceof QuantifiedPredicateNode) {
            return (QuantifiedPredicateNode) visitQuantifiedPredicateNode((QuantifiedPredicateNode) predicateNode, null);
        }
        return predicateNode;
    }

    public PredicateNode visitPredicateNode(PredicateNode predicateNode) {
        if(predicateNode != null) {
            if(predicateNode.getParent() instanceof IfOrSelectSubstitutionsNode) {
                IfOrSelectSubstitutionsNode substitutionsNode = (IfOrSelectSubstitutionsNode) predicateNode.getParent();
                if(substitutionsNode.getParent() instanceof OperationNode) {
                    OperationNode operationNode = (OperationNode) substitutionsNode.getParent();
                    return handlePredicateForEnumeration(predicateNode, operationNode.getParams(), new HashSet<>());
                }
            }
            return optimizePredicateNode(predicateNode);
        }
        return predicateNode;
    }

    public PredicateNode handlePredicateForEnumeration(PredicateNode predicateNode, List<DeclarationNode> declarations, Set<String> declarationsProcessed) {
        if(predicateNode instanceof PredicateOperatorWithExprArgsNode) {
            PredicateOperatorWithExprArgsNode predicate = (PredicateOperatorWithExprArgsNode) predicateNode;
            predicate.setArgumentsList(predicate.getExpressionNodes().stream()
                    .map(expr -> (ExprNode) visitExprNode(expr, null))
                    .collect(Collectors.toList()));
            PredicateOperatorWithExprArgsNode.PredOperatorExprArgs operator = predicate.getOperator();
            ExprNode lhs = predicate.getExpressionNodes().get(0);
            Set<String> declarationsAsString = declarations.stream()
                    .map(DeclarationNode::getName)
                    .collect(Collectors.toSet());
            if(lhs instanceof IdentifierExprNode) {
                String id = ((IdentifierExprNode) lhs).getName();
                if(!declarationsAsString.contains(id) || declarationsProcessed.contains(id)) {
                    return (PredicateNode) visitPredicateOperatorWithExprArgs(predicate, null);
                } else {
                    if(operator != PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF && operator != PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.EQUAL &&
                            operator != PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.INCLUSION && operator != PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.STRICT_INCLUSION) {
                        return (PredicateNode) visitPredicateOperatorWithExprArgs(predicate, null);
                    }
                    declarationsProcessed.add(id);
                }
            }
        } else if(predicateNode instanceof PredicateOperatorNode) {
            PredicateOperatorNode predicate = (PredicateOperatorNode) predicateNode;
            List<PredicateNode> predicates = predicate.getPredicateArguments()
                    .stream()
                    .map(pred -> handlePredicateForEnumeration(pred, declarations, declarationsProcessed))
                    .collect(Collectors.toList());
            return new PredicateOperatorNode(predicate.getSourceCodePosition(), predicate.getOperator(), predicates);
        }
        return predicateNode;
    }

    @Override
    public Node visitExprOperatorNode(ExpressionOperatorNode node, Void expected) {
       ExpressionOperatorNode.ExpressionOperator operator = node.getOperator();
       SourceCodePosition sourceCodePosition = node.getSourceCodePosition();

       switch (operator) {
           case CLOSURE: {
               if(node.getType() instanceof SetType) {
                   ExprNode subNode = node.getExpressionNodes().get(0);
                   BType type = ((CoupleType) ((SetType) node.getType()).getSubType()).getLeft();
                   ExprNode closure1Node = new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(subNode), ExpressionOperatorNode.ExpressionOperator.CLOSURE1);
                   closure1Node.setType(node.getType());
                   ExprNode typeNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(type.toString())));
                   typeNode.setType(new SetType(type));
                   ExprNode identityNode = new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(typeNode), ExpressionOperatorNode.ExpressionOperator.ID);
                   identityNode.setType(node.getType());
                   ExprNode resultNode = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(closure1Node, identityNode), ExpressionOperatorNode.ExpressionOperator.UNION);
                   resultNode.setType(node.getType());
                   return resultNode;
               }
           }
           case ITERATE: {
               if(node.getType() instanceof SetType) {

                   BType type = ((CoupleType) ((SetType) node.getType()).getSubType()).getLeft();
                   ExprNode typeNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(type.toString())));
                   typeNode.setType(new SetType(type));
                   ExprNode identityNode = new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(typeNode), ExpressionOperatorNode.ExpressionOperator.ID);
                   identityNode.setType(node.getType());
                   ExprNode compositionNode = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(identityNode, node), ExpressionOperatorNode.ExpressionOperator.COMPOSITION);
                   compositionNode.setType(node.getType());

                   ExprNode numberNode = node.getExpressionNodes().get(1);
                   NumberNode zeroNode = new NumberNode(sourceCodePosition, new BigInteger("0"));
                   PredicateNode condition = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.EQUAL, Arrays.asList(numberNode, zeroNode));

                   ExprNode resultNode = new IfExpressionNode(sourceCodePosition, condition, identityNode, compositionNode);
                   resultNode.setType(node.getType());
                   return resultNode;
               }
           }
           /*case POW:
           case FIN:
           case SEQ:
           case SEQ1:
           case ISEQ:
           case ISEQ1:
           case PERM:
               ExpressionOperatorNode expressionNode = new ExpressionOperatorNode(node.getSourceCodePosition(), Collections.emptyList(), ExpressionOperatorNode.ExpressionOperator.EMPTY_SET);
               typeChecker.checkExprNode(expressionNode);
               return expressionNode;*/
           default:
               break;
       }
       ExprNode exprNode = new ExpressionOperatorNode(node.getSourceCodePosition(), node.getExpressionNodes().stream()
                .map(expr -> (ExprNode) visitExprNode(expr, null))
                .collect(Collectors.toList()),
                node.getOperator());
       exprNode.setType(node.getType());
       return exprNode;
    }

    @Override
    public Node visitIdentifierExprNode(IdentifierExprNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitCastPredicateExpressionNode(CastPredicateExpressionNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitNumberNode(NumberNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitRealNumberNode(RealNumberNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitQuantifiedExpressionNode(QuantifiedExpressionNode node, Void expected) {
        QuantifiedExpressionNode exprNode = new QuantifiedExpressionNode(node.getSourceCodePosition(), node.getOperator(), node.getDeclarationList(),
                node.getPredicateNode(), (ExprNode) visitExprNode(node.getExpressionNode(), null));
        exprNode.setType(node.getType());
        return exprNode;
    }

    @Override
    public Node visitSetComprehensionNode(SetComprehensionNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitLambdaNode(LambdaNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitLetExpressionNode(LetExpressionNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitIfExpressionNode(IfExpressionNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitStringNode(StringNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitRecordNode(RecordNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitStructNode(StructNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitRecordFieldAccessNode(RecordFieldAccessNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitLTLPrefixOperatorNode(LTLPrefixOperatorNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitLTLKeywordNode(LTLKeywordNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitLTLInfixOperatorNode(LTLInfixOperatorNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitLTLBPredicateNode(LTLBPredicateNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitIdentifierPredicateNode(IdentifierPredicateNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitPredicateOperatorNode(PredicateOperatorNode node, Void expected) {
        return new PredicateOperatorNode(node.getSourceCodePosition(), node.getOperator(), node.getPredicateArguments().stream()
                .map(this::visitPredicateNode)
                .collect(Collectors.toList()));
    }

    @Override
    public Node visitPredicateOperatorWithExprArgs(PredicateOperatorWithExprArgsNode node, Void expected) {
        PredicateOperatorWithExprArgsNode.PredOperatorExprArgs operator = node.getOperator();
        if(operator == PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF) {
            return optimizeElementOf(node);
        } else if(operator == PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING) {
           return optimizeNotElementOf(node);
        } else if(operator == PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.INCLUSION) {
            return optimizeSubsetOf(node);
        } else if(operator == PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NON_INCLUSION) {
            return optimizeNotSubsetOf(node);
        }
        PredicateOperatorWithExprArgsNode result = new PredicateOperatorWithExprArgsNode(node.getSourceCodePosition(), operator,
                node.getExpressionNodes().stream()
                        .map(expr -> (ExprNode) visitExprNode(expr, null))
                        .collect(Collectors.toList()));
        result.setType(BoolType.getInstance());
        return result;
    }

    private PredicateNode optimizeElementOf(PredicateOperatorWithExprArgsNode node) {
        SourceCodePosition sourceCodePosition = node.getSourceCodePosition();
        ExprNode lhs = node.getExpressionNodes().get(0);
        ExprNode rhs = (ExprNode) visitExprNode(node.getExpressionNodes().get(1), null);

        ExprNode newRhs;
        if(rhs instanceof ExpressionOperatorNode) {
            ExpressionOperatorNode rhsAsExpression = ((ExpressionOperatorNode) rhs);
            ExpressionOperatorNode.ExpressionOperator rhsOperator = rhsAsExpression.getOperator();
            switch(rhsOperator) {
                case NAT: {
                    ExprNode newExpression = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(
                            new NumberNode(sourceCodePosition, new BigInteger(String.valueOf(0))),
                            new ExpressionOperatorNode(sourceCodePosition, ExpressionOperatorNode.ExpressionOperator.MAXINT)
                    ), ExpressionOperatorNode.ExpressionOperator.INTERVAL);
                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(lhs, newExpression));
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case NAT1: {
                    ExprNode newExpression = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(
                            new NumberNode(sourceCodePosition, new BigInteger(String.valueOf(1))),
                            new ExpressionOperatorNode(sourceCodePosition, ExpressionOperatorNode.ExpressionOperator.MAXINT)
                    ), ExpressionOperatorNode.ExpressionOperator.INTERVAL);
                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(lhs, newExpression));
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case INT: {
                    ExprNode newExpression = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(
                            new ExpressionOperatorNode(sourceCodePosition, ExpressionOperatorNode.ExpressionOperator.MININT),
                            new ExpressionOperatorNode(sourceCodePosition, ExpressionOperatorNode.ExpressionOperator.MAXINT)
                    ), ExpressionOperatorNode.ExpressionOperator.INTERVAL);
                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(lhs, newExpression));
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case SET_ENUMERATION: {
                    if (rhsAsExpression.getExpressionNodes().size() == 1) {
                        newRhs = rhsAsExpression.getExpressionNodes().get(0);
                        List<ExprNode> expressions = new ArrayList<>();
                        expressions.add(lhs);
                        expressions.add(newRhs);
                        PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.EQUAL, expressions);
                        predicateNode.setType(new UntypedType());
                        typeChecker.checkPredicateNode(predicateNode);
                        predicateNode = visitPredicateNode(predicateNode);
                        return predicateNode;
                    }
                    return node;
                }
                case INTERVAL: {
                    List<PredicateNode> predicates = new ArrayList<>();
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.GREATER_EQUAL, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.LESS_EQUAL, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(1))));
                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case UNION: {
                    List<PredicateNode> predicates = new ArrayList<>();
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(1))));
                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case INTERSECTION: {
                    List<PredicateNode> predicates = new ArrayList<>();
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(1))));
                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case SET_SUBTRACTION: {
                    List<PredicateNode> predicates = new ArrayList<>();
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(1))));
                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case FIN:
                case POW: {
                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.INCLUSION, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0)));
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case FIN1:
                case POW1: {
                    // Do not iterate over empty sets; rather keep original code
                    if(lhs instanceof ExpressionOperatorNode) {
                        if(((ExpressionOperatorNode) lhs).getOperator() == ExpressionOperatorNode.ExpressionOperator.SET_ENUMERATION) {
                            if(((ExpressionOperatorNode) lhs).getExpressionNodes().size() == 0) {
                                return node;
                            }
                        }
                    }
                    List<PredicateNode> predicates = new ArrayList<>();
                    // We can assume in the code generator that the cardinality is WD as we do not support code generation for infinite sets explicitly
                    ExpressionOperatorNode cardSetNode = new ExpressionOperatorNode(node.getSourceCodePosition(), Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD);
                    NumberNode zeroNode = new NumberNode(node.getSourceCodePosition(), new BigInteger("0"));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.GREATER, Arrays.asList(cardSetNode, zeroNode)));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.INCLUSION, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0))));
                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case ID: {
                    List<PredicateNode> predicates = new ArrayList<>();


                    BType leftType = ((CoupleType) lhs.getType()).getLeft();
                    BType rightType = ((CoupleType) lhs.getType()).getRight();


                    ExprNode leftNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType.toString())));
                    ExprNode rightNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType.toString())));

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.EQUAL, Arrays.asList(projection1, projection2)));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(projection1, rhsAsExpression.getExpressionNodes().get(0))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case INVERSE_RELATION: {
                    BType leftType = ((CoupleType) lhs.getType()).getLeft();
                    BType rightType = ((CoupleType) lhs.getType()).getRight();
                    


                    ExprNode leftNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType.toString())));
                    ExprNode rightNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType.toString())));

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    ExpressionOperatorNode mapletProjection = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(projection2, projection1), ExpressionOperatorNode.ExpressionOperator.COUPLE);

                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(mapletProjection, rhsAsExpression.getExpressionNodes().get(0)));
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case CARTESIAN_PRODUCT: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftType = ((CoupleType) lhs.getType()).getLeft();
                    BType rightType = ((CoupleType) lhs.getType()).getRight();
                    

                    ExprNode leftNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType.toString())));
                    ExprNode rightNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType.toString())));

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(projection1, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(projection2, rhsAsExpression.getExpressionNodes().get(1))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);
                    predicateNode.setType(new UntypedType());

                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case RANGE_SUBTRACTION: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftType = ((CoupleType) lhs.getType()).getLeft();
                    BType rightType = ((CoupleType) lhs.getType()).getRight();
                    


                    ExprNode leftNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType.toString())));
                    ExprNode rightNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType.toString())));

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(projection2, rhsAsExpression.getExpressionNodes().get(1))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case RANGE_RESTRICTION: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftType = ((CoupleType) lhs.getType()).getLeft();
                    BType rightType = ((CoupleType) lhs.getType()).getRight();
                    


                    ExprNode leftNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType.toString())));
                    ExprNode rightNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType.toString())));

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(projection2, rhsAsExpression.getExpressionNodes().get(1))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case DOMAIN_SUBTRACTION: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftType = ((CoupleType) lhs.getType()).getLeft();
                    BType rightType = ((CoupleType) lhs.getType()).getRight();
                    


                    ExprNode leftNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType.toString())));
                    ExprNode rightNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType.toString())));

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(projection1, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(1))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case DOMAIN_RESTRICTION: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftType = ((CoupleType) lhs.getType()).getLeft();
                    BType rightType = ((CoupleType) lhs.getType()).getRight();
                    


                    ExprNode leftNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType.toString())));
                    ExprNode rightNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType.toString())));

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(projection1, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(1))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case PARALLEL_PRODUCT: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftTypeTopLevel = ((CoupleType) lhs.getType()).getLeft();
                    BType rightTypeTopLevel = ((CoupleType) lhs.getType()).getRight();

                    ExprNode leftNodeTopLevel = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftTypeTopLevel.toString())));
                    ExprNode rightNodeTopLevel = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightTypeTopLevel.toString())));

                    ExpressionOperatorNode innerLhs = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNodeTopLevel, rightNodeTopLevel), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode innerRhs = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNodeTopLevel, rightNodeTopLevel), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    BType leftType1 = ((CoupleType) ((CoupleType) lhs.getType()).getLeft()).getLeft();
                    BType rightType1 = ((CoupleType) ((CoupleType) lhs.getType()).getLeft()).getRight();

                    ExprNode leftNode1 = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType1.toString())));
                    ExprNode rightNode1 = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType1.toString())));

                    ExpressionOperatorNode projection11 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode1, rightNode1), ExpressionOperatorNode.ExpressionOperator.PRJ1), innerLhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection12 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode1, rightNode1), ExpressionOperatorNode.ExpressionOperator.PRJ2), innerLhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    BType leftType2 = ((CoupleType) ((CoupleType) lhs.getType()).getRight()).getLeft();
                    BType rightType2 = ((CoupleType) ((CoupleType) lhs.getType()).getRight()).getRight();

                    ExprNode leftNode2 = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType2.toString())));
                    ExprNode rightNode2 = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType2.toString())));

                    ExpressionOperatorNode projection21 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode2, rightNode2), ExpressionOperatorNode.ExpressionOperator.PRJ1), innerRhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection22 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode2, rightNode2), ExpressionOperatorNode.ExpressionOperator.PRJ2), innerRhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(projection11, projection21), ExpressionOperatorNode.ExpressionOperator.COUPLE);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(projection12, projection22), ExpressionOperatorNode.ExpressionOperator.COUPLE);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(projection1, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(projection2, rhsAsExpression.getExpressionNodes().get(1))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case DIRECT_PRODUCT: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftTypeTopLevel = ((CoupleType) lhs.getType()).getLeft();
                    BType rightTypeTopLevel = ((CoupleType) lhs.getType()).getRight();

                    ExprNode leftNodeTopLevel = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftTypeTopLevel.toString())));
                    ExprNode rightNodeTopLevel = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightTypeTopLevel.toString())));

                    ExpressionOperatorNode innerLhs = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNodeTopLevel, rightNodeTopLevel), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode innerRhs = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNodeTopLevel, rightNodeTopLevel), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    BType rightType1 = ((CoupleType) ((CoupleType) lhs.getType()).getRight()).getLeft();
                    BType rightType2 = ((CoupleType) ((CoupleType) lhs.getType()).getRight()).getRight();

                    ExprNode rightNode1 = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType1.toString())));
                    ExprNode rightNode2 = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType2.toString())));

                    ExpressionOperatorNode projection21 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(rightNode1, rightNode2), ExpressionOperatorNode.ExpressionOperator.PRJ1), innerRhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection22 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(rightNode1, rightNode2), ExpressionOperatorNode.ExpressionOperator.PRJ2), innerRhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(innerLhs, projection21), ExpressionOperatorNode.ExpressionOperator.COUPLE);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(innerLhs, projection22), ExpressionOperatorNode.ExpressionOperator.COUPLE);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(projection1, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(projection2, rhsAsExpression.getExpressionNodes().get(1))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case SEQ: {

                    List<ExprNode> expressions = new ArrayList<>();

                    List<ExprNode> innerExpressions = new ArrayList<>();
                    List<ExprNode> lhsExpressions = new ArrayList<>();
                    lhsExpressions.add(new NumberNode(sourceCodePosition, new BigInteger("1")));
                    lhsExpressions.add(new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD));
                    ExpressionOperatorNode lhsExpression = new ExpressionOperatorNode(sourceCodePosition, lhsExpressions, ExpressionOperatorNode.ExpressionOperator.INTERVAL);

                    innerExpressions.add(lhsExpression);
                    innerExpressions.add(rhsAsExpression.getExpressionNodes().get(0));
                    ExpressionOperatorNode totalFunctionExpression = new ExpressionOperatorNode(sourceCodePosition, innerExpressions, ExpressionOperatorNode.ExpressionOperator.TOTAL_FUNCTION);

                    expressions.add(lhs);
                    expressions.add(totalFunctionExpression);

                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, expressions);

                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case SEQ1: {

                    List<ExprNode> expressions = new ArrayList<>();

                    List<ExprNode> innerExpressions = new ArrayList<>();
                    List<ExprNode> lhsExpressions = new ArrayList<>();
                    lhsExpressions.add(new NumberNode(sourceCodePosition, new BigInteger("1")));
                    lhsExpressions.add(new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD));
                    ExpressionOperatorNode lhsExpression = new ExpressionOperatorNode(sourceCodePosition, lhsExpressions, ExpressionOperatorNode.ExpressionOperator.INTERVAL);

                    innerExpressions.add(lhsExpression);
                    innerExpressions.add(rhsAsExpression.getExpressionNodes().get(0));
                    ExpressionOperatorNode totalFunctionExpression = new ExpressionOperatorNode(sourceCodePosition, innerExpressions, ExpressionOperatorNode.ExpressionOperator.TOTAL_FUNCTION);

                    expressions.add(lhs);
                    expressions.add(totalFunctionExpression);

                    ExpressionOperatorNode cardSetNode = new ExpressionOperatorNode(node.getSourceCodePosition(), Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD);
                    NumberNode zeroNode = new NumberNode(node.getSourceCodePosition(), new BigInteger("0"));

                    List<PredicateNode> predicates = new ArrayList<>();
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.GREATER, Arrays.asList(cardSetNode, zeroNode)));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, expressions));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);

                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case ISEQ: {

                    List<ExprNode> expressions = new ArrayList<>();

                    List<ExprNode> innerExpressions = new ArrayList<>();
                    List<ExprNode> lhsExpressions = new ArrayList<>();
                    lhsExpressions.add(new NumberNode(sourceCodePosition, new BigInteger("1")));
                    lhsExpressions.add(new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD));
                    ExpressionOperatorNode lhsExpression = new ExpressionOperatorNode(sourceCodePosition, lhsExpressions, ExpressionOperatorNode.ExpressionOperator.INTERVAL);

                    innerExpressions.add(lhsExpression);
                    innerExpressions.add(rhsAsExpression.getExpressionNodes().get(0));
                    ExpressionOperatorNode totalFunctionExpression = new ExpressionOperatorNode(sourceCodePosition, innerExpressions, ExpressionOperatorNode.ExpressionOperator.TOTAL_INJECTION);

                    expressions.add(lhs);
                    expressions.add(totalFunctionExpression);

                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, expressions);

                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case ISEQ1: {

                    List<ExprNode> expressions = new ArrayList<>();

                    List<ExprNode> innerExpressions = new ArrayList<>();
                    List<ExprNode> lhsExpressions = new ArrayList<>();
                    lhsExpressions.add(new NumberNode(sourceCodePosition, new BigInteger("1")));
                    lhsExpressions.add(new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD));
                    ExpressionOperatorNode lhsExpression = new ExpressionOperatorNode(sourceCodePosition, lhsExpressions, ExpressionOperatorNode.ExpressionOperator.INTERVAL);

                    innerExpressions.add(lhsExpression);
                    innerExpressions.add(rhsAsExpression.getExpressionNodes().get(0));
                    ExpressionOperatorNode totalFunctionExpression = new ExpressionOperatorNode(sourceCodePosition, innerExpressions, ExpressionOperatorNode.ExpressionOperator.TOTAL_INJECTION);

                    expressions.add(lhs);
                    expressions.add(totalFunctionExpression);

                    ExpressionOperatorNode cardSetNode = new ExpressionOperatorNode(node.getSourceCodePosition(), Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD);
                    NumberNode zeroNode = new NumberNode(node.getSourceCodePosition(), new BigInteger("0"));

                    List<PredicateNode> predicates = new ArrayList<>();
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.GREATER, Arrays.asList(cardSetNode, zeroNode)));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, expressions));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);

                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case PERM: {

                    List<ExprNode> expressions = new ArrayList<>();

                    List<ExprNode> innerExpressions = new ArrayList<>();
                    List<ExprNode> lhsExpressions = new ArrayList<>();
                    lhsExpressions.add(new NumberNode(sourceCodePosition, new BigInteger("1")));
                    lhsExpressions.add(new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD));
                    ExpressionOperatorNode lhsExpression = new ExpressionOperatorNode(sourceCodePosition, lhsExpressions, ExpressionOperatorNode.ExpressionOperator.INTERVAL);

                    innerExpressions.add(lhsExpression);
                    innerExpressions.add(rhsAsExpression.getExpressionNodes().get(0));
                    ExpressionOperatorNode totalFunctionExpression = new ExpressionOperatorNode(sourceCodePosition, innerExpressions, ExpressionOperatorNode.ExpressionOperator.TOTAL_BIJECTION);

                    expressions.add(lhs);
                    expressions.add(totalFunctionExpression);

                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, expressions);

                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case SET_RELATION: {
                    List<ExprNode> rhsNodes = ((ExpressionOperatorNode) rhs).getExpressionNodes();
                    ExprNode first = rhsNodes.get(0);
                    ExprNode second = rhsNodes.get(1);
                    ExprNode cartesianProductExpr = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(first, second), ExpressionOperatorNode.ExpressionOperator.CARTESIAN_PRODUCT);
                    ExprNode powExpr = new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(cartesianProductExpr), ExpressionOperatorNode.ExpressionOperator.POW);

                    List<ExprNode> expressions = new ArrayList<>();
                    expressions.add(lhs);
                    expressions.add(powExpr);

                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, expressions);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                default:
                    break;
            }
        }
        return node;
    }

    private PredicateNode optimizeSubsetOf(PredicateOperatorWithExprArgsNode node) {
        SourceCodePosition sourceCodePosition = node.getSourceCodePosition();
        ExprNode lhs = node.getExpressionNodes().get(0);
        ExprNode rhs = node.getExpressionNodes().get(1);

        // Do not iterate over empty sets; rather keep original code
        if(lhs instanceof ExpressionOperatorNode) {
            if(((ExpressionOperatorNode) lhs).getOperator() == ExpressionOperatorNode.ExpressionOperator.SET_ENUMERATION) {
                if(((ExpressionOperatorNode) lhs).getExpressionNodes().size() == 0) {
                    return node;
                }
            }
        }

        if(rhs instanceof ExpressionOperatorNode) {
            ExpressionOperatorNode.ExpressionOperator operator = ((ExpressionOperatorNode) rhs).getOperator();
            if(operator == ExpressionOperatorNode.ExpressionOperator.CARTESIAN_PRODUCT) {
                if (((ExpressionOperatorNode) rhs).getExpressionNodes().get(0) instanceof IdentifierExprNode && ((ExpressionOperatorNode) rhs).getExpressionNodes().get(1) instanceof IdentifierExprNode) {
                    return node;
                }
            } else if(operator == ExpressionOperatorNode.ExpressionOperator.ID) {
                if (((ExpressionOperatorNode) rhs).getExpressionNodes().get(0) instanceof IdentifierExprNode) {
                    return node;
                }
            }
        }

        optimizationVariableCounter++;
        DeclarationNode declarationNode = new DeclarationNode(sourceCodePosition, "_opt_" + optimizationVariableCounter, DeclarationNode.Kind.VARIABLE, machineNode);
        typeChecker.setDeclarationTypes(Collections.singletonList(declarationNode));
        declarationNode.setType(((SetType) lhs.getType()).getSubType());
        IdentifierExprNode newIdentifierNode = new IdentifierExprNode(sourceCodePosition, "_opt_" + optimizationVariableCounter, false);
        newIdentifierNode.setType(((SetType) lhs.getType()).getSubType());
        PredicateOperatorWithExprArgsNode firstPredicate = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(newIdentifierNode, lhs));
        PredicateNode innerPredicate = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(newIdentifierNode, rhs));
        innerPredicate.setType(new UntypedType());
        typeChecker.checkPredicateNode(innerPredicate);
        // Optimize inner predicates in quantified constructs for better performance
        innerPredicate = optimizeElementOf((PredicateOperatorWithExprArgsNode) innerPredicate);

        PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.IMPLIES, Arrays.asList(firstPredicate, innerPredicate));
        QuantifiedPredicateNode result = new QuantifiedPredicateNode(sourceCodePosition, Collections.singletonList(declarationNode), predicateNode, QuantifiedPredicateNode.QuantifiedPredicateOperator.UNIVERSAL_QUANTIFICATION);
        result = (QuantifiedPredicateNode) visitPredicateNode(result);
        result.setType(new UntypedType());
        typeChecker.checkPredicateNode(result);
        return result;
    }

    private PredicateNode optimizeNotSubsetOf(PredicateOperatorWithExprArgsNode node) {
        SourceCodePosition sourceCodePosition = node.getSourceCodePosition();
        ExprNode lhs = node.getExpressionNodes().get(0);
        ExprNode rhs = node.getExpressionNodes().get(1);

        // Do not iterate over empty sets; rather keep original code
        if(lhs instanceof ExpressionOperatorNode) {
            if(((ExpressionOperatorNode) lhs).getOperator() == ExpressionOperatorNode.ExpressionOperator.SET_ENUMERATION) {
                if(((ExpressionOperatorNode) lhs).getExpressionNodes().size() == 0) {
                    return node;
                }
            }
        }

        if(rhs instanceof ExpressionOperatorNode) {
            ExpressionOperatorNode.ExpressionOperator operator = ((ExpressionOperatorNode) rhs).getOperator();
            if(operator == ExpressionOperatorNode.ExpressionOperator.CARTESIAN_PRODUCT) {
                if (((ExpressionOperatorNode) rhs).getExpressionNodes().get(0) instanceof IdentifierExprNode && ((ExpressionOperatorNode) rhs).getExpressionNodes().get(1) instanceof IdentifierExprNode) {
                    return node;
                }
            } else if(operator == ExpressionOperatorNode.ExpressionOperator.ID) {
                if (((ExpressionOperatorNode) rhs).getExpressionNodes().get(0) instanceof IdentifierExprNode) {
                    return node;
                }
            }
        }


        optimizationVariableCounter++;
        DeclarationNode declarationNode = new DeclarationNode(sourceCodePosition, "_opt_" + optimizationVariableCounter, DeclarationNode.Kind.VARIABLE, machineNode);
        typeChecker.setDeclarationTypes(Collections.singletonList(declarationNode));
        declarationNode.setType(((SetType) lhs.getType()).getSubType());
        IdentifierExprNode newIdentifierNode = new IdentifierExprNode(sourceCodePosition, "_opt_" + optimizationVariableCounter, false);
        newIdentifierNode.setType(((SetType) lhs.getType()).getSubType());
        PredicateOperatorWithExprArgsNode firstPredicate = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(newIdentifierNode, lhs));
        PredicateNode innerPredicate = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(newIdentifierNode, rhs));
        innerPredicate.setType(new UntypedType());
        typeChecker.checkPredicateNode(innerPredicate);
        // Optimize inner predicates in quantified constructs for better performance
        innerPredicate = optimizeNotElementOf((PredicateOperatorWithExprArgsNode) innerPredicate);
        PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, Arrays.asList(firstPredicate, innerPredicate));
        QuantifiedPredicateNode result = new QuantifiedPredicateNode(sourceCodePosition, Collections.singletonList(declarationNode), predicateNode, QuantifiedPredicateNode.QuantifiedPredicateOperator.EXISTENTIAL_QUANTIFICATION);
        result = (QuantifiedPredicateNode) visitPredicateNode(result);
        result.setType(new UntypedType());
        typeChecker.checkPredicateNode(result);
        return result;
    }

    private PredicateNode optimizeNotElementOf(PredicateOperatorWithExprArgsNode node) {
        SourceCodePosition sourceCodePosition = node.getSourceCodePosition();
        ExprNode lhs = node.getExpressionNodes().get(0);
        ExprNode rhs = node.getExpressionNodes().get(1);

        if(rhs instanceof ExpressionOperatorNode) {
            ExpressionOperatorNode rhsAsExpression = ((ExpressionOperatorNode) rhs);
            ExpressionOperatorNode.ExpressionOperator rhsOperator = rhsAsExpression.getOperator();
            switch(rhsOperator) {
                case NAT: {
                    ExprNode newExpression = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(
                            new NumberNode(sourceCodePosition, new BigInteger(String.valueOf(0))),
                            new ExpressionOperatorNode(sourceCodePosition, ExpressionOperatorNode.ExpressionOperator.MAXINT)
                    ), ExpressionOperatorNode.ExpressionOperator.INTERVAL);
                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(lhs, newExpression));
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case NAT1: {
                    ExprNode newExpression = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(
                            new NumberNode(sourceCodePosition, new BigInteger(String.valueOf(1))),
                            new ExpressionOperatorNode(sourceCodePosition, ExpressionOperatorNode.ExpressionOperator.MAXINT)
                    ), ExpressionOperatorNode.ExpressionOperator.INTERVAL);
                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(lhs, newExpression));
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case INT: {
                    ExprNode newExpression = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(
                            new ExpressionOperatorNode(sourceCodePosition, ExpressionOperatorNode.ExpressionOperator.MININT),
                            new ExpressionOperatorNode(sourceCodePosition, ExpressionOperatorNode.ExpressionOperator.MAXINT)
                    ), ExpressionOperatorNode.ExpressionOperator.INTERVAL);
                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(lhs, newExpression));
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case INTERVAL: {
                    List<PredicateNode> predicates = new ArrayList<>();
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.LESS, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.GREATER, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(1))));
                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case UNION: {
                    List<PredicateNode> predicates = new ArrayList<>();
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(1))));
                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.AND, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);return predicateNode;
                }
                case INTERSECTION: {
                    List<PredicateNode> predicates = new ArrayList<>();
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(1))));
                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case SET_SUBTRACTION: {
                    List<PredicateNode> predicates = new ArrayList<>();
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(1))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0))));
                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case FIN:
                case POW: {
                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NON_INCLUSION, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0)));
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case FIN1:
                case POW1: {
                    if(lhs instanceof ExpressionOperatorNode) {
                        if(((ExpressionOperatorNode) lhs).getOperator() == ExpressionOperatorNode.ExpressionOperator.SET_ENUMERATION) {
                            if(((ExpressionOperatorNode) lhs).getExpressionNodes().size() == 0) {
                                return node;
                            }
                        }
                    }
                    List<PredicateNode> predicates = new ArrayList<>();
                    // We can assume in the code generator that the cardinality is WD as we do not support code generation for infinite sets explicitly
                    ExpressionOperatorNode cardSetNode = new ExpressionOperatorNode(node.getSourceCodePosition(), Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD);
                    NumberNode zeroNode = new NumberNode(node.getSourceCodePosition(), new BigInteger("0"));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.EQUAL, Arrays.asList(cardSetNode, zeroNode)));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NON_INCLUSION, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0))));
                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case ID: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftType = ((CoupleType) lhs.getType()).getLeft();
                    BType rightType = ((CoupleType) lhs.getType()).getRight();
                    


                    ExprNode leftNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType.toString())));
                    ExprNode rightNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType.toString())));

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_EQUAL, Arrays.asList(projection1, projection2)));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(projection1, rhsAsExpression.getExpressionNodes().get(0))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case INVERSE_RELATION: {
                    BType leftType = ((CoupleType) lhs.getType()).getLeft();
                    BType rightType = ((CoupleType) lhs.getType()).getRight();
                    


                    ExprNode leftNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType.toString())));
                    ExprNode rightNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType.toString())));

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    ExpressionOperatorNode mapletProjection = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(projection2, projection1), ExpressionOperatorNode.ExpressionOperator.COUPLE);

                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(mapletProjection, rhsAsExpression.getExpressionNodes().get(0)));
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case CARTESIAN_PRODUCT: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftType = ((CoupleType) lhs.getType()).getLeft();
                    BType rightType = ((CoupleType) lhs.getType()).getRight();
                    


                    ExprNode leftNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType.toString())));
                    ExprNode rightNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType.toString())));

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(projection1, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(projection2, rhsAsExpression.getExpressionNodes().get(1))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case RANGE_SUBTRACTION: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftType = ((CoupleType) lhs.getType()).getLeft();
                    BType rightType = ((CoupleType) lhs.getType()).getRight();
                    


                    ExprNode leftNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType.toString())));
                    ExprNode rightNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType.toString())));

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(projection2, rhsAsExpression.getExpressionNodes().get(1))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case RANGE_RESTRICTION: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftType = ((CoupleType) lhs.getType()).getLeft();
                    BType rightType = ((CoupleType) lhs.getType()).getRight();
                    


                    ExprNode leftNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType.toString())));
                    ExprNode rightNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType.toString())));

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(projection2, rhsAsExpression.getExpressionNodes().get(1))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(0))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case DOMAIN_SUBTRACTION: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftType = ((CoupleType) lhs.getType()).getLeft();
                    BType rightType = ((CoupleType) lhs.getType()).getRight();
                    


                    ExprNode leftNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType.toString())));
                    ExprNode rightNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType.toString())));

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF, Arrays.asList(projection1, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(1))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case DOMAIN_RESTRICTION: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftType = ((CoupleType) lhs.getType()).getLeft();
                    BType rightType = ((CoupleType) lhs.getType()).getRight();
                    


                    ExprNode leftNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType.toString())));
                    ExprNode rightNode = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType.toString())));

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode, rightNode), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(projection1, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(lhs, rhsAsExpression.getExpressionNodes().get(1))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case PARALLEL_PRODUCT: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftTypeTopLevel = ((CoupleType) lhs.getType()).getLeft();
                    BType rightTypeTopLevel = ((CoupleType) lhs.getType()).getRight();

                    ExprNode leftNodeTopLevel = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftTypeTopLevel.toString())));
                    ExprNode rightNodeTopLevel = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightTypeTopLevel.toString())));

                    ExpressionOperatorNode innerLhs = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNodeTopLevel, rightNodeTopLevel), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode innerRhs = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNodeTopLevel, rightNodeTopLevel), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    BType leftType1 = ((CoupleType) ((CoupleType) lhs.getType()).getLeft()).getLeft();
                    BType rightType1 = ((CoupleType) ((CoupleType) lhs.getType()).getLeft()).getRight();

                    ExprNode leftNode1 = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType1.toString())));
                    ExprNode rightNode1 = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType1.toString())));

                    ExpressionOperatorNode projection11 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode1, rightNode1), ExpressionOperatorNode.ExpressionOperator.PRJ1), innerLhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection12 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode1, rightNode1), ExpressionOperatorNode.ExpressionOperator.PRJ2), innerLhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    BType leftType2 = ((CoupleType) ((CoupleType) lhs.getType()).getRight()).getLeft();
                    BType rightType2 = ((CoupleType) ((CoupleType) lhs.getType()).getRight()).getRight();

                    ExprNode leftNode2 = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftType2.toString())));
                    ExprNode rightNode2 = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType2.toString())));

                    ExpressionOperatorNode projection21 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode2, rightNode2), ExpressionOperatorNode.ExpressionOperator.PRJ1), innerRhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection22 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNode2, rightNode2), ExpressionOperatorNode.ExpressionOperator.PRJ2), innerRhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(projection11, projection21), ExpressionOperatorNode.ExpressionOperator.COUPLE);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(projection12, projection22), ExpressionOperatorNode.ExpressionOperator.COUPLE);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(projection1, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(projection2, rhsAsExpression.getExpressionNodes().get(1))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case DIRECT_PRODUCT: {
                    List<PredicateNode> predicates = new ArrayList<>();

                    BType leftTypeTopLevel = ((CoupleType) lhs.getType()).getLeft();
                    BType rightTypeTopLevel = ((CoupleType) lhs.getType()).getRight();

                    ExprNode leftNodeTopLevel = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(leftTypeTopLevel.toString())));
                    ExprNode rightNodeTopLevel = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightTypeTopLevel.toString())));

                    ExpressionOperatorNode innerLhs = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNodeTopLevel, rightNodeTopLevel), ExpressionOperatorNode.ExpressionOperator.PRJ1), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode innerRhs = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(leftNodeTopLevel, rightNodeTopLevel), ExpressionOperatorNode.ExpressionOperator.PRJ2), lhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    BType rightType1 = ((CoupleType) ((CoupleType) lhs.getType()).getRight()).getLeft();
                    BType rightType2 = ((CoupleType) ((CoupleType) lhs.getType()).getRight()).getRight();

                    ExprNode rightNode1 = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType1.toString())));
                    ExprNode rightNode2 = MachineASTCreator.createExpressionAST(parseExpression(CharStreams.fromString(rightType2.toString())));

                    ExpressionOperatorNode projection21 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(rightNode1, rightNode2), ExpressionOperatorNode.ExpressionOperator.PRJ1), innerRhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);
                    ExpressionOperatorNode projection22 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(rightNode1, rightNode2), ExpressionOperatorNode.ExpressionOperator.PRJ2), innerRhs), ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL);

                    ExpressionOperatorNode projection1 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(innerLhs, projection21), ExpressionOperatorNode.ExpressionOperator.COUPLE);
                    ExpressionOperatorNode projection2 = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(innerLhs, projection22), ExpressionOperatorNode.ExpressionOperator.COUPLE);

                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(projection1, rhsAsExpression.getExpressionNodes().get(0))));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, Arrays.asList(projection2, rhsAsExpression.getExpressionNodes().get(1))));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case SEQ: {

                    List<ExprNode> expressions = new ArrayList<>();

                    List<ExprNode> innerExpressions = new ArrayList<>();
                    List<ExprNode> lhsExpressions = new ArrayList<>();
                    lhsExpressions.add(new NumberNode(sourceCodePosition, new BigInteger("1")));
                    lhsExpressions.add(new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD));
                    ExpressionOperatorNode lhsExpression = new ExpressionOperatorNode(sourceCodePosition, lhsExpressions, ExpressionOperatorNode.ExpressionOperator.INTERVAL);

                    innerExpressions.add(lhsExpression);
                    innerExpressions.add(rhsAsExpression.getExpressionNodes().get(0));
                    ExpressionOperatorNode totalFunctionExpression = new ExpressionOperatorNode(sourceCodePosition, innerExpressions, ExpressionOperatorNode.ExpressionOperator.TOTAL_FUNCTION);

                    expressions.add(lhs);
                    expressions.add(totalFunctionExpression);

                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, expressions);

                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case SEQ1: {

                    List<ExprNode> expressions = new ArrayList<>();

                    List<ExprNode> innerExpressions = new ArrayList<>();
                    List<ExprNode> lhsExpressions = new ArrayList<>();
                    lhsExpressions.add(new NumberNode(sourceCodePosition, new BigInteger("1")));
                    lhsExpressions.add(new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD));
                    ExpressionOperatorNode lhsExpression = new ExpressionOperatorNode(sourceCodePosition, lhsExpressions, ExpressionOperatorNode.ExpressionOperator.INTERVAL);

                    innerExpressions.add(lhsExpression);
                    innerExpressions.add(rhsAsExpression.getExpressionNodes().get(0));
                    ExpressionOperatorNode totalFunctionExpression = new ExpressionOperatorNode(sourceCodePosition, innerExpressions, ExpressionOperatorNode.ExpressionOperator.TOTAL_FUNCTION);

                    expressions.add(lhs);
                    expressions.add(totalFunctionExpression);

                    ExpressionOperatorNode cardSetNode = new ExpressionOperatorNode(node.getSourceCodePosition(), Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD);
                    NumberNode zeroNode = new NumberNode(node.getSourceCodePosition(), new BigInteger("0"));

                    List<PredicateNode> predicates = new ArrayList<>();
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.EQUAL, Arrays.asList(cardSetNode, zeroNode)));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, expressions));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);

                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case ISEQ: {

                    List<ExprNode> expressions = new ArrayList<>();

                    List<ExprNode> innerExpressions = new ArrayList<>();
                    List<ExprNode> lhsExpressions = new ArrayList<>();
                    lhsExpressions.add(new NumberNode(sourceCodePosition, new BigInteger("1")));
                    lhsExpressions.add(new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD));
                    ExpressionOperatorNode lhsExpression = new ExpressionOperatorNode(sourceCodePosition, lhsExpressions, ExpressionOperatorNode.ExpressionOperator.INTERVAL);

                    innerExpressions.add(lhsExpression);
                    innerExpressions.add(rhsAsExpression.getExpressionNodes().get(0));
                    ExpressionOperatorNode totalFunctionExpression = new ExpressionOperatorNode(sourceCodePosition, innerExpressions, ExpressionOperatorNode.ExpressionOperator.TOTAL_INJECTION);

                    expressions.add(lhs);
                    expressions.add(totalFunctionExpression);

                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, expressions);

                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case ISEQ1: {

                    List<ExprNode> expressions = new ArrayList<>();

                    List<ExprNode> innerExpressions = new ArrayList<>();
                    List<ExprNode> lhsExpressions = new ArrayList<>();
                    lhsExpressions.add(new NumberNode(sourceCodePosition, new BigInteger("1")));
                    lhsExpressions.add(new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD));
                    ExpressionOperatorNode lhsExpression = new ExpressionOperatorNode(sourceCodePosition, lhsExpressions, ExpressionOperatorNode.ExpressionOperator.INTERVAL);

                    innerExpressions.add(lhsExpression);
                    innerExpressions.add(rhsAsExpression.getExpressionNodes().get(0));
                    ExpressionOperatorNode totalFunctionExpression = new ExpressionOperatorNode(sourceCodePosition, innerExpressions, ExpressionOperatorNode.ExpressionOperator.TOTAL_INJECTION);

                    expressions.add(lhs);
                    expressions.add(totalFunctionExpression);

                    ExpressionOperatorNode cardSetNode = new ExpressionOperatorNode(node.getSourceCodePosition(), Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD);
                    NumberNode zeroNode = new NumberNode(node.getSourceCodePosition(), new BigInteger("0"));

                    List<PredicateNode> predicates = new ArrayList<>();
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.EQUAL, Arrays.asList(cardSetNode, zeroNode)));
                    predicates.add(new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, expressions));

                    PredicateNode predicateNode = new PredicateOperatorNode(sourceCodePosition, PredicateOperatorNode.PredicateOperator.OR, predicates);

                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case PERM: {

                    List<ExprNode> expressions = new ArrayList<>();

                    List<ExprNode> innerExpressions = new ArrayList<>();
                    List<ExprNode> lhsExpressions = new ArrayList<>();
                    lhsExpressions.add(new NumberNode(sourceCodePosition, new BigInteger("1")));
                    lhsExpressions.add(new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(lhs), ExpressionOperatorNode.ExpressionOperator.CARD));
                    ExpressionOperatorNode lhsExpression = new ExpressionOperatorNode(sourceCodePosition, lhsExpressions, ExpressionOperatorNode.ExpressionOperator.INTERVAL);

                    innerExpressions.add(lhsExpression);
                    innerExpressions.add(rhsAsExpression.getExpressionNodes().get(0));
                    ExpressionOperatorNode totalFunctionExpression = new ExpressionOperatorNode(sourceCodePosition, innerExpressions, ExpressionOperatorNode.ExpressionOperator.TOTAL_BIJECTION);

                    expressions.add(lhs);
                    expressions.add(totalFunctionExpression);

                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, expressions);

                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                case SET_RELATION: {
                    List<ExprNode> rhsNodes = ((ExpressionOperatorNode) rhs).getExpressionNodes();
                    ExprNode first = rhsNodes.get(0);
                    ExprNode second = rhsNodes.get(1);
                    ExprNode cartesianProductExpr = new ExpressionOperatorNode(sourceCodePosition, Arrays.asList(first, second), ExpressionOperatorNode.ExpressionOperator.CARTESIAN_PRODUCT);
                    ExprNode powExpr = new ExpressionOperatorNode(sourceCodePosition, Collections.singletonList(cartesianProductExpr), ExpressionOperatorNode.ExpressionOperator.POW);

                    List<ExprNode> expressions = new ArrayList<>();
                    expressions.add(lhs);
                    expressions.add(powExpr);

                    PredicateNode predicateNode = new PredicateOperatorWithExprArgsNode(sourceCodePosition, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.NOT_BELONGING, expressions);
                    predicateNode.setType(new UntypedType());
                    typeChecker.checkPredicateNode(predicateNode);
                    predicateNode = visitPredicateNode(predicateNode);
                    return predicateNode;
                }
                default:
                    break;
            }
        }
        return node;
    }

    @Override
    public Node visitQuantifiedPredicateNode(QuantifiedPredicateNode node, Void expected) {
        if(node.getOperator() == QuantifiedPredicateNode.QuantifiedPredicateOperator.UNIVERSAL_QUANTIFICATION) {
            PredicateOperatorNode subpredicate = (PredicateOperatorNode) node.getPredicateNode();
            if(subpredicate.getOperator() == PredicateOperatorNode.PredicateOperator.IMPLIES) {
                PredicateNode lhs = subpredicate.getPredicateArguments().get(0);
                PredicateNode rhs = subpredicate.getPredicateArguments().get(1);
                subpredicate = new PredicateOperatorNode(subpredicate.getSourceCodePosition(), PredicateOperatorNode.PredicateOperator.IMPLIES, Arrays.asList(lhs, optimizePredicateNode(rhs)));
                PredicateNode result = new QuantifiedPredicateNode(node.getSourceCodePosition(), node.getDeclarationList(), subpredicate, node.getOperator());
                typeChecker.checkPredicateNode(result);
                return result;
            }
        }
        return node;
    }

    @Override
    public Node visitLetPredicateNode(LetPredicateNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitIfPredicateNode(IfPredicateNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitVarSubstitutionNode(VarSubstitutionNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitWhileSubstitutionNode(WhileSubstitutionNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitListSubstitutionNode(ListSubstitutionNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitIfOrSelectSubstitutionsNode(IfOrSelectSubstitutionsNode node, Void expected) {
        return new IfOrSelectSubstitutionsNode(node.getSourceCodePosition(), node.getOperator(),
                node.getConditions().stream().map(this::visitPredicateNode).collect(Collectors.toList()),
                node.getSubstitutions().stream().map(subs -> (SubstitutionNode) this.visitSubstitutionNode(subs, null)).collect(Collectors.toList()),
                node.getElseSubstitution() == null ? null : (SubstitutionNode) visitSubstitutionNode(node.getElseSubstitution(), null));
    }

    @Override
    public Node visitAssignSubstitutionNode(AssignSubstitutionNode node, Void expected) {
        return new AssignSubstitutionNode(node.getSourceCodePosition(), node.getLeftSide(),
                node.getRightSide().stream()
                        .map(expr -> (ExprNode) visitExprNode(expr, null))
                        .collect(Collectors.toList()));
    }

    @Override
    public Node visitSkipSubstitutionNode(SkipSubstitutionNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitConditionSubstitutionNode(ConditionSubstitutionNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitAnySubstitution(AnySubstitutionNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitLetSubstitution(LetSubstitutionNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitBecomesElementOfSubstitutionNode(BecomesElementOfSubstitutionNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitBecomesSuchThatSubstitutionNode(BecomesSuchThatSubstitutionNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitSubstitutionIdentifierCallNode(OperationCallSubstitutionNode node, Void expected) {
        return node;
    }

    @Override
    public Node visitChoiceSubstitutionNode(ChoiceSubstitutionNode node, Void expected) {
        return node;
    }
}
