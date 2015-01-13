package lperkins2.x86Computers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import li.cil.oc.api.component.TextBuffer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
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
    private BufferedImage getScaledImage(Image srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TRANSLUCENT);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.finalize();
        g2.dispose();
        return resizedImg;
    }
    public void render(Machine machine)
    {
        
        
        this.monitor.renderToScreen();
        saveScreenshot();
        BufferedImage scaled = getScaledImage(buffer, this.width/2, this.height/2);
        
        File out = new File("Screenshot_Resized.png");
        try
        {
            ImageIO.write(scaled, "png", out);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        
        
        
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
                    
                    
                    //renderImageToScreen(buffer, screen);
                    
                    
                    
                    
                    
                    
                }
                else{
                    X86OpenComputers.log.info("No screen found");
                 //   return;
                }
            }
            catch(Exception e) {
                X86OpenComputers.log.info("Screen display had an error");
                X86Architecture.joinStackTrace(e);
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
                renderImageToOpenComputerLights2(buffer, (TileEntityGPU) host);
            }
            else{
                X86OpenComputers.log.info("GPU not castable!");
            }
        }
        
        
        
                
        
        
    }
    
    public void renderImageToScreen(BufferedImage img, TextBuffer screen){
        int targetWidth = img.getWidth(null);
        int targetHeight = img.getHeight(null);
        if (screen.getMaximumWidth() < targetWidth || screen.getMaximumHeight() < targetHeight / 2){
            screen.setMaximumResolution(targetWidth, targetHeight / 2);
            screen.setResolution(targetWidth, targetHeight / 2);
            screen.setRenderingEnabled(true);
        }
        int[] rawData = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int idx;
        
        int x;
        int y;
        int rgbColor;
        int rgbColor2;
        for (idx=0;idx<targetWidth*targetHeight;idx++){
            x=idx%(targetWidth);
            if (x==0){
                idx += this.width;
            }
            y=idx/(targetWidth);
            rgbColor = rawData[idx];
            rgbColor2 = rawData[idx-width];
            screen.setBackgroundColor(rgbColor2);
            screen.setForegroundColor(rgbColor);
            screen.set(x, y, "\u2580", false);
            
        }
        
    }
    
    public void renderImageToOpenComputerLights2(BufferedImage img, TileEntityGPU gpu){
        
        int targetWidth = gpu.gpu.getMonitor().getWidth();
        int targetHeight = gpu.gpu.getMonitor().getHeight();
        
        BufferedImage scaled = getScaledImage(img, targetWidth, targetHeight);
        
        
        
        ByteArrayOutputStream textureBuffer = new ByteArrayOutputStream();
        try
        {
            ImageIO.write(scaled, "png", textureBuffer);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
        X86OpenComputers.log.info("Target width: "+targetWidth);
        X86OpenComputers.log.info("Target height: "+targetHeight);
        
        
        
        
        DrawCMD cmd = new DrawCMD();
        

        
        
        

        
        
        Object[] nargs = new Object[] { toObjects(textureBuffer.toByteArray()) };
        cmd.cmd = CommandEnum.Import;
        cmd.args = nargs;
        int texture = 1;
        try
        {
            texture = (Integer) gpu.gpu.processCommand(cmd)[0];
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        gpu.gpu.drawlist.push(cmd);
        cmd = new DrawCMD();
        nargs = new Object[] { 0, texture, 0, 0};
        cmd.cmd = CommandEnum.DrawTexture;
        cmd.args = nargs;
        try
        {
            gpu.gpu.processCommand(cmd);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        
        
        
        
        gpu.gpu.drawlist.push(cmd);
        
        
        cmd = new DrawCMD();
        nargs = new Object[] { texture };
        cmd.cmd = CommandEnum.FreeTexture;
        cmd.args = nargs;
        
        
        try
        {
            gpu.gpu.processCommand(cmd);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        
        gpu.gpu.drawlist.push(cmd);
        
        
        gpu.gpu.processSendList();
        
    }
  //byte[] to Byte[]
    Byte[] toObjects(byte[] bytesPrim) {

        Byte[] bytes = new Byte[bytesPrim.length];
        int i = 0;
        for (byte b : bytesPrim) bytes[i++] = b; //Autoboxing
        return bytes;

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
