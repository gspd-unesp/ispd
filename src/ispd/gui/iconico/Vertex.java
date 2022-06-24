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
 * Vertice.java
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

public abstract class Vertex implements Icon {

    /**
     * It represents the <em>base</em> x-coordinate in cartesian
     * coordinates.
     */
    private int baseX;

    /**
     * It represents the <em>base</em> y-coordinate in cartesian
     * coordinates.
     */
    private int baseY;

    /**
     * It represents the x-coordinate in cartesian coordinates.
     */
    private Integer x;

    /**
     * It represents the y-coordinate in cartesian coordinates.
     */
    private Integer y;

    /**
     * It marks whether the vertex is selected. If this
     * variable is {@code true}, then this vertex is selected;
     * otherwise, it is not selected.
     */
    private boolean selected;

    /**
     * Constructor of {@link Vertex} which specifies the
     * x-coordinate, y-coordinate and whether the icon
     * is selected.
     *
     * @param x        the x-coordinate
     * @param y        the y-coordinate
     * @param selected whether the icon is selected
     */
    public Vertex(final Integer x,
                  final Integer y,
                  final boolean selected) {
        this.x = x;
        this.y = y;
        this.selected = selected;
    }

    /**
     * Constructor of {@link Vertex} which specifies the
     * x-coordinate and y-coordinate in the cartesian coordinates.
     * Further, the icon is set as non-selected by default.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @see #Vertex(Integer, Integer, boolean)
     */
    public Vertex(final Integer x, final Integer y) {
        this(x, y, false);
    }

    /**
     * Returns the x-coordinate.
     *
     * @return the x-coordinate
     */
    @Override
    public Integer getX() {
        return this.x;
    }

    /**
     * Returns the y-coordinate.
     *
     * @return the y-coordinate
     */
    @Override
    public Integer getY() {
        return this.y;
    }

    /**
     * Returns the base x-coordinate.
     *
     * @return the base x-coordinate
     */
    public int getBaseX() {
        return this.baseX;
    }

    /**
     * Returns the base y-coordinate.
     *
     * @return the base y-coordinate
     */
    public int getBaseY() {
        return this.baseY;
    }

    /**
     * It sets the vertex's <em>base</em> X and Y coordinates to the
     * specified coordinates.
     *
     * @param x the base x-coordinate
     * @param y the base y-coordinate
     */
    public void setBase(final Integer x, final Integer y) {
        this.baseX = x;
        this.baseY = y;
    }

    /**
     * It sets the vertex's X and Y coordinates.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public void setPosition(final Integer x, final Integer y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns {@code true} if this vertex is selected.
     * Otherwise, returns {@code false}.
     *
     * @return {@code true} if this vertex is selected;
     *         otherwise, returns {@code false}.
     */
    @Override
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * It sets the vertex as selected or not.
     *
     * @param selected if {@code true}, set this icon as selected;
     *                 otherwise, set this icon as not selected.
     */
    @Override
    public void setSelected(final boolean selected) {
        this.selected = selected;
    }
}
