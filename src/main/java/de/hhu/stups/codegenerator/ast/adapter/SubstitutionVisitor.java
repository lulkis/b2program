package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.Node;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.substitution.SubstitutionNode;

public class SubstitutionVisitor extends AbstractVisitor{

    private SubstitutionNode resultSubstitutionNode;

    public SubstitutionNode getResult(){
        return resultSubstitutionNode;
    }

    private SourceCodePosition getSourceCodePosition(Node node) {
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
