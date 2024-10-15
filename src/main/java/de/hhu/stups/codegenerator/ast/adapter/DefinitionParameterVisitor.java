package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.PExpression;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.Node;

import java.util.List;

public class DefinitionParameterVisitor extends AbstractVisitor{

    private Node result;
    private MachineNode machineNode;

    private List<PExpression> parameterBefore;
    private List<PExpression> parameterAfter;

    public DefinitionParameterVisitor(List<PExpression> parameterBefore, List<PExpression> parameterAfter) {
        this.parameterBefore = parameterBefore;
        this.parameterAfter = parameterAfter;
    }

    public Node getResult(){
        return result;
    }

    @Override
    public void caseAIdentifierExpression(AIdentifierExpression node){
        for(int i = 0; i < parameterAfter.size(); i++){
            if(node.toString().equals(parameterBefore.get(i).toString())){
                if(parameterAfter.get(i) instanceof AIdentifierExpression){
                    node.replaceBy(parameterAfter.get(i).clone());
                }
            }
        }
    }
}
