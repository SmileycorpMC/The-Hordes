package net.smileycorp.hordes.common.infection;

import net.minecraft.world.damagesource.DamageSource;

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

}
