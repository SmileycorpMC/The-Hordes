package net.smileycorp.hordes.infection;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.config.data.infection.InfectionDataLoader;
import net.smileycorp.hordes.infection.network.InfectMessage;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;

import java.util.List;
import java.util.UUID;

public class PotionInfected extends PotionHordes {
	
	private final UUID SPEED_MOD_UUID = UUID.fromString("05d68949-cb8b-4031-92a6-bd75e42b5cdd");
	private final String SPEED_MOD_NAME = Constants.name("Infected");
	private final AttributeModifier SPEED_MOD = new AttributeModifier(SPEED_MOD_NAME, -0.1f, 2);
	
	public PotionInfected() {
		super("Infected", true, 0x00440002);
	}

    @Override
	public List<ItemStack> getCurativeItems() {
    	return InfectionConfig.enableMobInfection ? InfectionDataLoader.INSTANCE.getCureList() : super.getCurativeItems();
    }
    
    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
    	if (entity instanceof EntityPlayer) ((EntityPlayer)entity).addExhaustion(0.03F * (amplifier+1));
    }
    
    @Override
    public boolean isReady(int duration, int amplifier) {
    	return InfectionConfig.infectHunger;
    }
    
    @Override
	public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        if (amplifier < 0 |! InfectionConfig.infectSlowness) return;
        IAttributeInstance attribute = map.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (attribute == null) return;
        attribute.removeModifier(SPEED_MOD_UUID);
        attribute.applyModifier(new AttributeModifier(SPEED_MOD_UUID, SPEED_MOD_NAME + " " + amplifier, this.getAttributeModifierAmount(amplifier - 1, SPEED_MOD), 2));
    }
    
    @Override
	public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
    	IAttributeInstance attribute = map.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
    	if (attribute != null) attribute.removeModifier(SPEED_MOD_UUID);
    }
    
    public static void apply(EntityLivingBase entity) {
        boolean prevented = preventInfection(entity);
        if (entity instanceof EntityPlayerMP) InfectionPacketHandler.sendTo(new InfectMessage(prevented), ((EntityPlayerMP) entity));
        if (!prevented) entity.addPotionEffect(new PotionEffect(HordesInfection.INFECTED, getInfectionTime(entity)));
    }
    
    public static boolean preventInfection(EntityLivingBase entity) {
        return entity.isPotionActive(HordesInfection.IMMUNITY);
    }
    
    public static int getInfectionTime(EntityLivingBase entity) {
        int time = InfectionConfig.ticksForEffectStage;
        if (entity.hasCapability(HordesCapabilities.INFECTION, null)) time = (int)((double)time
                * Math.pow(InfectionConfig.effectStageTickReduction, entity.getCapability(HordesCapabilities.INFECTION, null).getInfectionCount()));
        return time;
    }

}
