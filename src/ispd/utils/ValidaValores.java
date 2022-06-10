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
 * ValidaValores.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Aldo Ianelo Guerra;
 * Contributor(s):   Denison Menezes;
 *
 * Changes
 * -------
 *
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Aldo
 */
public class ValidaValores
{
    private static final Collection<String> palavrasReservadasJava = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
            "default", "do", "double", "else", "enum", "extends", "false", "final", "finally", "float", "for", "goto",
            "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null",
            "package", "private", "protected", "public", "return",
            "short", "static", "strictfp", "super", "switch", "synchronized",
            "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while"
    ); // TODO: Add var? Any other keywords?
    private static Collection<String> nodes = new HashSet<>(); // TODO: updated, but not queried. Useless?

    public static void addNomeIcone (String s)
    {
        ValidaValores.nodes.add(s);
    }

    public static void removeNomeIcone (String s)
    {
        ValidaValores.nodes.remove(s);
    }

    public static void removeTodosNomeIcone ()
    {
        ValidaValores.nodes = new HashSet<>();
    }

    public static boolean validaNomeClasse (String name)
    {
        if (!name.matches("[a-zA-Z$_][a-zA-Z\\d$_]*"))
            return false;

        return !palavrasReservadasJava.contains(name);
    }
}