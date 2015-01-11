package lperkins2.x86Computers;

import org.jpc.emulator.PC;
import org.jpc.emulator.memory.AddressSpace;
import org.jpc.emulator.memory.LazyCodeBlockMemory;
import org.jpc.emulator.memory.Memory;
import org.jpc.emulator.memory.PhysicalAddressSpace;
import org.jpc.emulator.memory.PhysicalAddressSpace.UnconnectedMemoryBlock;
import org.jpc.emulator.memory.codeblock.CodeBlockManager;

public class OpenComputersPhysicalAddressSpace extends PhysicalAddressSpace {
    public CodeBlockManager manager;
    public int QUICK_INDEX_SIZE;
    private final Memory UNCONNECTED = new UnconnectedMemoryBlock();
    OpenComputersX86PC pc;
    public OpenComputersPhysicalAddressSpace(CodeBlockManager manager, OpenComputersX86PC pc)
    {
        super(manager);
        this.pc = pc;
    }
    private void initialiseMemory()
    {
        for (int i = 0; i < this.pc.ramSize; i += AddressSpace.BLOCK_SIZE) {
            mapMemory(i, new LazyCodeBlockMemory(AddressSpace.BLOCK_SIZE, manager));
        }
        for (int i = 0; i < 32; i++)
        {
            mapMemory(0xd0000 + i * AddressSpace.BLOCK_SIZE, new PhysicalAddressSpace.UnconnectedMemoryBlock());
        }
    }
}
