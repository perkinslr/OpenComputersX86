package lperkins2.x86Computers;

import java.awt.Color;
import java.awt.Dimension;

import li.cil.oc.api.component.TextBuffer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.network.Environment;

import org.jpc.emulator.pci.peripheral.VGACard;
import org.jpc.j2se.PCMonitor;

import ds.mods.OCLights2.CommandEnum;
import ds.mods.OCLights2.block.tileentity.TileEntityGPU;
import ds.mods.OCLights2.gpu.DrawCMD;
import ds.mods.OCLights2.gpu.Texture;

public class OpenComputersVGACard extends VGACard{
    private int[] rawImageData;
    private int xmin,  xmax,  ymin,  ymax,  width,  height;
    private BufferedImage buffer;
    int renderingPass=0;
    OpenComputersX86Monitor monitor;

    public OpenComputersVGACard() 
    {
    }

    public int getXMin() {
        return xmin;
    }

    public int getXMax() {
        return xmax;
    }

    public int getYMin() {
        return ymin;
    }

    public int getYMax() {
        return ymax;
    }

    protected int rgbToPixel(int red, int green, int blue) {
        return ((0xFF & red) << 16) | ((0xFF & green) << 8) | (0xFF & blue);
    }

    public void resizeDisplay(int width, int height) 
    {
        if ((width == 0) || (height == 0))
            return;
        this.width = width;
        this.height = height;

        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        buffer.setAccelerationPriority(1);
        DataBufferInt buf = (DataBufferInt) buffer.getRaster().getDataBuffer();
        rawImageData = buf.getData();
        if (monitor != null){
            monitor.resizeDisplay(width, height);
        }
    }

    public void saveScreenshot()
    {
        File out = new File("Screenshot.png");
        try
        {
            ImageIO.write(buffer, "png", out);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setMonitor(OpenComputersX86Monitor mon) {
        this.monitor = mon;
        monitor.resizeDisplay(width, height);
    }

    public Dimension getDisplaySize()
    {
        return new Dimension(width, height);
    }

    public int[] getDisplayBuffer() 
    {
        return rawImageData;
    }

    protected void dirtyDisplayRegion(int x, int y, int w, int h) 
    {
        xmin = Math.min(x, xmin);
        xmax = Math.max(x + w, xmax);
        ymin = Math.min(y, ymin);
        ymax = Math.max(y + h, ymax);
    }

    public void paintPCMonitor(Graphics g, PCMonitor monitor)
    {
        g.drawImage(buffer, 0, 0, null);
        Dimension s = this.getDisplaySize();
        g.setColor(Color.black);
        g.fillRect(width, 0, s.width - width, height);
        g.fillRect(0, height, s.width, s.height - height);
    }

    public final void prepareUpdate() 
    {
        xmin = width;
        xmax = 0;
        ymin = height;
        ymax = 0;
    }

    @Override
    public void setMonitor(PCMonitor mon)
    {
        // TODO Auto-generated method stub
        
    }
    
    public BufferedImage getImageData(){
        return buffer;
    }

    public void render(Machine machine)
    {
        int stop = this.width*this.height;
        
        int idx;
        
        int x;
        int y;
        int rgbColor;
        int rgbColor2;
        this.monitor.renderToScreen();
        saveScreenshot();
        
        Iterator<Entry<String, String>> components = (machine.components().entrySet().iterator());
        Entry<String, String> comp;
        String address=null;
        String gpu_address=null;
        rawImageData = ((DataBufferInt) buffer.getRaster().getDataBuffer()).getData();
        while (components.hasNext()){ 
            comp = components.next();
            if (comp.getValue().equals("screen")){
                address=comp.getKey();
            }
            if (comp.getValue().equals("ocl_gpu")){
                gpu_address=comp.getKey();
            }
        }
        if (address!=null){
            try{
                
            
                Environment host = machine.node().network().node(address).host();       
                if (host instanceof TextBuffer){
                    TextBuffer screen = (TextBuffer) host;
                    
                    
                    X86OpenComputers.log.info(""+screen.getMaximumWidth()+"x"+ screen.getMaximumHeight());
                    screen.setMaximumResolution(width+1, height);
                    screen.setResolution(width, height);
                    screen.setRenderingEnabled(true);
                    
                    rawImageData = ((DataBufferInt) buffer.getRaster().getDataBuffer()).getData();
                    for (idx=0;idx<stop;idx++){
                        x=idx%this.width;
                        y=idx/this.width;
                        if (x==0){
                            y++;
                            idx+=this.width;
                        }
                        rgbColor = rawImageData[idx+width*renderingPass];
                        rgbColor2 = rawImageData[idx+width+this.width*renderingPass];
                        screen.setBackgroundColor(rgbColor);
                        screen.setForegroundColor(rgbColor2);
                        screen.set(x, y, "\u2580", false);
                        
                    }
                    
                }
                else{
                    X86OpenComputers.log.info("No screen found");
                 //   return;
                }
            }
            catch(Exception e) {
                X86OpenComputers.log.info("Screen display had an error");
                e.printStackTrace();
            }
            
            
        }
        else{
            X86OpenComputers.log.info("No screen found");
            //return;
        }
        
        if (gpu_address!=null){
            X86OpenComputers.log.info("GPU found");
            Environment host = machine.node().network().node(gpu_address).host();       
            if (host instanceof TileEntityGPU){
                TileEntityGPU gpu = (TileEntityGPU) host;
                Texture t = new Texture(this.width, this.height);
                t.graphics.drawImage(buffer, 0, 0, width, height, new Color(0,0,0), (ImageObserver) null);
                gpu.gpu.textures[42]=t;
                
                
                
                
                

                try
                {
                    DrawCMD cmd = new DrawCMD();
                    Object[] nargs = new Object[] { 0, 42, 0, 0 };
                    cmd.cmd = CommandEnum.DrawTexture;
                    cmd.args = nargs;
                    gpu.gpu.processCommand(cmd);
                    gpu.gpu.drawlist.push(cmd);
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                
                
            }
            else{
                X86OpenComputers.log.info("GPU not castable!");
            }
        }
        
        
        
                
        
        
    }

    public void paintPCMonitor(Graphics g, OpenComputersX86Monitor openComputersX86Monitor)
    {
        g.drawImage(buffer, 0, 0, null);
        Dimension s = openComputersX86Monitor.getSize();
        g.setColor(Color.black);
        g.fillRect(width, 0, s.width - width, height);
        g.fillRect(0, height, s.width, s.height - height);
        
    }
    
    
        
}
