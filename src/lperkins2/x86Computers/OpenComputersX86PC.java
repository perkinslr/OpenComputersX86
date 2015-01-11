package lperkins2.x86Computers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jpc.emulator.HardwareComponent;
import org.jpc.emulator.PC;
import org.jpc.emulator.memory.LinearAddressSpace;
import org.jpc.emulator.memory.PhysicalAddressSpace;
import org.jpc.emulator.memory.codeblock.CodeBlockManager;
import org.jpc.emulator.motherboard.DMAController;
import org.jpc.emulator.motherboard.GateA20Handler;
import org.jpc.emulator.motherboard.IOPortCapable;
import org.jpc.emulator.motherboard.IOPortHandler;
import org.jpc.emulator.motherboard.InterruptController;
import org.jpc.emulator.motherboard.IntervalTimer;
import org.jpc.emulator.motherboard.RTC;
import org.jpc.emulator.motherboard.SystemBIOS;
import org.jpc.emulator.motherboard.VGABIOS;
import org.jpc.emulator.pci.PCIBus;
import org.jpc.emulator.pci.PCIHostBridge;
import org.jpc.emulator.pci.PCIISABridge;
import org.jpc.emulator.pci.peripheral.DefaultVGACard;
import org.jpc.emulator.pci.peripheral.EthernetCard;
import org.jpc.emulator.pci.peripheral.PIIX3IDEInterface;
import org.jpc.emulator.pci.peripheral.VGACard;
import org.jpc.emulator.peripheral.FloppyController;
import org.jpc.emulator.peripheral.Keyboard;
import org.jpc.emulator.peripheral.PCSpeaker;
import org.jpc.emulator.peripheral.SerialPort;
import org.jpc.emulator.processor.ModeSwitchException;
import org.jpc.emulator.processor.Processor;
import org.jpc.emulator.processor.ProcessorException;
import org.jpc.j2se.VirtualClock;
import org.jpc.support.ArgProcessor;
import org.jpc.support.Clock;
import org.jpc.support.DriveSet;

public class OpenComputersX86PC{
    public int INSTRUCTIONS_BETWEEN_INTERRUPTS = 1;
    public boolean compile = true;
    public int ramSize;
    private static Logger LOGGING = X86OpenComputers.log;
    private Processor processor;
    private PhysicalAddressSpace physicalAddr;
    private LinearAddressSpace linearAddr;
    private Clock vmClock;
    private Set<HardwareComponent> parts;
    private CodeBlockManager manager;
    private EthernetCard ethernet;
    
    
    
    
    public OpenComputersX86PC(OpenComputersClock clock, Keyboard keyboard, DriveSet drives, OpenComputersVGACard vgaCard) throws IOException
    {
        //super(clock, drives);
        vgaCard.resizeDisplay(640,480);
        this.vmClock=clock;
        parts = new HashSet<HardwareComponent>();
        parts.add(vmClock);
        processor = new OpenComputersX86Processor(vmClock);
        parts.add(processor);
        manager = new CodeBlockManager();
        physicalAddr = new OpenComputersPhysicalAddressSpace(manager, this);
        parts.add(physicalAddr);
        linearAddr = new LinearAddressSpace();
        parts.add(linearAddr);

        parts.add(drives);
        
        parts.add(new IOPortHandler());
        parts.add(new InterruptController());

        parts.add(new DMAController(false, true));
        parts.add(new DMAController(false, false));

        parts.add(new RTC(0x70, 8));
        parts.add(new IntervalTimer(0x40, 0));
        parts.add(new GateA20Handler());

        //Peripherals
        parts.add(new PIIX3IDEInterface());
        parts.add(ethernet = new EthernetCard());
        vgaCard.setMonitor(new OpenComputersX86Monitor(this, vgaCard));
        parts.add(vgaCard);

        parts.add(new SerialPort(0));
        parts.add(keyboard);
        parts.add(new FloppyController());
        //parts.add(new PCSpeaker());

        //PCI Stuff
        parts.add(new PCIHostBridge());
        parts.add(new PCIISABridge());
        parts.add(new PCIBus());

        //BIOSes
        
        
        parts.add(new SystemBIOS("/resources/bios/bios.bin"));
        parts.add(new VGABIOS("/resources/bios/vgabios.bin"));

        if (!configure()) {
            throw new IllegalStateException("PC Configuration failed");
        }

        
    }
    
    public void changeFloppyDisk(org.jpc.support.BlockDevice disk, int index) {
        ((FloppyController) getComponent(FloppyController.class)).changeDisk(disk, index);
    }
    
    private boolean configure() {
        boolean fullyInitialised;
        int count = 0;
        do {
            fullyInitialised = true;
            for (HardwareComponent outer : parts) {
                
                if (outer.initialised()) {
                    continue;
                }
                
                for (HardwareComponent inner : parts) {
                    //LogManager.getLogger().info("About to try "+inner+" in "+outer);
                    outer.acceptComponent(inner);
                    //LogManager.getLogger().info("Trying component: "+outer+", initialized: "+outer.initialised());
                }
                //LogManager.getLogger().info("Tried component: "+outer+", initialized: "+outer.initialised());
                

                fullyInitialised &= outer.initialised();
            }
            count++;
        } while ((fullyInitialised == false) && (count < 100));

        if (!fullyInitialised) {
            StringBuilder sb = new StringBuilder("pc >> component configuration errors\n");
            List<HardwareComponent> args = new ArrayList<HardwareComponent>();
            for (HardwareComponent hwc : parts) {
                if (!hwc.initialised()) {
                    sb.append("component {" + hwc + "} not configured\n");
                    args.add(hwc);
                }
            }

            LOGGING.log(Level.WARN, sb.toString(), args.toArray());
            return false;
        }

        for (HardwareComponent hwc : parts) {
            if (hwc instanceof PCIBus) {
                ((PCIBus) hwc).biosInit();
            }
        }

        return true;
    }
    
    
    public void saveState(OutputStream out) throws IOException {
        LOGGING.log(Level.INFO, "snapshot saving");
        ZipOutputStream zout = new ZipOutputStream(out);
        for (HardwareComponent hwc : parts) {
            saveComponent(zout, hwc);
        }

        zout.finish();
        LOGGING.log(Level.INFO, "snapshot done");
    }

    private void saveComponent(ZipOutputStream zip, HardwareComponent component) throws IOException {
        LOGGING.log(Level.DEBUG, "snapshot saving {0}", component);
        int i = 0;
        while (true) {
            ZipEntry entry = new ZipEntry(component.getClass().getName() + "#" + i);
            try {
                zip.putNextEntry(entry);
                break;
            } catch (ZipException e) {
                if (e.getMessage().matches(".*(duplicate entry).*")) {
                    i++;
                } else {
                    throw e;
                }
            }
        }

        DataOutputStream dout = new DataOutputStream(zip);
        component.saveState(dout);
        dout.flush();
        zip.closeEntry();
    }
    
    public void loadState(InputStream in) throws IOException {
        LOGGING.log(Level.INFO, "snapshot loading");
        physicalAddr.reset();
        ZipInputStream zin = new ZipInputStream(in);
        Set<HardwareComponent> newParts = new HashSet<HardwareComponent>();
        IOPortHandler ioHandler = (IOPortHandler) getComponent(IOPortHandler.class);
        ioHandler.reset();
        newParts.add(ioHandler);
        try {
            for (ZipEntry entry = zin.getNextEntry(); entry != null; entry = zin.getNextEntry()) {
                DataInputStream din = new DataInputStream(zin);

                String cls = entry.getName().split("#")[0];
                Class clz;
                try {
                    clz = Class.forName(cls);
                } catch (ClassNotFoundException e) {
                    LOGGING.log(Level.WARN, "unknown class in snapshot", e);
                    continue;
                }
                HardwareComponent hwc = getComponent(clz);
                if (hwc instanceof PIIX3IDEInterface) {
                    ((PIIX3IDEInterface) hwc).loadIOPorts(ioHandler, din);
                } else if (hwc instanceof EthernetCard) {
                    ((EthernetCard) hwc).loadIOPorts(ioHandler, din);
                } 
//                Todo: support OpenComputersClock
//                else if (hwc instanceof VirtualClock) {
//                    ((VirtualClock) hwc).loadState(din, this);
//                } 
                else if (hwc instanceof PhysicalAddressSpace) {
                    ((PhysicalAddressSpace) hwc).loadState(din, manager);
                } else {
                    hwc.loadState(din);
                }

                if (hwc instanceof IOPortCapable) {
                    ioHandler.registerIOPortCapable((IOPortCapable) hwc);
                }

                parts.remove(hwc);
                newParts.add(hwc);
            }

            parts.clear();
            parts.addAll(newParts);

            linkComponents();
            LOGGING.log(Level.INFO, "snapshot load done");
        //pciBus.biosInit();
        } catch (IOException e) {
            LOGGING.log(Level.WARN, "snapshot load failed", e);
            throw e;
        }
    }

    private void linkComponents() {
        boolean fullyInitialised;
        int count = 0;

        do {
            fullyInitialised = true;
            for (HardwareComponent outer : parts) {
                if (outer.updated()) {
                    continue;
                }

                for (HardwareComponent inner : parts) {
                    outer.updateComponent(inner);
                }

                fullyInitialised &= outer.updated();
            }
            count++;
        } while ((fullyInitialised == false) && (count < 100));

        if (!fullyInitialised) {
            StringBuilder sb = new StringBuilder("pc >> component linking errors\n");
            List<HardwareComponent> args = new ArrayList<HardwareComponent>();
            for (HardwareComponent hwc : parts) {
                if (!hwc.initialised()) {
                    sb.append("component {" + args.size() + "} not linked");
                    args.add(hwc);
                }
            }
            LOGGING.log(Level.WARN, sb.toString(), args.toArray());
        }
    }

    /**
     * Reset this PC back to its initial state.
     * <p>
     * This is roughly equivalent to a hard-reset (power down-up cycle).
     */
    public void reset() {
        for (HardwareComponent hwc : parts) {
            hwc.reset();
        }
        configure();
    }

    /**
     * Get an subclass of <code>cls</code> from this instance's parts list.
     * <p>
     * If <code>cls</code> is not assignment compatible with <code>HardwareComponent</code>
     * then this method will return null immediately.
     * @param cls component type required.
     * @return an instance of class <code>cls</code>, or <code>null</code> on failure
     */
    public HardwareComponent getComponent(Class<? extends HardwareComponent> cls) {
        if (!HardwareComponent.class.isAssignableFrom(cls)) {
            return null;
        }

        for (HardwareComponent hwc : parts) {
            if (cls.isInstance(hwc)) {
                return hwc;
            }
        }
        return null;
    }

    /**
     * Gets the processor instance associated with this PC.
     * @return associated processor instance.
     */
    public Processor getProcessor() {
        return processor;
    }

    /**
     * Execute an arbitrarily large amount of code on this instance.
     * <p>
     * This method will execute continuously until there is either a mode switch,
     * or a unspecified large number of instructions have completed.  It should 
     * never run indefinitely.
     * @return total number of x86 instructions executed.
     */
    public final int execute() {
        
        if (processor.isProtectedMode()) {
            if (processor.isVirtual8086Mode()) {
                return executeVirtual8086();
            } else {
                return executeProtected();
            }
        } else {
            return executeReal();
        }
    }

    public final int executeReal()
    {
        int x86Count = 0;
        int clockx86Count = 0;
        int nextClockCheck = INSTRUCTIONS_BETWEEN_INTERRUPTS;
        try
        {
            for (int i = 0; i < 100; i++)
            {
                ethernet.checkForPackets();
                int block = physicalAddr.executeReal(processor, processor.getInstructionPointer());
                x86Count += block;
                clockx86Count += block;
                if (x86Count > nextClockCheck)
                {
                    nextClockCheck = x86Count + INSTRUCTIONS_BETWEEN_INTERRUPTS;
                    processor.processRealModeInterrupts(clockx86Count);
                    clockx86Count = 0;
                }
            }
        } catch (ProcessorException p) {
             processor.handleRealModeException(p);
        }
        catch (ModeSwitchException e)
        {
            LOGGING.log(Level.DEBUG, "Switching mode", e);
        }
        return x86Count;
    }

    public final int executeProtected() {
        int x86Count = 0;
        int clockx86Count = 0;
        int nextClockCheck = INSTRUCTIONS_BETWEEN_INTERRUPTS;
        try
        {
            for (int i = 0; i < 100; i++)
            {
                int block= linearAddr.executeProtected(processor, processor.getInstructionPointer());
                x86Count += block;
                clockx86Count += block;
                if (x86Count > nextClockCheck)
                {
                    nextClockCheck = x86Count + INSTRUCTIONS_BETWEEN_INTERRUPTS;
                    ethernet.checkForPackets();
                    processor.processProtectedModeInterrupts(clockx86Count);
                    clockx86Count = 0;
                }
            }
        } catch (ProcessorException p) {
                processor.handleProtectedModeException(p);
        }
        catch (ModeSwitchException e)
        {
            LOGGING.log(Level.DEBUG, "Switching mode", e);
        }
        return x86Count;
    }

    public final int executeVirtual8086() {
        int x86Count = 0;
        int clockx86Count = 0;
        int nextClockCheck = INSTRUCTIONS_BETWEEN_INTERRUPTS;
        try
        {
            for (int i = 0; i < 100; i++)
            {
                int block = linearAddr.executeVirtual8086(processor, processor.getInstructionPointer());
                x86Count += block;
                clockx86Count += block;
                if (x86Count > nextClockCheck)
                {
                    nextClockCheck = x86Count + INSTRUCTIONS_BETWEEN_INTERRUPTS;
                    ethernet.checkForPackets();
                    processor.processVirtual8086ModeInterrupts(clockx86Count);
                    clockx86Count = 0;
                }
            }
        }
        catch (ProcessorException p)
        {
            processor.handleVirtual8086ModeException(p);
        }
        catch (ModeSwitchException e)
        {
            LOGGING.log(Level.DEBUG, "Switching mode", e);
        }
        return x86Count;
    }
    
    

}
