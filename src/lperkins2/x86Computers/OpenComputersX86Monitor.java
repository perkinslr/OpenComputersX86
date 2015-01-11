package lperkins2.x86Computers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;

import org.jpc.emulator.PC;
import org.jpc.j2se.KeyHandlingPanel;
import org.jpc.j2se.PCMonitor;

public class OpenComputersX86Monitor extends KeyHandlingPanel{
    OpenComputersX86PC pc;
    OpenComputersVGACard vgaCard;
    public OpenComputersX86Monitor(OpenComputersX86PC pc, OpenComputersVGACard vgaCard)
    {
        this.pc=pc;
        this.vgaCard=vgaCard;
        this.setSize(vgaCard.getDisplaySize());
                
    }
    
    public boolean renderToScreen(){
        
        vgaCard.prepareUpdate();
        vgaCard.updateDisplay();

        int xmin = vgaCard.getXMin();
        int xmax = vgaCard.getXMax();
        int ymin = vgaCard.getYMin();
        int ymax = vgaCard.getYMax();
        
        repaint(xmin, ymin, xmax - xmin + 1, ymax - ymin + 1);
        return true;
    }
    
    public void resizeDisplay(int width, int height) 
    {
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));

        revalidate();
        repaint();
    }
    
    
    public void update(Graphics g) 
    {
        paint(g);
    }

    public void paint(Graphics g) 
    {
        
        vgaCard.paintPCMonitor(g, this);
    }
}
