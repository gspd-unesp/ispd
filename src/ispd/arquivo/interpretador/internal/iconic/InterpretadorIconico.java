package ispd.arquivo.interpretador.internal.iconic;

import javax.swing.JOptionPane;
import java.io.File;


public class InterpretadorIconico{

	Interpretador parser;
	
	public InterpretadorIconico(){
		
	}

	public boolean leArquivo(File arquivo){

		try{
			try {
				parser = new Interpretador(new java.io.FileInputStream(arquivo));
				parser.verbose = false;
				parser.printv("Modo verbose ligado");
				parser.Modelo();
			}
			catch (ParseException e) {
				parser.erroEncontrado = true;
				JOptionPane.showOptionDialog(null,"Foram encontrados os seguintes erros:\n"+e.getMessage(),"Erros Encontrados",JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,null,null);
				return parser.erroEncontrado;
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return parser.erroEncontrado;
		}

		return parser.erroEncontrado;
	}


	public void escreveArquivo(){
		parser.escreveArquivo();
	}



}