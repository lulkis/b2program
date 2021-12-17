package de.hhu.stups.codegenerator.generators;

import de.hhu.stups.codegenerator.analyzers.IdentifierAnalyzer;
import de.hhu.stups.codegenerator.handlers.NameHandler;
import de.hhu.stups.codegenerator.handlers.TemplateHandler;
import de.hhu.stups.codegenerator.json.modelchecker.ModelCheckingInfo;
import de.hhu.stups.codegenerator.json.modelchecker.OperationFunctionInfo;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.OperationNode;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.types.BType;
import de.prob.parser.ast.types.DeferredSetElementType;
import de.prob.parser.ast.types.EnumeratedSetElementType;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.hhu.stups.codegenerator.handlers.NameHandler.IdentifierHandlingEnum.INCLUDED_MACHINES;

public class ModelCheckingInfoGenerator {

    private final STGroup currentGroup;

    private final NameHandler nameHandler;

    private final InvariantGenerator invariantGenerator;

    private final TransitionGenerator transitionGenerator;

    private final TypeGenerator typeGenerator;


    public ModelCheckingInfoGenerator(final STGroup currentGroup, final NameHandler nameHandler, final InvariantGenerator invariantGenerator,
                                      final TransitionGenerator transitionGenerator, final TypeGenerator typeGenerator) {
        this.currentGroup = currentGroup;
        this.nameHandler = nameHandler;
        this.invariantGenerator = invariantGenerator;
        this.transitionGenerator = transitionGenerator;
        this.typeGenerator = typeGenerator;
    }

    private List<String> generateVariables(MachineNode node) {
        return node.getVariables().stream()
                .map(variable -> "_get_" + nameHandler.handleIdentifier(variable.getName(), NameHandler.IdentifierHandlingEnum.FUNCTION_NAMES))
                .collect(Collectors.toList());
    }

    private Map<String, String> generateTransitionEvaluationFunctions(MachineNode node) {
        Map<String, String> transitionEvaluationFunctions = new HashMap<>();
        for(OperationNode operation : node.getOperations()) {
            String opName = nameHandler.handleIdentifier(operation.getName(), INCLUDED_MACHINES);
            String transitionName = "_tr_" + opName;
            transitionEvaluationFunctions.put(opName, transitionName);
        }
        return transitionEvaluationFunctions;
    }

    private List<OperationFunctionInfo> generateOperationFunctions(String machineName, MachineNode node) {
        List<OperationFunctionInfo> operationFunctions = new ArrayList<>();
        for(OperationNode operation : node.getOperations()) {
            String opName = nameHandler.handleIdentifier(operation.getName(), INCLUDED_MACHINES);
            List<String> parameterTypes = new ArrayList<>();
            for(DeclarationNode param : operation.getParams()) {
                BType type = param.getType();
                ST typeTemplate = currentGroup.getInstanceOf("mc_info_type");
                TemplateHandler.add(typeTemplate, "isSet", type instanceof EnumeratedSetElementType || type instanceof DeferredSetElementType);
                TemplateHandler.add(typeTemplate, "machine", machineName);
                TemplateHandler.add(typeTemplate, "type", typeGenerator.generate(type));
                parameterTypes.add(typeTemplate.render());
            }
            operationFunctions.add(new OperationFunctionInfo(opName, parameterTypes));
        }
        return operationFunctions;
    }

    private List<String> generateInvariantFunctions(MachineNode node) {
        List<String> invariantFunctions = new ArrayList<>();
        int invariantSize = invariantGenerator.splitInvariant(node.getInvariant()).size();
        for(int i = 1; i <= invariantSize; i++) {
            invariantFunctions.add("_check_inv_" + i);
        }
        return invariantFunctions;
    }

    public ModelCheckingInfo generateModelCheckingInfo(MachineNode node) {
        String machineName = nameHandler.handle(node.getName());
        List<String> variables = generateVariables(node);
        Map<String, String> transitionEvaluationFunctions = generateTransitionEvaluationFunctions(node);
        List<OperationFunctionInfo> operationFunctions = generateOperationFunctions(machineName, node);
        List<String> invariantFunctions = generateInvariantFunctions(node);

        Map<String, List<String>> writeInformation = generateWriteInformation(node.getOperations(), node.getVariables());
        Map<String, List<String>> invariantReads = generateInvariantReads(node.getInvariant(), node.getVariables());
        Map<String, List<String>> guardsReads = generateGuardsRead(node.getOperations(), node.getVariables());

        // TODO: Split guards conjuncts
        return new ModelCheckingInfo(machineName, variables, transitionEvaluationFunctions, operationFunctions, invariantFunctions,
                                    writeInformation, invariantReads, guardsReads);
    }

    public Map<String, List<String>> generateWriteInformation(List<OperationNode> operations, List<DeclarationNode> variables) {
        List<String> variablesAsString = variables.stream().map(DeclarationNode::toString).collect(Collectors.toList());
        Map<String, List<String>> writeInformation = new HashMap<>();
        for (OperationNode operation : operations) {
            IdentifierAnalyzer identifierAnalyzer = new IdentifierAnalyzer(IdentifierAnalyzer.Kind.WRITE);
            identifierAnalyzer.visitSubstitutionNode(operation.getSubstitution(), null);
            List<String> identifiers = identifierAnalyzer.getIdentifiers()
                    .stream()
                    .filter(variablesAsString::contains)
                    .collect(Collectors.toList());
            String opName = nameHandler.handle(operation.getName());
            writeInformation.put(opName, identifiers);
        }
        return writeInformation;
    }

    public Map<String, List<String>> generateInvariantReads(PredicateNode invariant, List<DeclarationNode> variables) {
        List<PredicateNode> invariantConjuncts = invariantGenerator.splitInvariant(invariant);
        List<String> variablesAsString = variables.stream().map(DeclarationNode::toString).collect(Collectors.toList());
        Map<String, List<String>> invariantReads = new HashMap<>();
        for(int i = 0; i < invariantConjuncts.size(); i++) {
            PredicateNode conj = invariantConjuncts.get(i);
            IdentifierAnalyzer identifierAnalyzer = new IdentifierAnalyzer(IdentifierAnalyzer.Kind.READ);
            identifierAnalyzer.visitPredicateNode(conj, null);
            List<String> identifiers = identifierAnalyzer.getIdentifiers()
                    .stream()
                    .filter(variablesAsString::contains)
                    .collect(Collectors.toList());
            invariantReads.put("_check_inv_" + (i + 1), identifiers);
        }
        return invariantReads;
    }

    public Map<String, List<String>> generateGuardsRead(List<OperationNode> operations, List<DeclarationNode> variables) {
        List<String> variablesAsString = variables.stream().map(DeclarationNode::toString).collect(Collectors.toList());
        Map<String, List<String>> writeInformation = new HashMap<>();
        for (OperationNode operation : operations) {
            IdentifierAnalyzer identifierAnalyzer = new IdentifierAnalyzer(IdentifierAnalyzer.Kind.READ);
            PredicateNode guard = transitionGenerator.extractGuard(operation);
            String opName = "_tr_" + nameHandler.handle(operation.getName());
            List<String> identifiers = new ArrayList<>();
            if(guard != null) {
                identifierAnalyzer.visitPredicateNode(guard, null);
                identifiers.addAll(identifierAnalyzer.getIdentifiers()
                        .stream()
                        .filter(variablesAsString::contains)
                        .collect(Collectors.toList()));
            }
            writeInformation.put(opName, identifiers);
        }
        return writeInformation;
    }


}
