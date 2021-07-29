package net.smileycorp.hordes.infection;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.smileycorp.hordes.common.ConfigHandler;
import net.smileycorp.hordes.common.ModDefinitions;

public class PotionInfected extends Potion {
	
	public static ResourceLocation texture = new ResourceLocation(ModDefinitions.modid, "textures/gui/potions.png");
	
	public PotionInfected() {
		super(true, 0x00440002);
		String name = "Infected";
		setPotionName("effect." + ModDefinitions.getName(name));
		setRegistryName(ModDefinitions.getResource(name));
		setIconIndex(0, 0);
	}
	
	@Override
    public boolean shouldRender(PotionEffect effect) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getStatusIconIndex() {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        return super.getStatusIconIndex();
    }

    @Override
	public List<ItemStack> getCurativeItems() {
    	return ConfigHandler.enableMobInfection ? InfectionRegister.getCureList() : super.getCurativeItems();
    }

}
