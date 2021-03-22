/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui.iconico.grade;

import java.util.ResourceBundle;
import java.util.Set;

/**
 *
 * @author denison
 */
public interface ItemGrade {

    public IdentificadorItemGrade getId();

    public Set<ItemGrade> getConexoesEntrada();

    public Set<ItemGrade> getConexoesSaida();
    
    public String getAtributos(ResourceBundle palavras);

    /**
     *
     * @param posicaoMouseX the value of posicaoMouseX
     * @param posicaoMouseY the value of posicaoMouseY
     * @param idGlobal the value of idGlobal
     * @param idLocal the value of idLocal
     */
    public ItemGrade criarCopia(int posicaoMouseX, int posicaoMouseY, int idGlobal, int idLocal);
    
    public boolean isConfigurado();
}
