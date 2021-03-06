package eu.yvka.slothengine.scene.traversal;

import eu.yvka.slothengine.scene.Node;

/**
 * Visitor interface which is call by the traversal algorithms
 * for each visited node.
 *
 * @param <T> the type of the node
 */
@FunctionalInterface
public interface Visitor<T extends Node> {
	void visit(T node);
}
