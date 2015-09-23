package lperkins2.ocx86;

import li.cil.oc.api.component.TextBuffer;
import li.cil.oc.api.machine.Architecture;
import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.Signal;
import li.cil.oc.api.network.Environment;
import lperkins2.ocx86.SerialTTY.OpenComputersTerm;
import lperkins2.ocx86.SerialTTY.OpenComputersVT100;
import lperkins2.ocx86.SerialTTY.X86SerialPort;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jpc.emulator.peripheral.Keyboard;
import org.jpc.j2se.PCMonitor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

@Architecture.Name("x86 (i686)")
public class OpenComputersX86Architecture implements Architecture{
    public Machine machine;
    public boolean dirty=false;
    public X86PC pc;
    public int idx = 0;
    public PCMonitor monitor;
    public Keyboard keyboard;
    public OpenComputersX86Architecture(Machine machine) {
        this.machine = machine;
    }
    public OpenComputersVT100 vt100;
    public ExecutionResult sleep1 = new ExecutionResult.Sleep(1);
    public ExecutionResult sync = new ExecutionResult.SynchronizedCall();
    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    public Environment findComponent(String id){
        String address;
        Iterator<Map.Entry<String, String>> components = (machine.components().entrySet().iterator());
        while (components.hasNext()) {
            Map.Entry<String, String> comp = components.next();
            if (comp.getValue().equals(id)) {
                address = comp.getKey();
                return machine.node().network().node(address).host();
            }
        }
        return null;
    }


    @Override
    public boolean initialize() {
        System.out.println("initializing");
        try {
            pc = new X86PC(this);
            monitor = new PCMonitor(pc);
            keyboard = pc.keyboard;
            Environment screen = findComponent("screen");
            if (screen instanceof TextBuffer) {
                X86SerialPort serial = pc.x86SerialPort;
                OpenComputersTerm term = new OpenComputersTerm((TextBuffer) screen, this);
                vt100 = new OpenComputersVT100(term, serial.inputStream);
            }
            pc.start();



        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean isInitialized() {
        // TODO Auto-generated method stub
        return (pc!=null);
    }

    @Override
    public void load(NBTTagCompound paramNBTTagCompound) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnect() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean recomputeMemory(Iterable<ItemStack> paramIterable) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void runSynchronized() {
        dirty=false;
        vt100.start();
        try {
            System.out.println(new String(vt100.buf, "ASCII"));
        } catch (UnsupportedEncodingException e) {
            System.out.println("Not ascii");
        }
        X86VGACard vc = (X86VGACard) pc.getComponent(X86VGACard.class);
        vc.prepareUpdate();
        vc.updateDisplay();
        vc.saveScreenshot();
        BufferedImage bi = new BufferedImage(720,480,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        vc.paint(g);
        try{
            ImageIO.write(bi, "png", new File("Screeenshot.png"));
        }
        catch (IOException e){

        }
        vc.renderToMachine(bi, this.machine);



    }

    @Override
    public ExecutionResult runThreaded(boolean isSynchronizedReturn) {
        // TODO Auto-generated method stub
        for (int i=0;i<100;i++){
            pc.execute();
        }

        if (idx++%10 == 0 || dirty){
            return sync;


        }



        if (!isSynchronizedReturn) {
            Signal signal = null;
            while (true) {
                signal = machine.popSignal();
                if (signal != null) {
                    if (signal.name().equals("key_down")) {
                        byte character = (byte) (double) (Double) signal.args()[2]; // castception
                        if (character != 0) // Not a character
                            if (keyboard.initialised()) {
                                keyboard.keyPressed(character);
                                //keyboard.keyPressed(asciiCodeToScanScode(character, false));
                            }

                    } else if (signal.name().equals("key_up")) {
                        byte character = (byte) (double) (Double) signal.args()[2]; // castception
                        if (character != 0) // Not a character
                            if (keyboard.initialised()) {
                                keyboard.keyReleased(character);
                                //keyboard.keyReleased(asciiCodeToScanScode(character, true));
                            }

                    } else {
                        System.out.println("Unhandled signal: " + signal.name() + ", args: " + signal.args());
                    }

                } else
                    break;
            }
        }





        return sleep1;
    }

    @Override
    public void save(NBTTagCompound paramNBTTagCompound) {
        // TODO Auto-generated method stub

    }

    public void markDirty() {
        dirty=true;
    }
}
