/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e
 * Distribuídos da Unesp (GSPD).
 *
 * Project Info:  http://gspd.dcce.ibilce.unesp.br/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 *  USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * Aresta.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e
 * Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.gui.iconico;

public abstract class Edge implements Icon {

    /**
     * It represents the source vertex. Mathematically speaking,
     * this edge is <em>incident from</em> this vertex.
     */
    private Vertex source;

    /**
     * It represents the destination vertex. Mathematically
     * speaking, this edge is <em>incident to</em> this vertex.
     */
    private Vertex destination;

    /**
     * Constructor of {@link Edge} which specifies the source
     * and destination vertices. Mathematically speaking, the
     * source vertex is which this edge is <em>incident
     * from</em> and the destination vertex is which this
     * edge is <em>incident to</em>.
     *
     * @param source      the source vertex
     * @param destination the destination vertex
     */
    public Edge(final Vertex source,
                final Vertex destination) {
        this.source = source;
        this.destination = destination;
    }

    /**
     * Returns the source vertex.
     *
     * @return the source vertex
     */
    public Vertex getSource() {
        return this.source;
    }

    /**
     * Returns the destination vertex.
     *
     * @return the destination vertex
     */
    public Vertex getDestination() {
        return this.destination;
    }

    /**
     * It sets the source and destination vertices.
     *
     * @param source      the source vertex
     * @param destination the destination vertex
     */
    public void setPosition(final Vertex source,
                            final Vertex destination) {
        this.source = source;
        this.destination = destination;
    }

    /**
     * Returns the central edge position in x-axis in
     * cartesian coordinates.
     *
     * @return the central edge position in x-axis in
     *         cartesian coordinates.
     */
    @Override
    public Integer getX() {
        return this.source.getX() + (this.destination.getX() - this.source.getX()) / 2;
    }

    /**
     * Returns the central edge position in y-axis in
     * cartesian coordinates.
     *
     * @return the central edge position in y-axis in
     *         cartesian coordinates.
     */
    @Override
    public Integer getY() {
        return this.source.getY() + (this.destination.getY() - this.source.getY()) / 2;
    }
}
