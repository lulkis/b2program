package de.hhu.stups.codegenerator.ast.nodes;

import de.be4.classicalb.core.parser.IDefinitions;
import de.prob.parser.ast.SourceCodePosition;
import de.prob.parser.ast.nodes.MachineNode;

public class MachineNodeWithDefinitions extends MachineNode {

    private IDefinitions iDefinitions;

    public MachineNodeWithDefinitions(SourceCodePosition sourceCodePosition) {
        super(sourceCodePosition);
    }

    public void setIDefinitions(IDefinitions iDefinitions){
        this.iDefinitions = iDefinitions;
    }

    public IDefinitions getIDefinition(){
        return iDefinitions;
    }
}
