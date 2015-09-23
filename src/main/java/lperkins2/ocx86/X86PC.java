package lperkins2.ocx86;

import lperkins2.ocx86.SerialTTY.X86SerialPort;
import org.jpc.emulator.HardwareComponent;
import org.jpc.emulator.PC;
import org.jpc.emulator.execution.codeblock.CodeBlockManager;
import org.jpc.emulator.memory.LinearAddressSpace;
import org.jpc.emulator.memory.PhysicalAddressSpace;
import org.jpc.emulator.motherboard.*;
import org.jpc.emulator.pci.PCIBus;
import org.jpc.emulator.pci.PCIHostBridge;
import org.jpc.emulator.pci.PCIISABridge;
import org.jpc.emulator.pci.peripheral.EthernetCard;
import org.jpc.emulator.pci.peripheral.PIIX3IDEInterface;
import org.jpc.emulator.peripheral.*;
import org.jpc.emulator.processor.Processor;
import org.jpc.j2se.Option;
import org.jpc.j2se.VirtualClock;
import org.jpc.support.Clock;
import org.jpc.support.DriveSet;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;

public class X86PC extends PC {

    private final OpenComputersX86Architecture arch;
    public X86SerialPort x86SerialPort;

    public X86PC(OpenComputersX86Architecture arch) throws IOException {
		super();
        this.arch = arch;
		this.initialize(new VirtualClock(), DriveSet.buildFromArgs(new String[]{"-boot","cdrom", "-cdrom", "/home/perkins/scr/grub.iso"}), DEFAULT_RAM_SIZE, getStartTime());
	}
	public void initialize(Clock clock, DriveSet drives, int ramSize, Calendar startTime) throws IOException{
        SYS_RAM_SIZE = ramSize;
        parts = new LinkedList<HardwareComponent>();

        vmClock = clock;
        parts.add(vmClock);
        processor = new Processor(vmClock);
        parts.add(processor);
        manager = new CodeBlockManager();

        physicalAddr = new PhysicalAddressSpace(manager);

        parts.add(physicalAddr);

        linearAddr = new LinearAddressSpace();
        parts.add(linearAddr);

        parts.add(drives);

        //Motherboard

        parts.add(new IOPortHandler());
        pic = new InterruptController();
        parts.add(pic);

        parts.add(new DMAController(false, true));
        parts.add(new DMAController(false, false));

        parts.add(new RTC(0x70, 8, startTime));
        parts.add(new IntervalTimer(0x40, 0));
        parts.add(new GateA20Handler());

        //Peripherals
        parts.add(new PIIX3IDEInterface());
        if (Option.ethernet.isSet())
            parts.add(ethernet = new EthernetCard());
        parts.add(new X86VGACard());
        x86SerialPort = new X86SerialPort(0, this);
        parts.add(x86SerialPort);
        parts.add(new SerialPort(1));
        parts.add(new SerialPort(2));
        parts.add(new SerialPort(3));
        keyboard = new Keyboard();
        parts.add(keyboard);
        parts.add(new FloppyController());
        parts.add(new PCSpeaker());

        //PCI Stuff
        parts.add(new PCIHostBridge());
        parts.add(new PCIISABridge());
        parts.add(new PCIBus());

        if (Option.ethernet.isSet())
            parts.add(new EthernetCard());

        //BIOSes
        parts.add(new SystemBIOS(Option.bios.value("/resources/bios/bios.bin")));
        parts.add(new VGABIOS("/resources/bios/vgabios.bin"));

        if (Option.sound.value())
        {
            Midi.MIDI_Init();
            Mixer.MIXER_Init();
            String device = Option.sounddevice.value("sb16");
            if (device.equals("sb16"))
            {
                parts.add(new Mixer());
                parts.add(new MPU401());
                parts.add(new SBlaster());
                parts.add(new Adlib());
            }
        }

        if (!configure()) {
            throw new IllegalStateException("PC Configuration failed");
        }
    }

    public void markDirty() {
        arch.markDirty();
    }
}
