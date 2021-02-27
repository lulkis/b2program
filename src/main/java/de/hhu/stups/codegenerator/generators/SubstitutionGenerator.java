package de.hhu.stups.codegenerator.generators;


import de.hhu.stups.codegenerator.analyzers.ParallelConstructAnalyzer;
import de.hhu.stups.codegenerator.handlers.IterationConstructHandler;
import de.hhu.stups.codegenerator.handlers.NameHandler;
import de.hhu.stups.codegenerator.handlers.ParallelConstructHandler;
import de.hhu.stups.codegenerator.handlers.TemplateHandler;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.OperationNode;
import de.prob.parser.ast.nodes.expression.ExprNode;
import de.prob.parser.ast.nodes.expression.ExpressionOperatorNode;
import de.prob.parser.ast.nodes.expression.IdentifierExprNode;
import de.prob.parser.ast.nodes.expression.LambdaNode;
import de.prob.parser.ast.nodes.expression.RecordFieldAccessNode;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.nodes.predicate.PredicateOperatorWithExprArgsNode;
import de.prob.parser.ast.nodes.substitution.AnySubstitutionNode;
import de.prob.parser.ast.nodes.substitution.AssignSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.BecomesElementOfSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.BecomesSuchThatSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.ChoiceSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.IfOrSelectSubstitutionsNode;
import de.prob.parser.ast.nodes.substitution.ListSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.OperationCallSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.SubstitutionNode;
import de.prob.parser.ast.nodes.substitution.VarSubstitutionNode;
import de.prob.parser.ast.nodes.substitution.WhileSubstitutionNode;
import de.prob.parser.ast.types.BType;
import de.prob.parser.ast.types.CoupleType;
import de.prob.parser.ast.types.SetType;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SubstitutionGenerator {

    private final STGroup currentGroup;

    private final MachineGenerator machineGenerator;

    private OperationGenerator operationGenerator;

    private final NameHandler nameHandler;

    private final TypeGenerator typeGenerator;

    private final ExpressionGenerator expressionGenerator;

    private final PredicateGenerator predicateGenerator;

    private final IdentifierGenerator identifierGenerator;

    private final IterationConstructHandler iterationConstructHandler;

    private final ParallelConstructHandler parallelConstructHandler;

    private final RecordStructGenerator recordStructGenerator;

    private final DeclarationGenerator declarationGenerator;

    private final LambdaFunctionGenerator lambdaFunctionGenerator;

    private final InfiniteSetGenerator infiniteSetGenerator;

    private int currentLocalScope;

    private int localScopes;

    private int parallelNestingLevel;

    private int recordCounter;

    public SubstitutionGenerator(final STGroup currentGroup, final MachineGenerator machineGenerator, final NameHandler nameHandler,
                                 final TypeGenerator typeGenerator, final ExpressionGenerator expressionGenerator, final PredicateGenerator predicateGenerator,
                                 final IdentifierGenerator identifierGenerator,
                                 final IterationConstructHandler iterationConstructHandler, final ParallelConstructHandler parallelConstructHandler,
                                 final RecordStructGenerator recordStructGenerator, final DeclarationGenerator declarationGenerator, final LambdaFunctionGenerator lambdaFunctionGenerator,
                                 final InfiniteSetGenerator infiniteSetGenerator) {
        this.currentGroup = currentGroup;
        this.machineGenerator = machineGenerator;
        this.nameHandler = nameHandler;
        this.typeGenerator = typeGenerator;
        this.predicateGenerator = predicateGenerator;
        this.expressionGenerator = expressionGenerator;
        this.expressionGenerator.setSubstitutionGenerator(this);
        this.identifierGenerator = identifierGenerator;
        this.iterationConstructHandler = iterationConstructHandler;
        this.parallelConstructHandler = parallelConstructHandler;
        this.recordStructGenerator = recordStructGenerator;
        this.declarationGenerator = declarationGenerator;
        this.lambdaFunctionGenerator = lambdaFunctionGenerator;
        this.infiniteSetGenerator = infiniteSetGenerator;
        this.currentLocalScope = 0;
        this.localScopes = 0;
        this.parallelNestingLevel = 0;
        this.recordCounter = 0;
    }

    /*
    * This function generates code for the initialization for the given AST node of the machine.
    */
    public String visitInitialization(MachineNode node) {
        String machineName = nameHandler.handle(node.getName());
        ST initialization = currentGroup.getInstanceOf("initialization");
        TemplateHandler.add(initialization, "machine", machineName);
        TemplateHandler.add(initialization, "machines", node.getMachineReferences().stream()
                .map(reference -> nameHandler.handle(reference.getMachineNode().getName()))
                .collect(Collectors.toList()));
        TemplateHandler.add(initialization, "includes", declarationGenerator.generateIncludes(node));
        TemplateHandler.add(initialization, "properties", generateConstantsInitializations(node));
        TemplateHandler.add(initialization, "values", generateValues(node));
        if(node.getInitialisation() != null) {
            TemplateHandler.add(initialization, "body", machineGenerator.visitSubstitutionNode(node.getInitialisation(), null));
        }
        return initialization.render();
    }

    /*
    * This function generates code for the VALUES clause from the given AST node of a machine
    */
    public String generateValues(MachineNode node) {
        if(node.getValues().size() == 0) {
            return "";
        }
        ST values = currentGroup.getInstanceOf("values");
        List<String> assignments = node.getValues().stream()
                .map(substitution -> machineGenerator.visitSubstitutionNode(substitution, null))
                .collect(Collectors.toList());
        TemplateHandler.add(values, "assignments", assignments);
        return values.render();
    }

    /*
    * This function generates code for initiailizing all constants from the given AST node of a machine
    */
    public List<String> generateConstantsInitializations(MachineNode node) {
        Set<String> lambdaFunctions = machineGenerator.getLambdaFunctions();
        List<String> constantsInitializations = node.getConstants().stream()
                .filter(constant -> !lambdaFunctions.contains(constant.getName()))
                .map(constant -> generateConstantInitialization(node, constant))
                .collect(Collectors.toList());
        constantsInitializations.addAll(node.getConstants().stream()
                .filter(constant -> declarationGenerator.getEnumToMachine().containsKey(constant.getType().toString()))
                .map(this::generateConstantFromDeferredSet)
                .collect(Collectors.toList()));
        return constantsInitializations;
    }

    /*
    * This function generates code for the initialization of a constant
    */
    private String generateConstantInitialization(MachineNode node, DeclarationNode constant) {
        ST initialization = currentGroup.getInstanceOf("constant_initialization");
        TemplateHandler.add(initialization, "identifier", nameHandler.handleIdentifier(constant.getName(), NameHandler.IdentifierHandlingEnum.FUNCTION_NAMES));
        List<PredicateNode> equalProperties = predicateGenerator.extractEqualProperties(node, constant);
        if(equalProperties.isEmpty()) {
            return "";
        }
        ExprNode expression = ((PredicateOperatorWithExprArgsNode) equalProperties.get(0)).getExpressionNodes().get(1);
        if(infiniteSetGenerator.checkExpressionInfinite(expression, PredicateOperatorWithExprArgsNode.PredOperatorExprArgs.EQUAL)) {
            return "";
        }

        if(expression instanceof LambdaNode && lambdaFunctionGenerator.checkPredicate((LambdaNode) expression)) {
            return "";
        }
        TemplateHandler.add(initialization, "iterationConstruct", iterationConstructHandler.inspectExpression(expression).getIterationsMapCode().values());
        TemplateHandler.add(initialization, "val", machineGenerator.visitExprNode(expression, null));
        return initialization.render();
    }

    private String generateConstantFromDeferredSet(DeclarationNode constant) {
        ST initialization = currentGroup.getInstanceOf("constant_initialization");
        TemplateHandler.add(initialization, "identifier", nameHandler.handleIdentifier(constant.getName(), NameHandler.IdentifierHandlingEnum.FUNCTION_NAMES));
        TemplateHandler.add(initialization, "val", declarationGenerator.callEnum(constant.getType().toString(), constant));
        return initialization.render();
    }

    /*
    * This function generates code for if substitutions and select substitutions from the belonging AST node.
    */
    public String visitIfOrSelectSubstitutionsNode(IfOrSelectSubstitutionsNode node) {
        if (node.getOperator() == IfOrSelectSubstitutionsNode.Operator.SELECT) {
            return visitSelectSubstitution(node);
        }
        return visitIfSubstitution(node);
    }

    /*
    * This function generates code for select substitutions from the belonging AST node and the belonging template.
    */
    private String visitSelectSubstitution(IfOrSelectSubstitutionsNode node) {
        ST select = currentGroup.getInstanceOf("select");
        TemplateHandler.add(select, "iterationConstruct", iterationConstructHandler.inspectPredicates(node.getConditions()).getIterationsMapCode().values());
        TemplateHandler.add(select, "predicate", machineGenerator.visitPredicateNode(node.getConditions().get(0), null));
        TemplateHandler.add(select, "then", machineGenerator.visitSubstitutionNode(node.getSubstitutions().get(0), null));
        return select.render();
    }

    /*
    * This function generates code for if substitutions with and without else-branches from the belonging AST node and the belonging template.
    */
    private String visitIfSubstitution(IfOrSelectSubstitutionsNode node) {
        ST ifST = currentGroup.getInstanceOf("if");
        TemplateHandler.add(ifST, "iterationConstruct", iterationConstructHandler.inspectPredicates(node.getConditions()).getIterationsMapCode().values());
        TemplateHandler.add(ifST, "predicate", machineGenerator.visitPredicateNode(node.getConditions().get(0), null));
        TemplateHandler.add(ifST, "then", machineGenerator.visitSubstitutionNode(node.getSubstitutions().get(0), null));
        TemplateHandler.add(ifST, "else1", generateElseIfs(node));
        if (node.getElseSubstitution() != null) {
            TemplateHandler.add(ifST, "else1", generateElse(node));
        }
        return ifST.render();
    }

    /*
    * This function generates code from the else if branches with the belonging AST node.
    */
    private List<String> generateElseIfs(IfOrSelectSubstitutionsNode node) {
        List<String> conditions = node.getConditions().subList(1, node.getConditions().size()).stream()
                .map(condition -> machineGenerator.visitPredicateNode(condition, null))
                .collect(Collectors.toList());
        List<String> then = node.getSubstitutions().subList(1, node.getSubstitutions().size()).stream()
                .map(substitutionNode -> machineGenerator.visitSubstitutionNode(substitutionNode, null))
                .collect(Collectors.toList());
        List<String> elseIfs = new ArrayList<>();

        for (int i = 0; i < conditions.size(); i++) {
            ST elseST = currentGroup.getInstanceOf("elseif");
            TemplateHandler.add(elseST, "predicate", conditions.get(i));
            TemplateHandler.add(elseST, "then", then.get(i));
            elseIfs.add(elseST.render());
        }

        return elseIfs;
    }

    /*
    * This function generates code from the else branch from the belonging AST node.
    */
    private String generateElse(IfOrSelectSubstitutionsNode node) {
        ST elseST = currentGroup.getInstanceOf("else");
        TemplateHandler.add(elseST, "then", machineGenerator.visitSubstitutionNode(node.getElseSubstitution(), null));
        return elseST.render();
    }


    /*
    * This function generates code for a choice substitution from the belonging AST node.
    */
    public String visitChoiceSubstitutionNode(ChoiceSubstitutionNode node, Void expected) {
        ST choice = currentGroup.getInstanceOf("choice");
        int length = node.getSubstitutions().size();
        List<SubstitutionNode> substitutions = node.getSubstitutions();
        TemplateHandler.add(choice, "len", length);
        TemplateHandler.add(choice, "then", machineGenerator.visitSubstitutionNode(substitutions.get(0), null));
        TemplateHandler.add(choice, "choice1", generateOtherChoices(node));
        if(substitutions.size() > 1) {
            ST choice2 = currentGroup.getInstanceOf("choice2");
            TemplateHandler.add(choice2, "then", machineGenerator.visitSubstitutionNode(substitutions.get(length - 1), expected));
            TemplateHandler.add(choice, "choice1", choice2.render());
        }
        return choice.render();
    }

    /*
    * This function generates code for the other choices in the choice substitution
    */
    private List<String> generateOtherChoices(ChoiceSubstitutionNode node) {
        List<String> otherChoices = new ArrayList<>();
        for (int i = 1; i < node.getSubstitutions().size() - 1; i++) {
            ST choice = currentGroup.getInstanceOf("choice1");
            TemplateHandler.add(choice, "counter", i);
            TemplateHandler.add(choice, "then", machineGenerator.visitSubstitutionNode(node.getSubstitutions().get(i), null));
            otherChoices.add(choice.render());
        }
        return otherChoices;
    }

    /*
    * This function generates code for a list of assignments from the belonging AST node.
    */
    public String visitAssignSubstitutionNode(AssignSubstitutionNode node) {
        List<SubstitutionNode> substitutions = new ArrayList<>();
        if(node.getLeftSide().size() == 1) {
            ExprNode lhs = node.getLeftSide().get(0);
            ExprNode rhs = node.getRightSide().get(0);
            return generateAssignment(lhs, rhs);
        }
        for (int i = 0; i < node.getLeftSide().size(); i++) {
            ExprNode lhs = node.getLeftSide().get(i);
            ExprNode rhs = node.getRightSide().get(i);
            substitutions.add(new AssignSubstitutionNode(node.getSourceCodePosition(), Collections.singletonList(lhs), Collections.singletonList(rhs)));
        }
        ListSubstitutionNode assignments = new ListSubstitutionNode(node.getSourceCodePosition(), ListSubstitutionNode.ListOperator.Parallel, substitutions);
        return visitParallelSubstitutionNode(assignments);
    }

    /*
    * This function generates code for one assignment with the expressions and the belonging template
    */
    public String generateAssignment(ExprNode lhs, ExprNode rhs) {
        ST substitution = currentGroup.getInstanceOf("assignment");
        generateAssignmentBody(substitution, lhs, rhs);
        if(lhs instanceof ExpressionOperatorNode && ((ExpressionOperatorNode) lhs).getOperator() == ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL) {
            TemplateHandler.add(substitution, "val", getNestedFunctionCall(lhs, rhs));
        } else if(lhs instanceof RecordFieldAccessNode) {
            TemplateHandler.add(substitution, "val", getNestedRecordAccess(lhs, rhs));
        } else {
            TemplateHandler.add(substitution, "val", machineGenerator.visitExprNode(rhs, null));
        }
        return substitution.render();
    }

    /*
    * This function generates code for the top-level assigned range element of a nested function call on the left-hand side of an assignment
    */
    private String getNestedFunctionCall(ExprNode lhs, ExprNode rhs) {
        List<ST> templates = new ArrayList<>();
        ExprNode innerExpression = lhs;
        List<ExprNode> arguments = new ArrayList<>();
        List<BType> leftTypes = new ArrayList<>();
        List<BType> rightTypes = new ArrayList<>();
        boolean isNested = false;

        while (innerExpression instanceof ExpressionOperatorNode && ((ExpressionOperatorNode) innerExpression).getOperator() == ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL) {
            List<ExprNode> expressions = ((ExpressionOperatorNode) innerExpression).getExpressionNodes();
            ExprNode innerArgument = expressionGenerator.getArgumentFromExpressions(expressions.subList(1, expressions.size()));
            leftTypes.add(innerArgument.getType());
            rightTypes.add(innerExpression.getType());
            arguments.add(innerArgument);
            innerExpression = ((ExpressionOperatorNode) innerExpression).getExpressionNodes().get(0);
            templates.add(generateNestedFunctionCall(innerExpression, innerArgument, rhs, isNested));
            isNested = true;
        }

        ST resultST = templates.get(0);

        for(int i = 0; i < templates.size() - 1; i++) {
            resultST = generateFunctionCallRangeElement(resultST, templates.get(i+1), leftTypes.get(i), rightTypes.get(i), arguments.get(i));
        }
        return resultST.render();
    }

    /*
    * This function calculates one step of calculating the final result for the top-level assigned range element of a nested function call on the left-hand side of an assignment
    */
    private ST generateFunctionCallRangeElement(ST currentTemplate, ST nextTemplate, BType leftType, BType rightType, ExprNode argument) {
        ST template = currentGroup.getInstanceOf("function_call_range_element");
        TemplateHandler.add(template, "expr", nextTemplate.render());
        TemplateHandler.add(template, "leftType", typeGenerator.generate(leftType));
        TemplateHandler.add(template, "rightType", typeGenerator.generate(rightType));
        TemplateHandler.add(template, "arg", machineGenerator.visitExprNode(argument, null));
        TemplateHandler.add(template, "val", currentTemplate.render());
        return template;
    }

    /*
    * This function calculates one inner function call that must be overridden by a relation where the inner construct contains the new range element that the nested function call is assigned to.
    */
    private ST generateNestedFunctionCall(ExprNode innerExpression, ExprNode innerArgument, ExprNode rhs, boolean isNested) {
        ST template = currentGroup.getInstanceOf("function_call_nested");
        if(isNested) {
            TemplateHandler.add(template, "expr", machineGenerator.visitExprNode(innerExpression, null));
        } else {
            TemplateHandler.add(template, "expr", machineGenerator.visitExprNode(rhs, null));
        }
        TemplateHandler.add(template, "arg", machineGenerator.visitExprNode(innerArgument, null));
        TemplateHandler.add(template, "isNested", isNested);
        return template;
    }

    /*
     * This function generates code for the top-level assigned element of a nested record access on the left-hand side of an assignment
     */
    private String getNestedRecordAccess(ExprNode lhs, ExprNode rhs) {
        List<ST> templates = new ArrayList<>();
        ExprNode innerExpression = lhs;
        List<DeclarationNode> arguments = new ArrayList<>();
        boolean isNested = false;

        while (innerExpression instanceof RecordFieldAccessNode) {
            DeclarationNode innerArgument = ((RecordFieldAccessNode) innerExpression).getIdentifier();
            arguments.add(innerArgument);
            innerExpression = ((RecordFieldAccessNode) innerExpression).getRecord();
            templates.add(generatedNestedRecordAccess(innerExpression, innerArgument, rhs, isNested));
            isNested = true;
        }

        ST resultST = templates.get(0);

        for(int i = 0; i < templates.size() - 1; i++) {
            resultST = generateRecordAccessElement(resultST, templates.get(i+1), arguments.get(i));
        }
        return resultST.render();
    }

    /*
     * This function calculates one step of calculating the final result for the top-level assigned element of a nested record access on the left-hand side of an assignment
     */
    private ST generateRecordAccessElement(ST currentTemplate, ST nextTemplate, DeclarationNode argument) {
        ST template = currentGroup.getInstanceOf("record_access_element");
        TemplateHandler.add(template, "expr", nextTemplate.render());
        TemplateHandler.add(template, "arg", nameHandler.handleIdentifier(argument.getName(), NameHandler.IdentifierHandlingEnum.FUNCTION_NAMES));
        TemplateHandler.add(template, "val", currentTemplate.render());
        return template;
    }

    /*
     * This function calculates one inner record access that must be overridden by the next inner construct with containing the new element that the nested record access is assigned to.
     */
    private ST generatedNestedRecordAccess(ExprNode innerExpression, DeclarationNode innerArgument, ExprNode rhs, boolean isNested) {
        ST template = currentGroup.getInstanceOf("record_access_nested");
        if(isNested) {
            TemplateHandler.add(template, "record", machineGenerator.visitExprNode(innerExpression, null));
        } else {
            TemplateHandler.add(template, "record", machineGenerator.visitExprNode(rhs, null));
        }
        TemplateHandler.add(template, "field", nameHandler.handleIdentifier(innerArgument.getName(), NameHandler.IdentifierHandlingEnum.FUNCTION_NAMES));
        TemplateHandler.add(template, "isNested", isNested);
        return template;
    }

    /*
     * This function generates code for the argument of an assignment
     */
    private void generateAssignmentArgument(ST substitution, ExprNode lhs) {
        if(lhs instanceof ExpressionOperatorNode) {
            ExprNode argument = getInnerArgumentOfFunctionCall(lhs);
            IdentifierExprNode identifier = getIdentifierOnLhs(lhs);

            BType rightType = ((CoupleType)((SetType) identifier.getType()).getSubType()).getRight();

            TemplateHandler.add(substitution, "arg", machineGenerator.visitExprNode(argument, null));
            TemplateHandler.add(substitution, "leftType", typeGenerator.generate(argument.getType()));
            TemplateHandler.add(substitution, "rightType", typeGenerator.generate(rightType));
        } else if(lhs instanceof RecordFieldAccessNode) {
            DeclarationNode argument = getInnerArgumentOfRecordAccess(lhs);
            TemplateHandler.add(substitution, "arg", nameHandler.handleIdentifier(argument.getName(), NameHandler.IdentifierHandlingEnum.FUNCTION_NAMES));
        }
    }

    /*
     * This function generates code for the body of an assignment
     */
    private void generateAssignmentBody(ST substitution, ExprNode lhs, ExprNode rhs) {
        TemplateHandler.add(substitution, "iterationConstruct", iterationConstructHandler.inspectExpression(rhs).getIterationsMapCode().values());
        TemplateHandler.add(substitution, "machine", machineGenerator.getMachineName());
        parallelConstructHandler.setLhsInParallel(true);
        IdentifierExprNode identifier = getIdentifierOnLhs(lhs);
        boolean isIdentifierLhs = lhs instanceof IdentifierExprNode;
        boolean isRecordLhs = lhs instanceof RecordFieldAccessNode;
        parallelConstructHandler.setLhsInParallel(true);
        generateAssignmentArgument(substitution, lhs);
        TemplateHandler.add(substitution, "isIdentifierLhs", isIdentifierLhs);
        TemplateHandler.add(substitution, "isRecordAccessLhs", isRecordLhs);
        TemplateHandler.add(substitution, "identifier", machineGenerator.visitExprNode(identifier, null));
        TemplateHandler.add(substitution, "isPrivate", nameHandler.getGlobals().contains(identifier.getName()));
        parallelConstructHandler.setLhsInParallel(false);
        TemplateHandler.add(substitution, "modified_identifier", machineGenerator.visitExprNode(identifier, null));
    }

    /*
    * This function generates code for sequential substitution from the belonging AST node
    */
    public String visitListSubstitutionNode(ListSubstitutionNode node) {
        if(node.getOperator() == ListSubstitutionNode.ListOperator.Parallel) {
            return visitParallelSubstitutionNode(node);
        }
        return visitSequentialSubstitutionNode(node);
    }

    /*
    * This function generates code for a parallel substituion from the belonging AST node
    */
    private String visitParallelSubstitutionNode(ListSubstitutionNode node) {
        parallelNestingLevel++;
        ST substitutions = currentGroup.getInstanceOf("parallel");
        TemplateHandler.add(substitutions, "machine", nameHandler.handle(machineGenerator.getMachineName()));
        ParallelConstructAnalyzer parallelConstructAnalyzer = getParallelConstructAnalyzer();
        generateParallelLoads(substitutions, parallelConstructAnalyzer, node);
        generateParallelSubstitutions(substitutions, parallelConstructAnalyzer, node);
        parallelNestingLevel--;
        resetParallelConstructAnalyzer();
        return substitutions.render();
    }

    /*
    * This function updates the current ParallelConstructAnalyzer
    */
    private ParallelConstructAnalyzer getParallelConstructAnalyzer() {
        ParallelConstructAnalyzer parallelConstructAnalyzer;
        if(parallelConstructHandler.getParallelConstructAnalyzer() == null) {
            parallelConstructAnalyzer = new ParallelConstructAnalyzer(identifierGenerator);
            parallelConstructHandler.setParallelConstructAnalyzer(parallelConstructAnalyzer);
        } else {
            parallelConstructAnalyzer = new ParallelConstructAnalyzer(identifierGenerator);
            parallelConstructAnalyzer.getDefinedIdentifiersInParallel().addAll(parallelConstructHandler.getParallelConstructAnalyzer().getDefinedIdentifiersInParallel());
            parallelConstructAnalyzer.definedIdentifiersInCode().addAll(parallelConstructHandler.getParallelConstructAnalyzer().definedIdentifiersInCode());
            parallelConstructHandler.setParallelConstructAnalyzer(parallelConstructAnalyzer);
        }
        return parallelConstructAnalyzer;
    }

    /*
    * This function generates code for all necessary loads of variables before generating code for a parallel substitution
    */
    private void generateParallelLoads(ST substitutions, ParallelConstructAnalyzer parallelConstructAnalyzer, ListSubstitutionNode node) {
        parallelConstructAnalyzer.visitSubstitutionNode(node, null);
        parallelConstructHandler.setLhsInParallel(true);
        Set<String> loads = parallelConstructAnalyzer.getDefinedLoadsInParallel().stream()
                .map(this::visitParallelLoad)
                .collect(Collectors.toSet());
        TemplateHandler.add(substitutions, "loads", loads);
        parallelConstructHandler.setLhsInParallel(false);
        parallelConstructAnalyzer.resetParallel();
    }

    /*
    * This function generates code for all assignments of variables within a parallel substitution
    */
    private void generateParallelSubstitutions(ST substitutions, ParallelConstructAnalyzer parallelConstructAnalyzer, ListSubstitutionNode node) {
        List<String> others = node.getSubstitutions().stream()
                .map(subs -> machineGenerator.visitSubstitutionNode(subs, null))
                .collect(Collectors.toList());
        parallelConstructHandler.setParallelConstructAnalyzer(parallelConstructAnalyzer);
        TemplateHandler.add(substitutions, "others", others);
    }

    /*
    * This function resets the nesting level of the ParallelConstructHandler
    */
    private void resetParallelConstructAnalyzer() {
        if(parallelNestingLevel == 0) {
            parallelConstructHandler.setParallelConstructAnalyzer(null);
        }
    }

    /*
    * This function generates code for a parallel load from the given AST node
    */
    private String visitParallelLoad(ExprNode expr) {
        String identifier = machineGenerator.visitExprNode(expr, null);
        List<String> identifiersInCode = parallelConstructHandler.getParallelConstructAnalyzer().definedIdentifiersInCode();
        if(identifiersInCode.contains(identifier)) {
            return "";
        } else {
            identifiersInCode.add(identifier);
        }
        ST substitution = currentGroup.getInstanceOf("parallel_load");
        TemplateHandler.add(substitution, "machine", machineGenerator.getMachineName());
        TemplateHandler.add(substitution, "type", typeGenerator.generate(expr.getType()));
        TemplateHandler.add(substitution, "identifier", identifier);
        TemplateHandler.add(substitution, "name", expr.toString());
        TemplateHandler.add(substitution, "isPrivate", nameHandler.getGlobals().contains(((IdentifierExprNode) expr).getName()));
        return substitution.render();
    }

    /*
    * This function generates code for a sequential substitution with the belonging AST node.
    */
    private String visitSequentialSubstitutionNode(ListSubstitutionNode node) {
        List<String> substitutionCodes = node.getSubstitutions().stream()
                .map(substitutionNode -> machineGenerator.visitSubstitutionNode(substitutionNode, null))
                .collect(Collectors.toList());
        return String.join("\n", substitutionCodes);
    }

    /*
    * This function generates code for a becomes element of substitution with the belonging AST node
    */
    public String visitBecomesElementOfSubstitutionNode(BecomesElementOfSubstitutionNode node) {
        ST substitutions = currentGroup.getInstanceOf("assignments");
        List<String> assignments = new ArrayList<>();
        for (int i = 0; i < node.getIdentifiers().size(); i++) {
            assignments.add(generateNondeterminism(node.getIdentifiers().get(i), node.getExpression()));
        }
        TemplateHandler.add(substitutions, "assignments", assignments);
        return substitutions.render();
    }

    /*
    * This function generates code for a nondeterministic assignment
    */
    private String generateNondeterminism(ExprNode lhs, ExprNode rhs) {
        ST substitution = currentGroup.getInstanceOf("nondeterminism");
        generateAssignmentBody(substitution, lhs, rhs);
        if(lhs instanceof ExpressionOperatorNode && ((ExpressionOperatorNode) lhs).getOperator() == ExpressionOperatorNode.ExpressionOperator.FUNCTION_CALL) {
            TemplateHandler.add(substitution, "set", getNestedFunctionCall(lhs, rhs));
        } else if(lhs instanceof RecordFieldAccessNode) {
            TemplateHandler.add(substitution, "set", getNestedRecordAccess(lhs, rhs));
        } else {
            TemplateHandler.add(substitution, "set", machineGenerator.visitExprNode(rhs, null));
        }

        return substitution.render();
    }

    /*
    * This function extracts the changed identifier from the left-hand side of an assignment
    */
    private IdentifierExprNode getIdentifierOnLhs(ExprNode lhs) {
        ExprNode expression = null;
        if(lhs instanceof IdentifierExprNode) {
            return (IdentifierExprNode) lhs;
        } else if(lhs instanceof ExpressionOperatorNode) {
            expression = ((ExpressionOperatorNode) lhs).getExpressionNodes().get(0);
        } else if(lhs instanceof RecordFieldAccessNode) {
            expression = ((RecordFieldAccessNode) lhs).getRecord();
        }
        if(expression instanceof IdentifierExprNode) {
            return (IdentifierExprNode) expression;
        } else {
            return getIdentifierOnLhs(expression);
        }
    }

    /*
    * This function extracts the inner argument from a nested function call on the left-hand side of an assignment
    */
    private ExprNode getInnerArgumentOfFunctionCall(ExprNode lhs) {
        ExprNode expression = null;
        if(lhs instanceof ExpressionOperatorNode) {
            expression = ((ExpressionOperatorNode) lhs).getExpressionNodes().get(0);
            if(expression instanceof IdentifierExprNode) {
                List<ExprNode> expressions = ((ExpressionOperatorNode) lhs).getExpressionNodes();
                return expressionGenerator.getArgumentFromExpressions(expressions.subList(1, expressions.size()));
            }
        }
        return getInnerArgumentOfFunctionCall(expression);
    }

    /*
     * This function extracts the inner argument from a nested record access on the left-hand side of an assignment
     */
    private DeclarationNode getInnerArgumentOfRecordAccess(ExprNode lhs) {
        ExprNode expression = null;
        if(lhs instanceof RecordFieldAccessNode) {
            expression = ((RecordFieldAccessNode) lhs).getRecord();
            if(expression instanceof IdentifierExprNode) {
                return ((RecordFieldAccessNode) lhs).getIdentifier();
            }
        }
        return getInnerArgumentOfRecordAccess(expression);
    }

    /*
    * This function generates code for an operation call substitution from the belonging AST node
    */
    public String visitSubstitutionIdentifierCallNode(OperationCallSubstitutionNode node, Void expected) {
        List<String> variables = node.getAssignedVariables().stream()
                .map(var -> machineGenerator.visitExprNode(var, expected))
                .collect(Collectors.toList());
        String operationName = node.getOperationNode().getName();
        String machineName;
        if (node.getNames().size() > 1) {
            List<String> prefixStrings = node.getNames().subList(0, node.getNames().size() - 1);
            machineName = String.join(".", prefixStrings);
        } else {
            machineName = operationGenerator.getMachineFromOperation().get(operationName);
        }
        ST operationCall = getOperationCallTemplate(node, variables);
        TemplateHandler.add(operationCall, "thisName", machineGenerator.getMachineName());
        TemplateHandler.add(operationCall, "machine", nameHandler.handle(machineName));
        TemplateHandler.add(operationCall, "machineInstance", nameHandler.handleIdentifier(machineName, NameHandler.IdentifierHandlingEnum.MACHINES));
        TemplateHandler.add(operationCall, "function", nameHandler.handleIdentifier(operationName, NameHandler.IdentifierHandlingEnum.INCLUDED_MACHINES));
        TemplateHandler.add(operationCall, "args", node.getArguments().stream()
                .map(expr -> machineGenerator.visitExprNode(expr, expected))
                .collect(Collectors.toList()));
        TemplateHandler.add(operationCall,"this", machineName.equals(machineGenerator.getMachineName()));
        return operationCall.render();
    }

    /*
    * This function gets the needed template for an operation call. It dependes on the size of the return parameters.
    */
    private ST getOperationCallTemplate(OperationCallSubstitutionNode node, List<String> variables) {
        ST operationCall = null;
        if(variables.size() > 1) {
            operationCall = getOperationCallTemplateWithManyParameters(node, variables);
        } else if(variables.size() == 1) {
            operationCall = getOperationCallTemplateWithOneParameter(variables.get(0));
        } else {
            operationCall = getOperationCallTemplateWithoutAssignment();
        }
        return operationCall;
    }

    /*
    * This function returns the template for an operation call without assignment
    */
    private ST getOperationCallTemplateWithoutAssignment() {
        return currentGroup.getInstanceOf("operation_call_without_assignment");
    }

    /*
    * This function returns the template for an operation call with assignment with one parameter
    */
    private ST getOperationCallTemplateWithOneParameter(String variable) {
        ST operationCall = currentGroup.getInstanceOf("operation_call_with_assignment_one_parameter");
        TemplateHandler.add(operationCall, "var", variable);
        TemplateHandler.add(operationCall, "isPrivate", nameHandler.getGlobals().contains(variable));
        return operationCall;
    }

    /*
    * This function returns the template for an operation call with assignment with many parameters
    */
    private ST getOperationCallTemplateWithManyParameters(OperationCallSubstitutionNode node, List<String> variables) {
        OperationNode operationNode = node.getOperationNode();
        ST operationCall = currentGroup.getInstanceOf("operation_call_with_assignment_many_parameters");
        TemplateHandler.add(operationCall, "struct", recordStructGenerator.getStruct(operationNode));
        TemplateHandler.add(operationCall, "var", "_ld_record_" + recordCounter);
        List<String> assignments = new ArrayList<>();
        for(int i = 0; i < variables.size(); i++) {
            ST assignment = currentGroup.getInstanceOf("operation_call_assignment");
            TemplateHandler.add(assignment, "var", variables.get(i));
            TemplateHandler.add(assignment, "record", "_ld_record_" + recordCounter);
            TemplateHandler.add(assignment, "field", operationNode.getOutputParams().get(i).getName());
            assignments.add(assignment.render());
        }
        recordCounter++;
        TemplateHandler.add(operationCall, "assignments", assignments);
        return operationCall;
    }

    /*
    * This function generates code for a while loop with the belonging AST node and the belonging template.
    */
    public String visitWhileSubstitutionNode(WhileSubstitutionNode node, Void expected) {
        ST whileST = currentGroup.getInstanceOf("while");
        TemplateHandler.add(whileST, "iterationConstruct1", iterationConstructHandler.inspectPredicate(node.getCondition()).getIterationsMapCode().values());
        TemplateHandler.add(whileST, "predicate", machineGenerator.visitPredicateNode(node.getCondition(), expected));
        TemplateHandler.add(whileST, "then", machineGenerator.visitSubstitutionNode(node.getBody(), expected));
        TemplateHandler.add(whileST, "iterationConstruct2", iterationConstructHandler.inspectPredicate(node.getCondition()).getIterationsMapCode().values());
        return whileST.render();
    }

    /*
    * This function generates from a var substitution with the belonging AST node and template. During this step the
    * flag for local scope is set to true and finally resetted to false. This is needed for handling collisions between
    * local variables and output parameters.
    */
    public String visitVarSubstitutionNode(VarSubstitutionNode node, Void expected) {
        ST varST = currentGroup.getInstanceOf("var");
        this.localScopes++;
        this.currentLocalScope++;
        identifierGenerator.push(localScopes);
        node.getLocalIdentifiers().forEach(identifier -> identifierGenerator.addLocal(identifier.getName()));
        TemplateHandler.add(varST, "machine", nameHandler.handle(machineGenerator.getMachineName()));
        TemplateHandler.add(varST, "locals", generateVariablesInVar(node.getLocalIdentifiers()));
        TemplateHandler.add(varST, "body", machineGenerator.visitSubstitutionNode(node.getBody(), expected));
        identifierGenerator.pop();
        node.getLocalIdentifiers().forEach(identifier -> identifierGenerator.resetLocal(identifier.getName()));
        this.currentLocalScope--;
        return varST.render();
    }

    /*
    * This function generates code for all declarations of local variables from a var substitution from the list of identifiers as AST nodes.
    */
    private List<String> generateVariablesInVar(List<DeclarationNode> identifiers) {
        return identifiers.stream()
                .map(this::generateVariableInVar)
                .collect(Collectors.toList());
    }

    /*
    * This function generates code for one declaration of a local variable from a var substitution node from the belonging AST node and template.
    */
    private String generateVariableInVar(DeclarationNode identifier) {
        ST declaration = currentGroup.getInstanceOf("local_declaration");
        TemplateHandler.add(declaration, "type", typeGenerator.generate(identifier.getType()));
        TemplateHandler.add(declaration, "identifier", identifierGenerator.generateVarDeclaration(identifier.getName(), true));
        return declaration.render();
    }

    /*
    * This function generates code for an ANY substitution from the belonging AST node
    */
    public String visitAnySubstitutionNode(AnySubstitutionNode node) {
        iterationConstructHandler.inspectSubstitution(node);
        return iterationConstructHandler.getIterationsMapCode().get(node.toString());
    }

    /*
    * This function generates code for a becomes such that substitution from the belonging AST node
    */
    public String visitBecomesSuchThatSubstitutionNode(BecomesSuchThatSubstitutionNode node) {
        iterationConstructHandler.inspectSubstitution(node);
        return iterationConstructHandler.getIterationsMapCode().get(node.toString());
    }

    public int getCurrentLocalScope() {
        return currentLocalScope;
    }

    public void setOperationGenerator(OperationGenerator operationGenerator) {
        this.operationGenerator = operationGenerator;
    }
}
