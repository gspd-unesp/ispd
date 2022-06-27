/*	Author: Victor Aoqui
	Last modified: 10/10/2010
	Universidade Estadual Paulista "Jlio de Mesquita Filho"
	Instituto de Biocincias, Letras e Cincias Exatas
	Departamento de Cincias de Computao e Estatstica
	Grupo de Sistemas Paralelos e Distribudos
*/

package ispd.InterpretadorExterno.SimGrid;
import java.io.*;

public class InterpretadorSimGrid
{
	private static String fname;

	private void setFileName(File f)
	{
		fname = f.getName();
	}
	
	public static String getFileName()
	{
		return fname;
	}
	
	public void interpreta(File file1, File file2)
	{
		boolean error;
		try
		{
			try
			{
				FileInputStream application_file = new FileInputStream(file1);
				FileInputStream plataform_file = new FileInputStream(file2);
				SimGrid parser = SimGrid.getInstance(application_file);
				setFileName(file1);
				parser.ReInit(application_file);
				parser.modelo();
				setFileName(file2);
				parser.ReInit(plataform_file);
				parser.modelo();
				error = parser.resultadoParser();
				if(!error)
					parser.writefile();
				parser.reset();
			}
			catch(ParseException x){}
		}
		catch(IOException y){}
	}
}
