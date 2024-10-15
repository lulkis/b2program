package de.hhu.stups.codegenerator.ast;

import de.be4.classicalb.core.parser.node.PPredicate;
import de.be4.classicalb.core.parser.node.PSubstitution;
import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.codegenerator.ast.adapter.MachineVisitor;
import de.hhu.stups.codegenerator.ast.adapter.PredicateVisitor;
import de.hhu.stups.codegenerator.ast.adapter.SubstitutionVisitor;
import de.prob.parser.ast.nodes.MachineNode;
import de.prob.parser.ast.nodes.predicate.PredicateNode;
import de.prob.parser.ast.nodes.substitution.SubstitutionNode;

public class VisitorCoordinator {

    public MachineNode convertMachineNode(Start start){
        MachineVisitor visitor = new MachineVisitor();
        start.apply(visitor);
        return visitor.getResult();
    }

    public SubstitutionNode convertSubstitutionNode(PSubstitution node){
        SubstitutionVisitor visitor = new SubstitutionVisitor();
        node.apply(visitor);
        return visitor.getResult();
    }

    public PredicateNode convertPredicateNode(PPredicate node){
        PredicateVisitor visitor = new PredicateVisitor();
        node.apply(visitor);
        return visitor.getResult();
    }
}
