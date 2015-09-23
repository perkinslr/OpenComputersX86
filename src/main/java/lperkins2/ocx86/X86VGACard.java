package lperkins2.ocx86;

import ds.mods.OCLights2.block.tileentity.TileEntityGPU;
import li.cil.oc.api.component.TextBuffer;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.network.Environment;
import org.jpc.emulator.pci.peripheral.DefaultVGACard;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Iterator;
import java.util.Map.Entry;

public class X86VGACard extends DefaultVGACard {

	
	
	public void paint(Graphics2D g){
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(buffer, 0, 0, 720, 480, 0, 0, 720, 480, null);
	}

	public void renderToMachine(BufferedImage image, Machine machine){
		String address = null;
		String gpu_address = null;
		Iterator<Entry<String, String>> components = (machine.components().entrySet().iterator());
		while (components.hasNext()){ 
			Entry<String, String> comp = components.next();
			if (comp.getValue().equals("screen")){
                address = comp.getKey();
            }
			else if (comp.getValue().equals("ocl_gpu")){
                gpu_address=comp.getKey();
			}
			
			
		}
		if (address!=null){
            Environment host = machine.node().network().node(address).host();
            if (host instanceof TextBuffer){
                TextBuffer screen = (TextBuffer) host;
                renderImageToScreen(image, screen);
            }
		}
		if (gpu_address!=null){
			Environment host = machine.node().network().node(address).host();
                if (host instanceof TileEntityGPU){
            	    TileEntityGPU gpu = (TileEntityGPU) host;
                    renderImageToOCL2GPU(image, gpu);
                }
            }

		
	}
	private void renderImageToOCL2GPU(BufferedImage image, TileEntityGPU gpu){
		return;
	}
	private void renderImageToScreen(BufferedImage image, TextBuffer screen) {
        if (true){
            return;
        }
        screen.setMaximumResolution(720,480/2);
		screen.setResolution(720, 480 / 2);
		screen.setColorDepth(TextBuffer.ColorDepth.EightBit);
		int targetWidth=720;
		int targetHeight=480;
		screen.fill(0, 0, targetWidth, targetHeight/2, '\u2580');
		screen.setRenderingEnabled(true);
		int[] rawData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		int idx;
		int stop = targetWidth*targetHeight;
		for (idx=0;idx<stop;idx++){
			int x = idx % targetWidth;
			if (x==0){
				idx+=targetWidth;
			}
			int y=idx/targetWidth;
			screen.setBackgroundColor(rawData[idx]);
			screen.setForegroundColor(rawData[idx]);
			screen.set(x, y, "\u2580", false);



		}
		

	}

}
