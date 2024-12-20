package de.hhu.stups.codegenerator.generators.iteration;

import de.hhu.stups.codegenerator.GeneratorMode;
import de.hhu.stups.codegenerator.generators.MachineGenerator;
import de.hhu.stups.codegenerator.generators.TypeGenerator;
import de.hhu.stups.codegenerator.handlers.IterationConstructHandler;
import de.hhu.stups.codegenerator.handlers.TemplateHandler;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.expression.ExprNode;
import de.prob.parser.ast.nodes.expression.LambdaNode;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.nodes.predicate.PredicateOperatorNode;
import de.prob.parser.ast.types.BType;
import de.prob.parser.ast.types.CoupleType;
import de.prob.parser.ast.types.SetType;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by fabian on 04.03.19.
 */
public class LambdaGenerator {

    private final STGroup group;

    private final MachineGenerator machineGenerator;

    private final IterationConstructGenerator iterationConstructGenerator;

    private final IterationConstructHandler iterationConstructHandler;

    private final IterationPredicateGenerator iterationPredicateGenerator;

    private final TypeGenerator typeGenerator;

    public LambdaGenerator(final STGroup group, final MachineGenerator machineGenerator, final IterationConstructGenerator iterationConstructGenerator,
                                     final IterationConstructHandler iterationConstructHandler, final IterationPredicateGenerator iterationPredicateGenerator,
                                     final TypeGenerator typeGenerator) {
        this.group = group;
        this.machineGenerator = machineGenerator;
        this.iterationConstructGenerator = iterationConstructGenerator;
        this.iterationConstructHandler = iterationConstructHandler;
        this.iterationPredicateGenerator = iterationPredicateGenerator;
        this.typeGenerator = typeGenerator;
    }

    /*
    * This function generates code for a lambda expression from the belonging AST node
    */
    public String generateLambda(PredicateNode conditionalPredicate, LambdaNode node) {
        machineGenerator.inIterationConstruct(node.getDeclarations());
        PredicateNode predicate = node.getPredicate();
        List<DeclarationNode> declarations = node.getDeclarations();
        ExprNode expression = node.getExpression();
        BType type = node.getType();

        ST template = group.getInstanceOf("lambda");

        List<ST> enumerationTemplates = iterationPredicateGenerator.getEnumerationTemplates(iterationConstructGenerator, declarations, predicate, false);
        Collection<String> otherConstructs = generateOtherIterationConstructs(predicate, expression);

        iterationConstructGenerator.prepareGeneration(predicate, declarations, type, false);

        int iterationConstructCounter = iterationConstructHandler.getIterationConstructCounter();
        String identifier = "_ic_set_" + iterationConstructCounter;

        CoupleType coupleType = (CoupleType) ((SetType) node.getType()).getSubType();

        generateBody(template, enumerationTemplates, otherConstructs, identifier, conditionalPredicate, predicate, coupleType, declarations, expression, type);

        String result = template.render();
        iterationConstructGenerator.addGeneration(node.toString(), identifier, declarations, result);
        machineGenerator.leaveIterationConstruct(node.getDeclarations());
        return result;
    }

    /*
    * This function generates code for other iteration constructs used within the lambda expression
    */
    private Collection<String> generateOtherIterationConstructs(PredicateNode predicate, ExprNode expression) {
        IterationConstructGenerator otherConstructsGenerator = iterationConstructHandler.getNewIterationConstructGenerator();
        otherConstructsGenerator.getAllBoundedVariables().addAll(iterationConstructGenerator.getAllBoundedVariables());
        for (String key : iterationConstructGenerator.getIterationsMapIdentifier().keySet()) {
            otherConstructsGenerator.getIterationsMapIdentifier().put(key, iterationConstructGenerator.getIterationsMapIdentifier().get(key));
        }
        iterationConstructHandler.inspectExpression(iterationConstructHandler.inspectPredicate(otherConstructsGenerator, predicate), expression);
        for (String key : otherConstructsGenerator.getIterationsMapIdentifier().keySet()) {
            iterationConstructGenerator.getIterationsMapIdentifier().put(key, otherConstructsGenerator.getIterationsMapIdentifier().get(key));
        }
        return otherConstructsGenerator.getIterationsMapCode().values();
    }

    /*
    * This function generates code for the inner body of the lambda expression
    */
    private void generateBody(ST template, List<ST> enumerationTemplates, Collection<String> otherConstructs, String identifier, PredicateNode conditionalPredicate, PredicateNode predicate, CoupleType coupleType, List<DeclarationNode> declarations, ExprNode expression, BType type) {
        iterationConstructHandler.setIterationConstructGenerator(iterationConstructGenerator);
        String lhsExpr;
        if(coupleType.getLeft() instanceof CoupleType) {
            lhsExpr = machineGenerator.getExpressionGenerator().generateTuple(declarations
                    .stream()
                    .map(dec -> "_ic_" + dec.getName() + "_" + machineGenerator.getBoundedVariablesDepth().get(dec.getName()))
                    .collect(Collectors.toList()), ((CoupleType) coupleType.getLeft()).getLeft(), ((CoupleType) coupleType.getLeft()).getRight());
        } else {
            String name = declarations.get(declarations.size() - 1).getName();
            lhsExpr = "_ic_" + name + "_" + machineGenerator.getBoundedVariablesDepth().get(name);
        }
        String leftType = typeGenerator.generate(coupleType.getLeft());
        String rightType = typeGenerator.generate(coupleType.getRight());


        String innerBody = generateLambdaExpression(otherConstructs, conditionalPredicate, predicate, leftType, rightType, expression, identifier, lhsExpr, declarations);
        String lambda = iterationPredicateGenerator.evaluateEnumerationTemplates(enumerationTemplates, innerBody, conditionalPredicate).render();
        TemplateHandler.add(template, "isJavaScript", machineGenerator.getMode() == GeneratorMode.JS);
        TemplateHandler.add(template, "type", typeGenerator.generate(type));
        TemplateHandler.add(template, "identifier", identifier);
        TemplateHandler.add(template, "leftType", leftType);
        TemplateHandler.add(template, "rightType", rightType);
        TemplateHandler.add(template, "lambda", lambda);
    }

    /*
    * This function generates code for the expression representing the values the variables are mapped to within the lambda expression
    */
    public String generateLambdaExpression(Collection<String> otherConstructs, PredicateNode conditionalPredicate, PredicateNode predicateNode, String leftType, String rightType, ExprNode expression, String relationName, String elementName, List<DeclarationNode> declarations) {
        PredicateNode subpredicate = iterationPredicateGenerator.subpredicate(predicateNode, declarations, false);
        ST template = group.getInstanceOf("lambda_expression");
        TemplateHandler.add(template, "otherIterationConstructs", otherConstructs);
        TemplateHandler.add(template, "emptyPredicate", subpredicate == null || ((PredicateOperatorNode) subpredicate).getPredicateArguments().size() == 0);
        TemplateHandler.add(template, "hasCondition", conditionalPredicate != null);
        if(conditionalPredicate != null) {
            TemplateHandler.add(template, "conditionalPredicate", machineGenerator.visitPredicateNode(conditionalPredicate, null));
        }
        if(subpredicate != null) {
            TemplateHandler.add(template, "predicate", machineGenerator.visitPredicateNode(subpredicate, null));
        }
        TemplateHandler.add(template, "leftType", leftType);
        TemplateHandler.add(template, "rightType", rightType);
        TemplateHandler.add(template, "relation", relationName);
        TemplateHandler.add(template, "element", elementName);
        TemplateHandler.add(template, "expression", machineGenerator.visitExprNode(expression, null));
        return template.render();
    }

}
