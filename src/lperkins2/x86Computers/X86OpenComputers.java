package lperkins2.x86Computers;

import java.lang.reflect.Method;

import li.cil.oc.api.Machine;
import cpw.mods.fml.common.Mod;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;


@Mod(modid = X86OpenComputers.MODID, name = X86OpenComputers.NAME, version = X86OpenComputers.VERSION, dependencies = "required-after:OpenComputers@[1.4.0,)")
public class X86OpenComputers {
    public static final String MODID = "ocx86";
    public static final String NAME = "OC X86 Architecture";
    public static final String VERSION = "0.0.1";
    public static Logger log;
    public static ItemX86Processor cpuX86Processor;
    public static DriverX86Processor cpuX86Driver;
    
    
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        cpuX86Processor = new ItemX86Processor();
        cpuX86Driver = new DriverX86Processor();
        GameRegistry.registerItem(cpuX86Processor, "x86CPU");
        
        
        
        
        
    }
    
    @EventHandler
    public void init(FMLInitializationEvent e) {
        li.cil.oc.api.Driver.add(cpuX86Driver);
        Machine.add(X86Architecture.class);
    }
    
    
}
