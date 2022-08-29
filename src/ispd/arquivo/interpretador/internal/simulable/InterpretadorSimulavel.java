package ispd.arquivo.interpretador.internal.simulable;

import ispd.motor.queueNetworks.RedesDeFilas;

import javax.swing.JOptionPane;
import java.io.File;
import java.util.List;


public class InterpretadorSimulavel{

	Interpretador parser;
	
	public InterpretadorSimulavel(){
		
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
		//parser.escreveArquivo();
	}

	public RedesDeFilas getRedeFilas(){
		return parser.getRedeFilas();
	}
	
	public String getTarefas(){
		return parser.getTarefas();
	}
	
	public List<String> getNomeId(){
		return parser.getNomeId();
	}

}