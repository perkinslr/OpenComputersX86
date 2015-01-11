package lperkins2.x86Computers;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemX86Processor extends Item{
    public ItemX86Processor() {
        super();
        setUnlocalizedName("cpuX86");
        setCreativeTab(li.cil.oc.api.CreativeTab.instance);
    }
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister) {
        this.itemIcon = par1IconRegister.registerIcon(X86OpenComputers.MODID + ":" + this.getUnlocalizedName().substring(5));
    }
}
