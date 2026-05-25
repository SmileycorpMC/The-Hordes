package net.smileycorp.hordes.infection.capability;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import org.jetbrains.annotations.Nullable;

public interface Infection {
	
	int getInfectionCount();
	
	void increaseInfection();
	
	class Impl implements Infection {

		private final Entity entity;

		public Impl(Entity entity) {
			this.entity = entity;
		}

		@Override
		public int getInfectionCount() {
			return entity.getData(HordesCapabilities.INFECTION_COUNT);
		}
		
		@Override
		public void increaseInfection() {
			entity.setData(HordesCapabilities.INFECTION_COUNT, getInfectionCount() + 1);
		}
		
	}

	class Provider implements ICapabilityProvider<Entity, Void, Infection> {

		private Infection instance;

		@Override
		public @Nullable Infection getCapability(Entity entity, Void unused) {
			if (!(entity instanceof LivingEntity)) return null;
			if (instance == null) instance = new Impl(entity);
			return instance;
		}

	}
	
}