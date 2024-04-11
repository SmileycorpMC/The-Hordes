package net.smileycorp.hordes.infection;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
	public Component getLocalizedDeathMessage(LivingEntity entity) {
		if (!(entity instanceof Player)) return super.getLocalizedDeathMessage(entity);
		String msg = "death.attack.infection";
		LazyOptional<ZombifyPlayer> optional = entity.getCapability(HordesCapabilities.ZOMBIFY_PLAYER);
		if (optional.isPresent() && optional.orElseGet(null).wasZombified()) msg += ".zombified";
		return new TranslatableComponent(msg, entity.getDisplayName());
	}
	
}
