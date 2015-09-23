package lperkins2.ocx86.SerialTTY;

import com.jcraft.jcterm.EmulatorVT100;
import com.jcraft.jcterm.Term;

import java.io.InputStream;

/**
 * Created by perkins on 9/22/15.
 */
public class OpenComputersVT100 extends EmulatorVT100 {


    public OpenComputersVT100(Term term, InputStream in) {
        super(term, in);
    }
}
