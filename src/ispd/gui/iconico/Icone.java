/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.gui.iconico;

import java.awt.Graphics;
/**
 *
 * @author denison
 */
public interface Icone {
    
    public boolean isSelected();
    
    public void setSelected(boolean selected);
    
    public void draw(Graphics g);
    
    public boolean contains(int x, int y);
    /**
     * Posição do objeto no eixo X
     */
    public Integer getX();
    /**
     * Posição do objeto no eixo Y
     */
    public Integer getY();
}
