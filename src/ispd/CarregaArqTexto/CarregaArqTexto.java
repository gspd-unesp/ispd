package ispd.CarregaArqTexto;
import ispd.DescreveSistema.*;
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


public class CarregaArqTexto{

	CarregaArq parser;
	
	public CarregaArqTexto(){
		
	}

	public boolean leArquivo(File arquivo){

		try{
			try {
				parser = new CarregaArq(new java.io.FileInputStream(arquivo));
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


	public DescreveSistema getDescricao(){
		return parser.getDescricao();
	}

	public int getW(){
		return parser.getW();
	}


}