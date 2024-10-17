package de.hhu.stups.codegenerator.ast;

import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.codegenerator.ast.adapter.*;
import de.prob.parser.ast.nodes.DefinitionNode;
import de.prob.parser.ast.nodes.EnumeratedSetDeclarationNode;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.Node;
import de.prob.parser.ast.nodes.OperationNode;
import de.prob.parser.ast.nodes.expression.ExprNode;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.nodes.substitution.SubstitutionNode;

import java.util.ArrayList;
import java.util.List;

public class VisitorCoordinator {

    public MachineNode convertMachineNode(Start start, IDefinitions definitions){
        MachineVisitor visitor = new MachineVisitor(definitions);
        start.apply(visitor);
        return visitor.getResult();
    }

    public MachineNode convertMachineNode(Start start){
        MachineVisitor visitor = new MachineVisitor(null);
        start.apply(visitor);
        return visitor.getResult();
    }

    public SubstitutionNode convertSubstitutionNode(PSubstitution node, MachineNode machineNode){
        SubstitutionVisitor visitor = new SubstitutionVisitor(machineNode);
        if(node != null){
            node.apply(visitor);
        }
        return visitor.getResult();
    }

    public PredicateNode convertPredicateNode(PPredicate node, MachineNode machineNode){
        PredicateVisitor visitor = new PredicateVisitor(machineNode);
        node.apply(visitor);
        return visitor.getResult();
    }

    public OperationNode convertOperationNode(POperation node, MachineNode machineNode){
        OperationVisitor visitor = new OperationVisitor(machineNode);
        node.apply(visitor);
        return visitor.getResult();
    }

    public ExprNode convertExpressionNode(PExpression node, MachineNode machineNode){
        ExpressionVisitor visitor = new ExpressionVisitor(machineNode);
        node.apply(visitor);
        return visitor.getResult();
    }

    public ExprNode convertExpressionNode(PExpression node){
        ExpressionVisitor visitor = new ExpressionVisitor(null);
        node.apply(visitor);
        return visitor.getResult();
    }

    public Node convertSetNode(PSet node, MachineNode machineNode){
        SetVisitor visitor = new SetVisitor(machineNode);
        node.apply(visitor);
        return visitor.getResult();
    }

    public DefinitionNode convertDefinitionNode(PDefinition node, MachineNode machineNode){
        DefinitionVisitor visitor = new DefinitionVisitor(machineNode);
        node.apply(visitor);
        return visitor.getResult();
    }

    public List<ExprNode> convertExpressionNode(List<PExpression> nodeList){
        List<ExprNode> resultList = new ArrayList<>();
        for (PExpression node: nodeList){
            resultList.add(convertExpressionNode(node));
        }
        return resultList;
    }

    public List<OperationNode> convertOperationNode(List<POperation> nodeList, MachineNode machineNode){
        List<OperationNode> resultList = new ArrayList<>();
        for (POperation node: nodeList){
            resultList.add(convertOperationNode(node, machineNode));
        }
        return resultList;
    }

    public List<SubstitutionNode> convertSubstitutionNode(List<PSubstitution> nodeList, MachineNode machineNode){
        List<SubstitutionNode> resultList = new ArrayList<>();
        for (PSubstitution node : nodeList){
            resultList.add(convertSubstitutionNode(node, machineNode));
        }
        return resultList;
    }

    public List<Node> convertSetNode(List<PSet> nodeList, MachineNode machineNode){
        List<Node> resultList = new ArrayList<>();
        for(PSet node : nodeList){
            resultList.add(convertSetNode(node, machineNode));
        }
        return resultList;
    }

    public List<DefinitionNode> convertDefinitionNode(List<PDefinition> nodeList, MachineNode machineNode){
        List<DefinitionNode> resultList = new ArrayList<>();
        for(PDefinition node : nodeList){
            resultList.add(convertDefinitionNode(node, machineNode));
        }
        return resultList;
    }
}
