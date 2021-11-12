package de.hhu.stups.codegenerator.generators;

import de.hhu.stups.codegenerator.handlers.NameHandler;
import de.hhu.stups.codegenerator.handlers.TemplateHandler;
import de.hhu.stups.codegenerator.json.modelchecker.ModelCheckingInfo;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.OperationNode;
import de.prob.parser.ast.types.BType;
import de.prob.parser.ast.types.CoupleType;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelCheckingGenerator {

    private ModelCheckingInfo modelCheckingInfo;

    private final STGroup currentGroup;

    private final NameHandler nameHandler;

    private final TypeGenerator typeGenerator;

    public ModelCheckingGenerator(final STGroup currentGroup, final NameHandler nameHandler,
                                  final TypeGenerator typeGenerator) {
        this.currentGroup = currentGroup;
        this.nameHandler = nameHandler;
        this.typeGenerator = typeGenerator;
    }

    public String generate(MachineNode machineNode, boolean forModelChecking, boolean isIncludedMachine, boolean forVisualisation) {
        if((forModelChecking || forVisualisation) && !isIncludedMachine) {
            typeGenerator.setFromOutside(true);
            ST template = currentGroup.getInstanceOf("model_check");
            TemplateHandler.add(template, "nextStates", generateNextStates(machineNode));
            TemplateHandler.add(template, "evalState", generateEvalState(machineNode));
            TemplateHandler.add(template, "printResult", generatePrintResult());
            TemplateHandler.add(template, "main", generateMain(machineNode));
            typeGenerator.setFromOutside(false);
            return template.render();
        }
        return "";
    }

    public String generateNextStates(MachineNode machineNode) {
        ST template = currentGroup.getInstanceOf("model_check_next_states");
        TemplateHandler.add(template, "machine", nameHandler.handle(machineNode.getName()));
        TemplateHandler.add(template, "transitions", generateTransitions(machineNode));
        return template.render();
    }

    public List<String> generateTransitions(MachineNode machineNode) {
        List<String> transitions = new ArrayList<>();
        List<OperationNode> operations = machineNode.getOperations();
        for(int i = 0; i < operations.size(); i++) {
            transitions.add(generateTransition(machineNode, operations.get(i), i+1));
        }
        return transitions;
    }

    public String generateTransition(MachineNode machineNode, OperationNode operationNode, int index) {
        ST template = currentGroup.getInstanceOf("model_check_transition");
        String opName = nameHandler.handle(operationNode.getName());
        boolean hasParameters = !operationNode.getParams().isEmpty();
        TemplateHandler.add(template, "hasParameters", hasParameters);
        BType tupleType;
        if(!hasParameters) {
            tupleType = null;
        } else {
            tupleType = this.extractTypeFromDeclarations(operationNode.getParams());
        }
        if(hasParameters) {
            TemplateHandler.add(template, "tupleType", typeGenerator.generate(tupleType));
            TemplateHandler.add(template, "transitionIdentifier", "_trid_" + index);
        }

        TemplateHandler.add(template, "evalTransitions", modelCheckingInfo.getTransitionEvaluationFunctions().get(opName));
        TemplateHandler.add(template, "execTransitions", generateTransitionBody(machineNode, operationNode, tupleType, index));
        return template.render();
    }

    public String generateTransitionBody(MachineNode machineNode, OperationNode opNode, BType tupleType, int index) {
        ST template = currentGroup.getInstanceOf("model_check_transition_body");
        boolean hasParameters = !opNode.getParams().isEmpty();
        TemplateHandler.add(template, "machine", nameHandler.handle(machineNode.getName()));
        TemplateHandler.add(template, "operation", nameHandler.handle(opNode.getName()));
        TemplateHandler.add(template, "hasParameters", hasParameters);
        List<String> readParameters = new ArrayList<>();
        List<String> parameters = new ArrayList<>();

        if(hasParameters) {

            BType currentType = tupleType;

            List<DeclarationNode> declarationParams = opNode.getParams();

            if (declarationParams.size() == 1) {
                ST paramTemplateLhs = currentGroup.getInstanceOf("model_check_transition_param_assignment");
                TemplateHandler.add(paramTemplateLhs, "type", typeGenerator.generate(currentType));
                TemplateHandler.add(paramTemplateLhs, "param", "_tmp_" + 1);
                TemplateHandler.add(paramTemplateLhs, "val", "param");
                TemplateHandler.add(paramTemplateLhs, "isLhs", false);
                TemplateHandler.add(paramTemplateLhs, "oneParameter", true);
                String lhsParameter = paramTemplateLhs.render();
                readParameters.add(lhsParameter);
                parameters.add("_tmp_" + 1);
            } else {
                int j = 1;
                for (int i = 0; i < declarationParams.size(); i++) {
                    DeclarationNode paramNode = opNode.getParams().get(i);


                    if (i < opNode.getParams().size() - 1) {
                        // Access rhs were it is not the left-most parameter

                        ST paramTemplateLhs = currentGroup.getInstanceOf("model_check_transition_param_assignment");
                        TemplateHandler.add(paramTemplateLhs, "type", typeGenerator.generate(paramNode.getType()));
                        TemplateHandler.add(paramTemplateLhs, "param", "_tmp_" + j);
                        TemplateHandler.add(paramTemplateLhs, "val", j == 1 ? "param" : "_tmp_" + (j - 1));
                        TemplateHandler.add(paramTemplateLhs, "isLhs", false);
                        TemplateHandler.add(paramTemplateLhs, "oneParameter", false);
                        String lhsParameter = paramTemplateLhs.render();
                        readParameters.add(lhsParameter);
                        parameters.add(0, "_tmp_" + j);

                        j++;



                        if(i < opNode.getParams().size() - 2) {
                            // Store temporary tuples im necessary
                            currentType = ((CoupleType) currentType).getLeft();
                            ST paramTemplateRhs = currentGroup.getInstanceOf("model_check_transition_param_assignment");
                            TemplateHandler.add(paramTemplateRhs, "type", typeGenerator.generate(currentType));
                            TemplateHandler.add(paramTemplateRhs, "param", "_tmp_" + j);
                            TemplateHandler.add(paramTemplateRhs, "val", j == 2 ? "param" : "_tmp_" + (j - 2));
                            TemplateHandler.add(paramTemplateRhs, "isLhs", true);
                            TemplateHandler.add(paramTemplateRhs, "oneParameter", false);
                            readParameters.add(paramTemplateRhs.render());
                            j++;
                        }
                    } else {
                        // Access left-most parameter
                        ST paramTemplateLhs = currentGroup.getInstanceOf("model_check_transition_param_assignment");
                        TemplateHandler.add(paramTemplateLhs, "type", typeGenerator.generate(paramNode.getType()));
                        TemplateHandler.add(paramTemplateLhs, "param", "_tmp_" + j);
                        TemplateHandler.add(paramTemplateLhs, "val", j == 2 ? "param" : "_tmp_" + (j - 2));
                        TemplateHandler.add(paramTemplateLhs, "isLhs", true);
                        TemplateHandler.add(paramTemplateLhs, "oneParameter", false);
                        String lhsParameter = paramTemplateLhs.render();
                        readParameters.add(lhsParameter);
                        parameters.add(0, "_tmp_" + j);
                        j++;
                    }

                }
            }
        }
        TemplateHandler.add(template, "readParameters", readParameters);
        TemplateHandler.add(template, "parameters", parameters);
        return template.render();
    }

    public String generateEvalState(MachineNode machineNode) {
        ST template = currentGroup.getInstanceOf("model_check_evaluate_state");
        TemplateHandler.add(template, "machine", nameHandler.handle(machineNode.getName()));
        TemplateHandler.add(template, "variables", generateEvaluateVariables());
        return template.render();
    }

    public List<String> generateEvaluateVariables() {
        List<String> variables = new ArrayList<>();
        for(String variable : modelCheckingInfo.getVariables()) {
            ST template = currentGroup.getInstanceOf("model_check_evaluate_variable");
            TemplateHandler.add(template, "getter", variable);
            variables.add(template.render());
        }
        return variables;
    }

    public String generatePrintResult() {
        ST template = currentGroup.getInstanceOf("model_check_print");
        return template.render();
    }

    public String generateMain(MachineNode machineNode) {
        ST template = currentGroup.getInstanceOf("model_check_main");
        TemplateHandler.add(template, "machine", nameHandler.handle(machineNode.getName()));
        return template.render();
    }


    /*
     * This function extracts the couple type from a list of declarations
     */
    public BType extractTypeFromDeclarations(List<DeclarationNode> declarations) {
        if(declarations.size() == 1) {
            return declarations.get(0).getType();
        }
        CoupleType result = new CoupleType(declarations.get(0).getType(), declarations.get(1).getType());
        for(int i = 2; i < declarations.size(); i++) {
            result = new CoupleType(result, declarations.get(i).getType());
        }
        return result;
    }

    public List<String> generateHashEqual() {
        return Arrays.asList(generateEqual(), generateUnequal(), generateHash());
    }

    public String generateEqual() {
        ST template = currentGroup.getInstanceOf("machine_equal");
        TemplateHandler.add(template, "machine", modelCheckingInfo.getMachineName());
        List<String> predicates = new ArrayList<>();
        for(String var : modelCheckingInfo.getVariables()) {
            ST predicateTemplate = currentGroup.getInstanceOf("machine_equal_predicate");
            TemplateHandler.add(predicateTemplate, "var", var);
            predicates.add(predicateTemplate.render());
        }
        TemplateHandler.add(template, "predicates", predicates);
        return template.render();
    }

    public String generateUnequal() {
        ST template = currentGroup.getInstanceOf("machine_unequal");
        TemplateHandler.add(template, "machine", modelCheckingInfo.getMachineName());
        List<String> predicates = new ArrayList<>();
        for(String var : modelCheckingInfo.getVariables()) {
            ST predicateTemplate = currentGroup.getInstanceOf("machine_unequal_predicate");
            TemplateHandler.add(predicateTemplate, "var", var);
            predicates.add(predicateTemplate.render());
        }
        TemplateHandler.add(template, "predicates", predicates);
        return template.render();
    }

    public String generateHash() {
        ST template = currentGroup.getInstanceOf("machine_hash");
        List<String> assignments = new ArrayList<>();
        int i = 0;
        for(String var : modelCheckingInfo.getVariables()) {
            ST assignmentTemplate = currentGroup.getInstanceOf("machine_hash_assignment");
            TemplateHandler.add(assignmentTemplate, "isFirst", i == 0);
            TemplateHandler.add(assignmentTemplate, "var", var);
            assignments.add(assignmentTemplate.render());
            i++;
        }
        TemplateHandler.add(template, "assignments", assignments);
        return template.render();
    }



    public void setModelCheckingInfo(ModelCheckingInfo modelCheckingInfo) {
        this.modelCheckingInfo = modelCheckingInfo;
    }
}