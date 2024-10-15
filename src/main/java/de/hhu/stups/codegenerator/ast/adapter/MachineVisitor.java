package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.Node;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.DeclarationNode;
import de.prob.parser.ast.nodes.EnumeratedSetDeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.OperationNode;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.nodes.substitution.SubstitutionNode;

import java.util.ArrayList;
import java.util.List;

public class MachineVisitor extends AbstractVisitor{

    private MachineNode resultMachineNode;

    private String name;
    private List<DeclarationNode> variables = new ArrayList<>();
    private PredicateNode invariant;
    private SubstitutionNode initialisation;
    private List<OperationNode> operations;
    private List<EnumeratedSetDeclarationNode> setEnumerations = new ArrayList<>();


    public MachineNode getResult(){
        return resultMachineNode;
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
