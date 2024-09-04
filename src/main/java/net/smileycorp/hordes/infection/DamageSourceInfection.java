package net.smileycorp.hordes.infection;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.capability.ZombifyPlayer;

public class DamageSourceInfection extends DamageSource {

	public DamageSourceInfection() {
		super("infection");
	}

	@Override
	public boolean isBypassArmor() {
		return true;
	}

	@Override
	public boolean isBypassInvul() {
		return true;
	}
	
	@Override
	public ITextComponent getLocalizedDeathMessage(LivingEntity entity) {
		if (!(entity instanceof PlayerEntity)) return super.getLocalizedDeathMessage(entity);
		String msg = "death.attack.infection";
		LazyOptional<ZombifyPlayer> optional = entity.getCapability(HordesCapabilities.ZOMBIFY_PLAYER);
		if (optional.isPresent() && optional.orElseGet(null).wasZombified()) msg += ".zombified";
		return new TranslationTextComponent(msg, entity.getDisplayName());
	}
	
}
