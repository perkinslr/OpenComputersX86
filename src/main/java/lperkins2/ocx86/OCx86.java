package lperkins2.ocx86;
import li.cil.oc.api.Machine;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;


@Mod(modid = "OCx86", name = "OpenComputers x86", version = "0.1.0", dependencies = "required-after:OpenComputers@[1.4.0,)")
public class OCx86 {
	
	@EventHandler
    public void preInit(FMLPreInitializationEvent event) {
	}

	@EventHandler
    public void init(FMLInitializationEvent e) {
		Machine.add(OpenComputersX86Architecture.class);
	}
	
}
