package lperkins2.x86Computers;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.jpc.emulator.HardwareComponent;
import org.jpc.emulator.PC;
import org.jpc.emulator.Timer;
import org.jpc.emulator.TimerResponsive;
import org.jpc.emulator.processor.Processor;
import org.jpc.j2se.VirtualClock;
import org.jpc.support.Clock;

public class OpenComputersClock extends VirtualClock{
    public static final long IPS = Processor.IPS;
    public static final long NSPI = 10*1000000000L/IPS; //Nano seconds per instruction
    
}
