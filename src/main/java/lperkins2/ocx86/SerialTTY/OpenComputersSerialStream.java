package lperkins2.ocx86.SerialTTY;

import lperkins2.ocx86.X86PC;
import org.jpc.emulator.peripheral.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;

/**
 * Created by perkins on 9/22/15.
 */
public class OpenComputersSerialStream extends InputStream {
    private final X86PC pc;
    public ArrayDeque<String> buffer;
    public SerialPort serialPort;
    public byte[] currentBuffer;
    public int bptr = 0;
    public OpenComputersSerialStream(SerialPort sp, X86PC pc){
        this.pc = pc;
        this.serialPort = sp;
        this.buffer = new ArrayDeque<String>();
    }
    @Override
    public int read() throws IOException {
        if (currentBuffer==null || currentBuffer.length==bptr){
            if (buffer.size()==0){
                throw new IOException("Buffer empty");
            }
            bptr = 0;
            currentBuffer = buffer.pop().getBytes();
        }
        return currentBuffer[bptr++];
    }


    public void append(String data) {
        pc.markDirty();
        this.buffer.push(data);
    }
}
