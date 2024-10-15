package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.Node;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.DefinitionNode;

public class DefinitionVisitor extends AbstractVisitor{

    private DefinitionNode resultDefinitionNode;

    public DefinitionNode getResult() {
        return resultDefinitionNode;
    }

    private SourceCodePosition getSourceCodePos(Node node){
        SourceCodePosition sourceCodePosition = new SourceCodePosition();
        sourceCodePosition.setStartColumn(node.getStartPos().getPos());
        sourceCodePosition.setStartLine(node.getStartPos().getLine());
        sourceCodePosition.setText(node.toString().replace(" ", ""));
        return sourceCodePosition;
    }
}
