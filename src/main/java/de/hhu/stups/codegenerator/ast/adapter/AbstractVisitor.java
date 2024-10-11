package de.hhu.stups.codegenerator.ast.adapter;

import de.prob.parser.ast.nodes.Node;

public abstract class AbstractVisitor {
    private Node result;

    public abstract Node getResult();
}