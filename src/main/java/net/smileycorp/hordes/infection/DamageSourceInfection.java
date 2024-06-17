package net.smileycorp.hordes.infection;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.smileycorp.hordes.common.capability.HordesCapabilities;

public class DamageSourceInfection extends DamageSource {

	public DamageSourceInfection() {
		super("infection");
	}
	
	@Override
	public boolean isDamageAbsolute() {
        return true;
    }
	
	@Override
	public boolean isUnblockable() {
        return true;
    }
	
	@Override
	public ITextComponent getDeathMessage(EntityLivingBase entity) {
		if (!(entity instanceof EntityPlayer)) return super.getDeathMessage(entity);
		String msg = "death.attack.infection";
		if (entity.hasCapability(HordesCapabilities.ZOMBIFY_PLAYER, null)
				&& entity.getCapability(HordesCapabilities.ZOMBIFY_PLAYER, null).wasZombified()) msg += ".zombified";
		return new TextComponentTranslation(msg, entity.getDisplayName());
	}

}
