/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.arquivo.interpretador.gerador;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Classe de interface entre arquivos gerados pelo javaCC para interpretar 
 * a gramatica do gerador de escalonadores e o iSPD
 * @author denison
 */
public class InterpretadorGerador {

    private InputStream istream;
    private Interpretador parser = null;
    
    /**
     * @param codigo Texto com código do gerador de escalonadores
     */
    public InterpretadorGerador(String codigo){
        try {
            istream = new ByteArrayInputStream(codigo.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(InterpretadorGerador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public InterpretadorGerador(InputStream istream) {
        this.istream = istream;
    }

    /**
     * Inicia a analise do código gerador de escalonadores
     * @return erros encontrados no código
     */
    public boolean executarParse() {
        try {
            parser = new Interpretador(istream);
            parser.verbose = false;
            parser.printv("Modo verbose ligado");
            parser.Escalonador();
            return parser.erroEncontrado;
        } catch (ParseException ex) {
            parser.erroEncontrado = true;
            JOptionPane.showMessageDialog(null, "Foram encontrados os seguintes erros:\n" + ex.getMessage(), "Erros Encontrados", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(InterpretadorGerador.class.getName()).log(Level.SEVERE, null, ex);
            return parser.erroEncontrado;
        }
    }

    /**
     * @return Retorna nome do escalonador gerado
     */
    public String getNome() {
        return parser.getArquivoNome();
    }
    
    /**
     * @return Retorna código java do escalonador gerado a partir do código interpretado
     */
    public String getCodigo() {
        return parser.getCodigo();
    }
}
