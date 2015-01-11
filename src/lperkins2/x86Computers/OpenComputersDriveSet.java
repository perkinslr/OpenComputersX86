package lperkins2.x86Computers;

import org.jpc.support.BlockDevice;
import org.jpc.support.DriveSet;
import org.jpc.support.DriveSet.BootType;

public class OpenComputersDriveSet extends DriveSet{

    public OpenComputersDriveSet(BootType boot, BlockDevice floppyDrive, BlockDevice hardDrive)
    {
        super(boot, floppyDrive, hardDrive);
        // TODO Auto-generated constructor stub
    }

}
