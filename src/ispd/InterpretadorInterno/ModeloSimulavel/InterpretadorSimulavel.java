package ispd.InterpretadorInterno.ModeloSimulavel;
import ispd.DescreveSistema.*;
import ispd.RedesDeFilas.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;  
import java.net.*;  
import javax.imageio.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.io.*;


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