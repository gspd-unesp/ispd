/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * Icone.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
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

import java.awt.Graphics;

public interface Icon {

    /**
     * It draws this icon.
     *
     * @param g the {@link Graphics} which will draw this icon
     */
    void draw(Graphics g);

    /**
     * Returns {@code true} if this icon is contained in an area near
     * the given X and Y coordinates. Otherwise, {@code false} is
     * returned.
     *
     * @apiNote
     * This method is often used to detect clicks on the icon from the
     * user interacting in the grid.
     *
     * @implNote
     * The area in which will be set to define whether this icon is
     * contained is this area is implementation-dependent, that is each
     * class implementing {@link Icon} may define a different area.
     *
     * @param x the X-coordinate
     * @param y the Y-coordinate
     *
     * @return {@code true} if this icon is near the given X and  Y
     *         coordinates; otherwise, returns {@code false}.
     */
    boolean contains(int x, int y);

    /**
     * Returns {@code true} if this icon has been selected. Otherwise
     * {@code false} is returned.
     *
     * @return {@code true} if this icon has been selected; otherwise,
     *          {@code false} is returned.
     */
    boolean isSelected();

    /**
     * It sets this {@link Icon icon} as selected or not.
     *
     * @param selected if {@code true}, set this icon as selected;
     *                 otherwise, set this icon as not selected.
     */
    void setSelected(boolean selected);

    /**
     * Returns the icon x-coordinate in cartesian coordinates.
     * @return the icon x-coordinate in cartesian coordinates
     */
    Integer getX();

    /**
     * Returns the icon y-coordinate in cartesian coordinates.
     * @return the icon y-coordinate in cartesian coordinates
     */
    Integer getY();

}
