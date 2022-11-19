package ispd.gui.iconico;

import java.awt.Graphics;
import java.awt.Image;

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
     * returned.<br>
     * This method is often used to detect clicks on the icon from the
     * user interacting in the grid.<br>
     * The area in which will be set to define whether this icon is
     * contained is this area is implementation-dependent, that is each
     * class implementing Icon may define a different area.
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


    /**
     * Returns the icon image; that is, the image that is
     * going to be representing this icon.
     *
     * @return this icon image
     */
    Image getImage();

}
