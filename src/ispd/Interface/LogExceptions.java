package ispd.Interface;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.io.PrintStream;
import java.lang.*;
import java.util.*;
import java.awt.image.*;  
import java.net.*;  
import javax.imageio.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;



public class LogExceptions implements Thread.UncaughtExceptionHandler {
	
	public void uncaughtException(Thread t, Throwable e){
		ByteArrayOutputStream fosErr = new ByteArrayOutputStream();
		PrintStream psErr = new PrintStream(fosErr);
		e.printStackTrace(psErr);
		mostrarErro(fosErr);
		e.printStackTrace();
		//JOptionPane.showMessageDialog(null, t+"\nErro\n"+fosErr.toString());
	}

	private void mostrarErro(ByteArrayOutputStream objErr){
		try{
			if(objErr.size() > 0){
				String erro = "";
				erro += "\n---------- error description ----------\n";
				erro += objErr.toString();
				erro += "\n---------- error description ----------\n";
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
				Date date = new Date();
				String codigo = dateFormat.format(date);
				File file = new File("Error_iSPD_"+codigo);
				FileWriter writer = new FileWriter(file);
				PrintWriter saida = new PrintWriter(writer,true);
				saida.print(erro);
				saida.close();   
				writer.close();
				String saidaString = "";
				saidaString += "Error encountered during system operation.\n";
				saidaString += "Error saved in the file: "+ file.getAbsolutePath()+"\n";
				saidaString += "Please send the error to the developers.\n";
				saidaString += erro;
				CaixaTexto caixa = new CaixaTexto("System Error",saidaString);
				caixa.setVisible(true);
				objErr.reset();
			}
		}catch(Exception e){  
			JOptionPane.showMessageDialog(null,e.getMessage(),"Warning",JOptionPane.WARNING_MESSAGE);  
		}
	}
	
}