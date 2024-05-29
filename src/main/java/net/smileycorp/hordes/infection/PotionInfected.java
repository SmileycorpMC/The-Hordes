package net.smileycorp.hordes.infection;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.InfectionConfig;

import java.util.List;
import java.util.UUID;

public class PotionInfected extends Potion {
	
	public static final ResourceLocation TEXTURE = Constants.loc("textures/gui/potions.png");
	
	private final UUID SPEED_MOD_UUID = UUID.fromString("05d68949-cb8b-4031-92a6-bd75e42b5cdd");
	private final String SPEED_MOD_NAME = Constants.name("Infected");
	private final AttributeModifier SPEED_MOD = new AttributeModifier(SPEED_MOD_NAME, -0.1f, 2);
	
	public PotionInfected() {
		super(true, 0x00440002);
		String name = "Infected";
		setPotionName("effect." + Constants.name(name));
		setRegistryName(Constants.loc(name));
		setIconIndex(0, 0);
	}
	
	@Override
    public boolean shouldRender(PotionEffect effect) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getStatusIconIndex() {
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
        return super.getStatusIconIndex();
    }

    @Override
	public List<ItemStack> getCurativeItems() {
    	return InfectionConfig.enableMobInfection ? InfectionRegister.getCureList() : super.getCurativeItems();
    }
    
    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
    	if (entity instanceof EntityPlayer) {
            ((EntityPlayer)entity).addExhaustion(0.007F * (amplifier+1));
        }
    }
    
    @Override
    public boolean isReady(int duration, int amplifier) {
    	return InfectionConfig.infectHunger;
    }
    
    @Override
	public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        if (amplifier > 0 && InfectionConfig.infectSlowness) {
        	IAttributeInstance attribute = map.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
        	if (attribute != null) {
        		attribute.removeModifier(SPEED_MOD_UUID);
        		attribute.applyModifier(new AttributeModifier(SPEED_MOD_UUID, SPEED_MOD_NAME + " " + amplifier, this.getAttributeModifierAmount(amplifier-1, SPEED_MOD), 2));
            }
        }
    }
    
    @Override
	public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
    	IAttributeInstance attribute = map.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
    	if (attribute != null) attribute.removeModifier(SPEED_MOD_UUID);
    }

}
