package net.smileycorp.hordes.infection;

import net.minecraft.util.DamageSource;

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
