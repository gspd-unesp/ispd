package ispd.Interface;
import java.awt.*;
import java.util.*;
import javax.swing.*;



public class ValidaValores{

	public ValidaValores(){
	}

	public static String nomePrograma = "iSPD";


/*################################################
#	Metodo para validar se o valor fornecido #
#pelo usuário corresponde a um inteiro.          #
################################################*/
	public static boolean validaInteiro(String temp){
		if(temp.matches("\\d*")){
			return true;
		}else{
			JOptionPane.showMessageDialog(null,"Please enter an integer number.","Warning",JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}

/*################################################
#	Metodo para validar se o nome fornecido  #
#pelo usuário corresponde a a um nome valido     #
#para um icone.                                  #
################################################*/
	public static boolean validaNome(String temp){
		if(temp.matches("[a-zA-Z](.)*") || temp.matches("")){
			return true;
		}else{
			JOptionPane.showMessageDialog(null,"Please enter a valid name.\nThe name must begin with a letter.","Warning",JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}

/*################################################
#	Metodo para validar se o valor fornecido #
#pelo usuário corresponde a um double valido.    #
################################################*/
	public static boolean validaDouble(String temp){
		if(temp.matches("[0-9]+,[0-9]+")){
			JOptionPane.showMessageDialog(null,"Please use \'.\' to specify floating point numbers.","Warning",JOptionPane.WARNING_MESSAGE);
			return false;
		}else if(temp.matches("[0-9]+.[0-9]+") || temp.matches("")){
				return true;
			}else if(temp.matches("[0-9]+")){
					return true;
				}else{
					JOptionPane.showMessageDialog(null,"Please enter a floating point number.","Warning",JOptionPane.WARNING_MESSAGE);
					return false;
				}
	}

}
