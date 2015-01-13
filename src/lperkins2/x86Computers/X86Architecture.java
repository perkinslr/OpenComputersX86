package lperkins2.x86Computers;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.jpc.emulator.PC;
import org.jpc.emulator.peripheral.Keyboard;
import org.jpc.support.Clock;
import org.jpc.support.DriveSet;

import scala.Mutable;
import net.minecraft.nbt.NBTTagCompound;
import li.cil.oc.api.machine.Architecture;
import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Machine;
//import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.Signal;

@Architecture.Name("x86 (i686)")
public class X86Architecture implements Architecture{
    
    private int numberOfCalls=0;
    private OpenComputersX86PC x86pc;
    private OpenComputersClock clock;
    private DriveSet drives;
    private Keyboard keyboard;
    private boolean shouldCrash = false;
    private boolean initialized = false;
    private Machine machine;
    OpenComputersVGACard vgaCard;
    
    
    /** The constructor must have exactly this signature. */
    public X86Architecture(Machine machine) {
        this.machine = machine;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void recomputeMemory() {
        if (x86pc == null){
            return;
        }
        int availableMemory = (machine.host().installedMemory());
        if (availableMemory != x86pc.ramSize){
            x86pc.ramSize = availableMemory;
            if (isInitialized()){
                LogManager.getLogger().info("Should crash on next tick");
                shouldCrash=true;
            }
        }
        
        
    }

    public boolean initialize() {
        // Set up new VM here
        vgaCard = new OpenComputersVGACard();
        vgaCard.resizeDisplay(320, 200);
        shouldCrash = false;
        String[] args = {
                "-fda", "resources/images/floppy.img", "-hda", "resources/images/floppy.img", "-boot", "cdrom", "-cdrom", "Downloads/super_grub2_disk_hybrid_2.00s1-beta6.iso", "-mhz", "75"
        };
        drives = DriveSet.buildFromArgs(args);
        keyboard = new Keyboard();
        try
        {
            x86pc = new OpenComputersX86PC(this.machine, new OpenComputersClock(), keyboard, drives, vgaCard);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        recomputeMemory();
        shouldCrash = false;
        initialized=true;
        return true;
    }

    public void close() {
        initialized=false;
        x86pc = null;
    }
    
    
    public byte asciiCodeToScanScode(int key_code, boolean release){
        byte r = (byte) key_code;
        switch (key_code){

            case(0x0D):  // CR
                r = (byte) 0x1C;
                break;
            case(0x1B):  // ESC
                r = (byte) 0x01;
                break;
            case(0x20):  // Space
                r = (byte) 0x39;
                break;
            case(0x27):  // '
                r = (byte) 0x28;
                break;
            case(0x2A):  // *
                r = (byte) 0x37;
                break;
            case(0x2B):  // +
                r = (byte) 0x4E;
                break;
                
            case(0x2C):  // '
                r = (byte) 0x28;
                break;
                
            case(0x2D):  // -
                r = (byte) 0x0C;
                break;
            case(0X2E):  // .
                r = (byte) 0x34;
                break;
            case(0x2F):  // /
                r = (byte) 0x35;
                break;
            
            // 0-9
            case(0x30):  // 0
                r = (byte) 0x0B;
                break;
            case(0x31):  // 1
                r = (byte) 0x02;
                break;
            case(0x32):  // 2
                r = (byte) 0x03;
                break;
            case(0x33):  // 3
                r = (byte) 0x04;
                break;
            case(0x34):  // 4
                r = (byte) 0x05;
                break;
            case(0x35):  // 5
                r = (byte) 0x06;
                break;
            case(0x36):  // 6
                r = (byte) 0x07;
                break;
            case(0x37):  // 7
                r = (byte) 0x08;
                break;
            case(0x38):  // 8
                r = (byte) 0x09;
                break;
            case(0x39):  // 9
                r = (byte) 0x0A;
                break;
            case(0x3B):  // ;
                r = (byte) 0x27;
                break;
            
            case(0x3C):  // =
                r = (byte) 0x0D;
                break;
            
            case(0x5B):  // [
                r = (byte) 0x1A;
                break;
            case(0x5C):  // \
                r = (byte) 0x2B;
                break;
            case(0x5D):  // ]
                r = (byte) 0x1B;
                break;
            case(0x60):  // `
                r = (byte) 0x29;
                break;
            
            //a-z
            case(0x61):  // a
                r = (byte) 0x1E;
                break;
            case(0x62):  // b
                r = (byte) 0x30;
                break;
            case(0x63):  // c
                r = (byte) 0x2E;
                break;
            case(0x64):  // d
                r = (byte) 0x20;
                break;
            case(0x65):  // e
                r = (byte) 0x12;
                break;
            case(0x66):  // f
                r = (byte) 0x21;
                break;
            case(0x67):  // g
                r = (byte) 0x22;
                break;
            case(0x68):  // h
                r = (byte) 0x23;
                break;
            case(0x69):  // i
                r = (byte) 0x17;
                break;
            case(0x6A):  // j
                r = (byte) 0x24;
                break;
            case(0x6B):  // k
                r = (byte) 0x25;
                break;
            case(0x6C):  // l
                r = (byte) 0x26;
                break;
            case(0x6D):  // m
                r = (byte) 0x32;
                break;
            case(0x6E):  // n
                r = (byte) 0x31;
                break;
            case(0x6F):  // o
                r = (byte) 0x18;
                break;
            case(0x70):  // p
                r = (byte) 0x19;
                break;
            case(0x71):  // q
                r = (byte) 0x10;
                break;
            case(0x72):  // r
                r = (byte) 0x13;
                break;
            case(0x73):  // s
                r = (byte) 0x1F;
                break;
            case(0x74):  // t
                r = (byte) 0x14;
                break;
            case(0x75):  // u
                r = (byte) 0x16;
                break;
            case(0x76):  // v
                r = (byte) 0x2F;
                break;
            case(0x77):  // w
                r = (byte) 0x11;
                break;
            case(0x78):  // x
                r = (byte) 0x2D;
                break;
            case(0x79):  // y
                r = (byte) 0x15;
                break;
            case(0x7A):  // z
                r = (byte) 0x2C;
                break;
            
            case(0x7B):  // {
                r = (byte) 0x1A;
                break;
            case(0x7C):  // |
                r = (byte) 0x2B;
                break;
            case(0x7D):  // }
                r = (byte) 0x1B;
                break;
            case(0x7E):  // ~
                r = (byte) 0x29;
                break;
            case(0x7F):  // DEL
                r = (byte) -0x53;
                break;

                
                
            
            default:
                X86OpenComputers.log.info("Keyboard scan code unknown for "+key_code+", assuming same as input");
                return (byte) key_code;
        }
        if (release){
            if (r > 0){
                return (byte) (r + 0x80);
            }
            else{
                return (byte) (r - 0x80);
            }
        }
        else{
            return r;
        }
    }
    

    public ExecutionResult runThreaded(boolean isSynchronizedReturn) { 
        numberOfCalls++;
        if (shouldCrash){
            return new ExecutionResult.Error("Should crash flaged, hardware change");
        }
        try {
            if (!isSynchronizedReturn) {
                Signal signal = null;
                while (true) {
                    signal = machine.popSignal();
                    if (signal != null) {
                        X86OpenComputers.log.info("Signal: " + signal.name() + ", args: "+signal.args());
                        if (signal.name().equals("key_down")) {
                            byte character = (byte) (double) (Double) signal.args()[2]; // castception
                            if (character != 0) // Not a character
                                if (keyboard.checkInitialised()){
                                    keyboard.keyPressed(character);
                                    //asciiCodeToScanScode(character, false));
                                }
                                else{
                                    X86OpenComputers.log.info("Keyboard not ready");
                                }
                        }
                        else if (signal.name().equals("key_up")) {
                            byte character = (byte) (double) (Double) signal.args()[2]; // castception
                            if (character != 0) // Not a character
                                if (keyboard.initialised()){
                                    keyboard.keyReleased(character);
                                    //keyboard.keyReleased(asciiCodeToScanScode(character, true));
                                }
                                else{
                                    X86OpenComputers.log.info("Keyboard not ready");
                                }
                        }
                        else {
                            X86OpenComputers.log.info("Unhandled signal: " + signal.name() + ", args: "+signal.args());
                        }
                        
                    } else
                        break;
                }
            }
            //X86OpenComputers.log.info("Starting execute");
            try{
                x86pc.execute();
            }
            catch (EscapeContinuation c){
                return new ExecutionResult.Sleep(0);
            }
            //X86OpenComputers.log.info("Ended execute");
            if (0==numberOfCalls%1000){
                return new ExecutionResult.SynchronizedCall();
            }
            return new ExecutionResult.Sleep(0);
        } catch (Throwable t) {
            X86OpenComputers.log.info("Execute failed");
            X86OpenComputers.log.info("Stack Trace: "+joinStackTrace(t));
            
            
                StackTraceElement[] elements = Thread.currentThread().getStackTrace();

                for(int i=0; i<elements.length; i++) {
                    X86OpenComputers.log.info("Execute failed: "+elements[i]);
                }
            
                X86OpenComputers.log.info("Execute failed: "+t);
            
            return new ExecutionResult.Error(t.toString());
        }
    }

    public void runSynchronized() {
        try {
            this.vgaCard.render(this.machine);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void onConnect() {
    }

    // TODO: Needs more things
    public void load(NBTTagCompound nbt) {

    }

    // TODO: Needs more things
    public void save(NBTTagCompound nbt) {
        
    }
    
    public static String joinStackTrace(Throwable e) {
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            joinStackTrace(e, writer);
            return writer.toString();
        }
        finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                    // ignore
                }
        }
    }

    public static void joinStackTrace(Throwable e, StringWriter writer) {
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(writer);

            while (e != null) {

                printer.println(e);
                StackTraceElement[] trace = e.getStackTrace();
                for (int i = 0; i < trace.length; i++)
                    printer.println("\tat " + trace[i]);

                e = e.getCause();
                if (e != null)
                    printer.println("Caused by:\r\n");
            }
        }
        finally {
            if (printer != null)
                printer.close();
        }
    }
    
}
