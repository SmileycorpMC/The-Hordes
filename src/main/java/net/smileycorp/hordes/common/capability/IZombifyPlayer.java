package net.smileycorp.hordes.common.capability;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public interface IZombifyPlayer {

	Mob createZombie(Player player);

	Mob getZombie();

	void clearZombie();

	boolean wasZombified();

	class Provider implements ICapabilityProvider {

		protected final IZombifyPlayer impl = new ZombifyPlayer();

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
			return capability == HordesCapabilities.ZOMBIFY_PLAYER ? LazyOptional.of(() -> impl).cast() : LazyOptional.empty();
		}

	}

}
