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
 * Main.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 09-Set-2014 : Version 2.0;
 * 16-Out-2014 : change the location of the iSPD base directory;
 *
 */
package ispd;

import ispd.application.GuiApplication;
import ispd.application.TerminalApplication;

import java.util.Locale;

// TODO: Check permission to delete docs

/**
 * @author denison
 */
public class Main
{
    private static final Locale EN_US_LOCALE = new Locale("en", "US");

    public static void main (String[] args)
    {
        Main.setDefaultLocale();

        final var app = (args.length == 0)
                ? new GuiApplication()
                : new TerminalApplication(args);

        app.run();
    }

    private static void setDefaultLocale ()
    {
        Locale.setDefault(EN_US_LOCALE);
    }
}