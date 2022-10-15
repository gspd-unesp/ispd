package ispd.arquivo.xml.models;

import ispd.gui.iconico.Edge;
import ispd.gui.iconico.Vertex;

import java.util.List;

/**
 * Simple tuple-like object to represent an iconic model, with vertices and
 * edges
 *
 * @param vertices vertices in the model
 * @param edges edges in the model
 */
public record IconicModel(
        List<Vertex> vertices,
        List<Edge> edges) {
}