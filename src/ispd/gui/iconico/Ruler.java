/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ispd.gui.iconico;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Ruler extends JComponent {

    /**
     * It represents the font used to mark the labels on the ruler.
     */
    private static final Font RULER_FONT =
            new Font("SansSerif", Font.PLAIN, 10);

    /**
     * It represents the background color.
     */
    private static final Color RULER_BACKGROUND_COLOR =
            new Color(240, 240, 240);

    /**
     * It represents the tick length. The term <em>tick</em> is coined
     * to represent integral unit numbers in the rule.
     */
    private static final int RULER_TICK_LENGTH = 10;

    /**
     * It represents the pre-tick length. The term <em>pre-tick</em>
     * is coined to represent the fractional numbers in the rule.
     */
    private static final int RULER_PRE_TICK_LENGTH = 7;

    /**
     * It represents the ruler's size.
     */
    private static final int SIZE = 35;

    /**
     * It stores the ruler's orientation, this is essential because
     * the orientation in which the ruler is drawn on the grid depends
     * on the value specified by this variable.
     */
    private final RulerOrientation orientation;

    /**
     * It stores the ruler's unit, this is essential because the unit
     * is used to draw the <em>ticks</em> and <em>labels</em> relative
     * to the unit specified in this variable.
     */
    private RulerUnit unit;

    /**
     * Constructor which specifies the ruler orientation and the ruler unit.
     *
     * @param orientation the orientation
     * @param unit        the unit
     */
    /* package-private */ Ruler(final RulerOrientation orientation,
                                final RulerUnit unit) {
        this.orientation = orientation;
        this.unit = unit;
    }

    protected void paintComponent(final Graphics g) {
        final var units = this.unit.getUnit();
        final var increment = this.unit.getIncrement();

        final Rectangle drawHere = g.getClipBounds();

        // Fill clipping area with dirty brown/orange.
        g.setColor(Ruler.RULER_BACKGROUND_COLOR);
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);

        // Set the color and font for marking the ruler's labels.
        g.setFont(Ruler.RULER_FONT);
        g.setColor(Color.BLACK);

        final var isHorizontal =
                this.orientation == RulerOrientation.HORIZONTAL;

        // Use clipping bounds to calculate first and last tick locations.
        int start;
        final int end;
        if (isHorizontal) {
            start = (drawHere.x / increment) * increment;
            end = (((drawHere.x + drawHere.width) / increment) + 1)
                  * increment;
        } else {
            start = (drawHere.y / increment) * increment;
            end = (((drawHere.y + drawHere.height) / increment) + 1)
                  * increment;
        }

        // Make a special case of 0 to display the number
        // within the rule and draw a units label.
        if (start == 0) {
            final var text = "0 " + this.unit.getSymbol();

            if (isHorizontal) {
                g.drawLine(0, Ruler.SIZE - 1,
                        0, Ruler.SIZE - Ruler.RULER_TICK_LENGTH - 1);
                g.drawString(text, 2, 21);
            } else {
                g.drawLine(Ruler.SIZE - 1, 0,
                        Ruler.SIZE - Ruler.RULER_TICK_LENGTH - 1, 0);
                g.drawString(text, 9, 10);
            }
            start = increment;
        }

        // ticks and labels
        for (int i = start; i < end; i += increment) {
            final String text;
            final int tickLength;

            if (i % units == 0) {
                tickLength = Ruler.RULER_TICK_LENGTH;
                text = Integer.toString(i / units);
            } else {
                tickLength = Ruler.RULER_PRE_TICK_LENGTH;
                text = null;
            }

            if (isHorizontal) {
                g.drawLine(i, Ruler.SIZE - 1, i, Ruler.SIZE - tickLength - 1);
                if (text != null)
                    g.drawString(text, i - 3, 21);
            } else {
                g.drawLine(Ruler.SIZE - 1, i, Ruler.SIZE - tickLength - 1, i);
                if (text != null)
                    g.drawString(text, 9, i + 3);
            }
        }
    }

    /**
     * It updates the ruler unit to the specified unit.
     * <p>
     * Further, the specified unit is supposed to be <em>non-null</em>.
     * Therefore, unexpected behavior may arise if this precondition
     * is not followed.
     *
     * @param newUnit the unit to be updated to
     */
    /* package-private */ void updateUnitTo(final RulerUnit newUnit) {
        this.unit = newUnit;
        this.repaint();
    }

    /**
     * It sets the ruler's preferred height.
     *
     * @param preferredHeight the preferred height
     */
    /* package-private */ void setPreferredHeight(final int preferredHeight) {
        this.setPreferredSize(new Dimension(Ruler.SIZE, preferredHeight));
    }

    /**
     * It sets the ruler's preferred width.
     *
     * @param preferredWidth the preferred width
     */
    /* package-private */ void setPreferredWidth(final int preferredWidth) {
        this.setPreferredSize(new Dimension(preferredWidth, Ruler.SIZE));
    }
}