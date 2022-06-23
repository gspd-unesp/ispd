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
 * IdentificadorItemGrade.java
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
package ispd.gui.iconico.grade;

public final class GridItemIdentifier {

    /**
     * It represents the local identifier for the {@link
     * GridItem grid item} which owns this identifier.
     */
    private Integer localId;

    /**
     * It represents the global identifier for the {@link
     * GridItem grid item} which owns this identifier.
     */
    private Integer globalId;

    /**
     * It represents the name for the {@link GridItem grid
     * item} which owns this identifier.
     */
    private String name;

    /**
     * Constructor of {@link GridItemIdentifier} which
     * specifies the local, global and name identifiers.
     *
     * @param localId  the local id
     * @param globalId the global id
     * @param name     the name
     */
    /* package-private */ GridItemIdentifier(final int localId,
                                             final int globalId,
                                             final String name) {
        this.localId = localId;
        this.globalId = globalId;
        this.name = name;
    }

    /**
     * It returns the local id.
     *
     * @return the local id
     */
    public Integer getLocalId() {
        return localId;
    }

    /**
     * It sets the local id.
     *
     * @param localId the local id to be set to
     */
    public void setLocalId(final Integer localId) {
        this.localId = localId;
    }

    /**
     * It returns the global id.
     *
     * @return the global id
     */
    public Integer getGlobalId() {
        return globalId;
    }

    /**
     * It sets the global id.
     *
     * @param globalId the global id to be set to
     */
    public void setGlobalId(final Integer globalId) {
        this.globalId = globalId;
    }

    /**
     * It returns the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * It sets the name.
     *
     * @param name the name to be set to
     */
    public void setName(final String name) {
        this.name = name;
    }
}
