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
 * ProgressoSimulacao.java
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
 * 18-Set-2014 : Retirado analise de modelo iconico e simulável
 * 14-Out-2014 : Adicionado chamadas para realizar a simulação
 *
 */
package ispd.motor;

import ispd.InterpretadorInterno.ModeloIconico.InterpretadorIconico;
import ispd.InterpretadorInterno.ModeloSimulavel.InterpretadorSimulavel;
import ispd.arquivo.xml.IconicoXML;
import org.w3c.dom.Document;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe de conexão entre interface de usuario e motor de simulação
 *
 * @author denison
 */
public abstract class ProgressoSimulacao
{
    /**
     * Escreve os arquivos com os modelos icônicos e simuláveis, e realiza a
     * analise e validação dos mesmos
     *
     * @param iconicModel Texto contendo o modelo icônico que será analisado
     */
    public void AnalisarModelos (final String iconicModel)
    {
        final File file = new File("modeloiconico");

        this.doTask("Writing iconic model.", () -> this.writeIconicModel(iconicModel, file));

        final InterpretadorIconico parser = new InterpretadorIconico();

        this.doTask("Interpreting iconic model.", () -> parser.leArquivo(file));

        this.doTask("Writing simulation model.", parser::escreveArquivo);

        this.doTask("Interpreting simulation model.", this::interpretSimulationModel);
    }

    private void doTask (final String taskName, final Runnable task)
    {
        this.printTaskName(taskName);
        task.run();
        this.incProgresso(5);//[5%] --> 10%
        this.println("OK", Color.green);
    }

    private void writeIconicModel (final String model, final File file)
    {
        try (final FileWriter fw = new FileWriter(file);
             final PrintWriter pw = new PrintWriter(fw, true))
        {
            pw.print(model);
        } catch (final IOException ex)
        {
            Logger.getLogger(ProgressoSimulacao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void interpretSimulationModel ()
    {
        final InterpretadorSimulavel parser2 = new InterpretadorSimulavel();
        final PrintStream stdout = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        parser2.leArquivo(new File("modelosimulavel"));
        System.setOut(stdout);
    }

    private void printTaskName (final String text)
    {
        this.print(text);
        this.print(" -> ");
    }

    public abstract void incProgresso (int n);

    public void println (final String text, final Color cor)
    {
        this.print(text, cor);
        this.print("\n", cor);
    }

    public void print (final String text)
    {
        this.print(text, Color.black);
    }

    public abstract void print (String text, Color cor);

    public void validarInicioSimulacao (final Document model) throws IllegalArgumentException
    {
        this.printTaskName("Verifying configuration of the icons.");

        if (null == model)
        {
            this.printAndThrow(new IllegalArgumentException("The model has no icons."));
            return;
        }

        try
        {
            IconicoXML.validarModelo(model);
        } catch (final IllegalArgumentException e)
        {
            this.printAndThrow(e);
        }

        this.incProgresso(5);
        this.println("OK", Color.green);
    }

    private void printAndThrow (final IllegalArgumentException x)
    {
        this.println("Error!", Color.red);
        throw x;
    }

    public void println (final String text)
    {
        this.print(text, Color.black);
        this.print("\n", Color.black);
    }
}