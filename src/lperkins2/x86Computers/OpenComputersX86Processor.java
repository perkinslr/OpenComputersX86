package lperkins2.x86Computers;

import org.jpc.emulator.processor.Processor;
import org.jpc.support.Clock;
import lperkins2.x86Computers.EscapeContinuation;


public class OpenComputersX86Processor extends Processor{
    private OpenComputersX86PC pc;
    //public static final int IPS = 7500000; //CPU "Clock Speed" in instructions per second

    public OpenComputersX86Processor(OpenComputersX86PC pc, Clock clock)
    {
        super(clock);
        this.pc = pc;
        // TODO Auto-generated constructor stub
    }
    
    public boolean checkForInterrupt(){
        return ((interruptFlags & IFLAGS_HARDWARE_INTERRUPT) != 0);
    }
    
    public void waitForInterrupt() throws EscapeContinuation
    {
        if ((interruptFlags & IFLAGS_HARDWARE_INTERRUPT) == 0){
            pc.waitForInterrupt=true;
            throw (new EscapeContinuation("waiting for interrupt"));
        }
        //System.out.println("*****START HALT");
        resumeAfterInterrupt();
        return;
        
    }
    public void resumeAfterInterrupt(){
        if (isProtectedMode()) {
            if (isVirtual8086Mode()) {
                processProtectedModeInterrupts(0);
            } else {
                processProtectedModeInterrupts(0);
            }
        } else {
            processRealModeInterrupts(0);
        }
    }
        
        
        
}
