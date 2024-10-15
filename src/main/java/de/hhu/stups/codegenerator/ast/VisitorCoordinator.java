package de.hhu.stups.codegenerator.ast;

import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.codegenerator.ast.adapter.*;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.OperationNode;
import de.prob.parser.ast.nodes.expression.ExprNode;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.nodes.substitution.SubstitutionNode;
import jdk.dynalink.Operation;

import java.util.ArrayList;
import java.util.List;

public class VisitorCoordinator {

    public MachineNode convertMachineNode(Start start){
        MachineVisitor visitor = new MachineVisitor();
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

    public ExprNode convertExpressionNode(PExpression node){
        ExpressionVisitor visitor = new ExpressionVisitor();
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
}
