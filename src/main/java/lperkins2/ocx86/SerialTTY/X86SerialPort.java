package lperkins2.ocx86.SerialTTY;

import lperkins2.ocx86.X86PC;
import org.jpc.emulator.peripheral.SerialPort;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Created by perkins on 9/22/15.
 */
public class X86SerialPort extends SerialPort {

    public PipedInputStream inputStream;
    public PipedOutputStream outputStream;

    public X86SerialPort(int portNumber, X86PC pc) {
        super(portNumber);
        this.inputStream = new PipedInputStream();
        try {
            this.outputStream = new PipedOutputStream(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void print(String data)
    {
        System.out.println("Got serial "+data);
        try {
            outputStream.write(data.getBytes());
        } catch (IOException e) {
            System.out.println("Cannot write serial out");
        }

    }
}
