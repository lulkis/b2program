package de.hhu.stups.codegenerator;


import de.prob.parser.ast.nodes.expression.ExpressionOperatorNode;
import de.prob.parser.ast.nodes.predicate.PredicateOperatorNode;
import de.prob.parser.ast.nodes.predicate.PredicateOperatorWithExprArgsNode;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.CARD;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.COUPLE;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.DIVIDE;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.FALSE;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.INTERSECTION;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.INTERVAL;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.MINUS;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.MOD;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.MULT;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.PLUS;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.RELATIONAL_IMAGE;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.SET_ENUMERATION;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.SET_SUBTRACTION;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.TRUE;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.UNARY_MINUS;
import static de.prob.parser.ast.nodes.expression.ExpressionOperatorNode.ExpressionOperator.UNION;


public class OperatorGenerator {

    @FunctionalInterface
    public interface IOperator {
        Object getOperator();
    }

    private static final List<ExpressionOperatorNode.ExpressionOperator> BINARY_EXPRESSION_OPERATORS =
            Arrays.asList(PLUS,MINUS,MULT,DIVIDE,MOD,INTERSECTION, UNION, SET_SUBTRACTION);

    private static final List<ExpressionOperatorNode.ExpressionOperator> UNARY_EXPRESSION_OPERATORS =
            Arrays.asList(UNARY_MINUS, CARD, RELATIONAL_IMAGE, FUNCTION_CALL);

    private static final List<PredicateOperatorNode.PredicateOperator> BINARY_PREDICATE_OPERATORS =
            Arrays.asList(PredicateOperatorNode.PredicateOperator.AND, PredicateOperatorNode.PredicateOperator.OR,
                        PredicateOperatorNode.PredicateOperator.IMPLIES, PredicateOperatorNode.PredicateOperator.EQUIVALENCE);

    private static final List<PredicateOperatorNode.PredicateOperator> UNARY_PREDICATE_OPERATORS =
            Collections.singletonList(PredicateOperatorNode.PredicateOperator.NOT);

    private static final List<ExpressionOperatorNode.ExpressionOperator> EXPRESSION_BOOLEANS =
            Arrays.asList(TRUE,FALSE);

    private static final List<PredicateOperatorNode.PredicateOperator> PREDICATE_BOOLEANS =
            Arrays.asList(PredicateOperatorNode.PredicateOperator.TRUE, PredicateOperatorNode.PredicateOperator.FALSE);

    private static final List<Object> BINARY_SWAP =
            Collections.singletonList(PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.ELEMENT_OF);

    private final STGroup group;

    public OperatorGenerator(final STGroup group) {
        this.group = group;
    }

    public String generateExpression(ExpressionOperatorNode node, List<String> expressionList) {
        ExpressionOperatorNode.ExpressionOperator operator = node.getOperator();
        if(BINARY_EXPRESSION_OPERATORS.contains(operator)) {
            return generateBinary(() -> operator, expressionList);
        } else if(UNARY_EXPRESSION_OPERATORS.contains(operator)) {
            return generateUnaryExpression(operator, expressionList);
        } else if(EXPRESSION_BOOLEANS.contains(operator)) {
            return generateBoolean(operator);
        } else if(node.getOperator() == SET_ENUMERATION){
            return generateSetEnumeration(expressionList);
        } else if(node.getOperator() == INTERVAL) {
            return generateInterval(expressionList);
        } else if(node.getOperator() == COUPLE) {
            return generateCouple(expressionList);
        }
        throw new RuntimeException("Given operator is not implemented: " + node.getOperator());
    }

    public String generatePredicate(PredicateOperatorNode node, List<String> expressionList) {
        PredicateOperatorNode.PredicateOperator operator = node.getOperator();
        if(BINARY_PREDICATE_OPERATORS.contains(operator)) {
            return generateBinary(() -> operator, expressionList);
        } else if(UNARY_PREDICATE_OPERATORS.contains(operator)) {
            return generateUnaryPredicate(operator, expressionList);
        } else if (PREDICATE_BOOLEANS.contains(operator)) {
            return generateBoolean(operator);
        }
        throw new RuntimeException("Given operator is not implemented: " + node.getOperator());
    }


    private String generateUnaryExpression(ExpressionOperatorNode.ExpressionOperator operator, List<String> expressionList) {
        ST expression = generateUnary(operator);
        expression.add("obj", expressionList.get(0));
        expression.add("args", expressionList.subList(1, expressionList.size()));
        return expression.render();
    }

    private String generateUnaryPredicate(PredicateOperatorNode.PredicateOperator operator, List<String> expressionList) {
        ST expression = generateUnary(operator);
        expression.add("obj", expressionList.get(0));
        expression.add("args", expressionList.subList(1, expressionList.size()));
        return expression.render();
    }

    public String generateBinary(IOperator operator, List<String> expressionList) {
        Optional<String> result = expressionList.stream()
            .reduce((a, e) -> {
                Object op = operator.getOperator();
                ST expression = getTemplateFromBinaryOperator(op);
                if(expression == null) {
                    throw new RuntimeException("Given operator was not implemented: " + op);
                }
                if(BINARY_SWAP.contains(op)) {
                    expression.add("arg1", e);
                    expression.add("arg2", a);
                } else {
                    expression.add("arg1", a);
                    expression.add("arg2", e);
                }
                return expression.render();
            });
        return result.isPresent() ? result.get() : "";
    }

    private ST getTemplateFromBinaryOperator(Object op) {
        ST expression = null;
        if(op instanceof ExpressionOperatorNode.ExpressionOperator) {
            expression = generateBinary((ExpressionOperatorNode.ExpressionOperator) op);
        } else if(op instanceof PredicateOperatorNode.PredicateOperator) {
            expression = generateBinary((PredicateOperatorNode.PredicateOperator) op);
        } else if(op instanceof PredicateOperatorWithExprArgsNode.PredOperatorExprArgs) {
            expression = generateBinary((PredicateOperatorWithExprArgsNode.PredOperatorExprArgs) op);
        }
        return expression;
    }

    private ST generateUnary(ExpressionOperatorNode.ExpressionOperator operator) {
        ST template = group.getInstanceOf("unary");
        switch (operator) {
            case UNARY_MINUS:
                template.add("operator", "negative");
                break;
            case CARD:
                template.add("operator", "card");
                break;
            case FUNCTION_CALL:
                template.add("operator", "functionCall");
                break;
            case RELATIONAL_IMAGE:
                template.add("operator", "relationImage");
                break;
            default:
                throw new RuntimeException("Given operator is not implemented: " + operator);
        }
        return template;
    }

    private ST generateBinary(ExpressionOperatorNode.ExpressionOperator operator) {
        ST template = group.getInstanceOf("binary");
        switch(operator) {
            case PLUS:
                template.add("operator", "plus");
                break;
            case MINUS:
                template.add("operator", "minus");
                break;
            case MULT:
                template.add("operator", "multiply");
                break;
            case DIVIDE:
                template.add("operator", "divide");
                break;
            case MOD:
                template.add("operator", "modulo");
                break;
            case INTERSECTION:
                template.add("operator", "intersect");
                break;
            case UNION:
                template.add("operator", "union");
                break;
            case SET_SUBTRACTION:
                template.add("operator", "complement");
                break;
            default:
                throw new RuntimeException("Given operator is not implemented: " + operator);
        }
        return template;
    }

    private ST generateUnary(PredicateOperatorNode.PredicateOperator operator) {
        ST template = group.getInstanceOf("unary");
        switch(operator) {
            case NOT:
                template.add("operator", "not");
                break;
            default:
                throw new RuntimeException("Given node is not implemented: " + operator);
        }
        return template;
    }

    private ST generateBinary(PredicateOperatorNode.PredicateOperator operator) {
        ST template = group.getInstanceOf("binary");
        switch(operator) {
            case AND:
                template.add("operator", "and");
                break;
            case OR:
                template.add("operator", "or");
                break;
            case IMPLIES:
                template.add("operator", "implies");
                break;
            case EQUIVALENCE:
                template.add("operator", "equivalent");
                break;
            default:
                throw new RuntimeException("Given node is not implemented: " + operator);
        }
        return template;
    }


    private ST generateBinary(PredicateOperatorWithExprArgsNode.PredOperatorExprArgs operator) {
        ST template = group.getInstanceOf("binary");
        switch(operator) {
            case ELEMENT_OF:
                template.add("operator", "elementOf");
                break;
            case EQUAL:
                template.add("operator", "equal");
                break;
            case NOT_EQUAL:
                template.add("operator", "unequal");
                break;
            case LESS:
                template.add("operator", "less");
                break;
            case LESS_EQUAL:
                template.add("operator", "lessEqual");
                break;
            case GREATER:
                template.add("operator", "greater");
                break;
            case GREATER_EQUAL:
                template.add("operator", "greaterEqual");
                break;
            default:
                throw new RuntimeException("Given node is not implemented: " + operator);
        }
        return template;
    }

    private String generateInterval(List<String> arguments) {
        ST interval = group.getInstanceOf("interval");
        interval.add("arg1", arguments.get(0));
        interval.add("arg2", arguments.get(1));
        return interval.render();
    }

    private String generateCouple(List<String> arguments) {
        ST couple = group.getInstanceOf("couple_create");
        couple.add("arg1", arguments.get(0));
        couple.add("arg2", arguments.get(1));
        return couple.render();
    }

    private String generateSetEnumeration(List<String> expressions) {
        return group.getInstanceOf("set_enumeration").add("enums", expressions).render();
    }

    private String generateBoolean(ExpressionOperatorNode.ExpressionOperator operator) {
        return group.getInstanceOf("boolean_val").add("val", operator == TRUE).render();
    }

    private String generateBoolean(PredicateOperatorNode.PredicateOperator operator) {
        return group.getInstanceOf("boolean_val").add("val", operator == PredicateOperatorNode.PredicateOperator.TRUE).render();
    }

}
