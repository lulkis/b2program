package de.hhu.stups.codegenerator.ast.adapter;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.prob.parser.ast.nodes.Node;

//Eine abstrakte Klasse für die Visitors. Gibt den übersetzten ANTLR Knoten zurück
public abstract class AbstractVisitor extends DepthFirstAdapter {
    private Node result;

    public abstract Node getResult();
}