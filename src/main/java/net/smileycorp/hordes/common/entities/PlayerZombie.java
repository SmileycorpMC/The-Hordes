package net.smileycorp.hordes.common.entities;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PlayerZombie<T extends MobEntity & PlayerZombie<T>> {

	void setPlayer(PlayerEntity player);

	void setPlayer(String username);

	void setPlayer(UUID uuid);

	void setPlayer(GameProfile profile);

	Optional<UUID> getPlayerUUID();

	void storeDrops(Collection<ItemEntity> drops);

	void setInventory(NonNullList<ItemStack> drops);

	NonNullList<ItemStack> getInventory();

	void copyFrom(PlayerZombie entity);

	void setDisplayCape(boolean display);

	boolean displayCape();

	double getXCloakO();

	double getYCloakO();

	double getZCloakO();

	double getXCloak();

	double getYCloak();

	double getZCloak();

	void setXCloakO(double value);

	void setYCloakO(double value);

	void setZCloakO(double value);

	void setXCloak(double value);

	void setYCloak(double value);

	void setZCloak(double value);

	default void moveCloak(ZombieEntity entity) {
		setXCloakO(getXCloak());
		setYCloakO(getYCloak());
		setZCloakO(getZCloak());
		double d0 = entity.getX() - getXCloak();
		double d1 = entity.getY() - getYCloak();
		double d2 = entity.getZ() - getZCloak();
		if (d0 > 10.0D) {
			setXCloak(entity.getX());
			setXCloakO(getXCloak());
		}

		if (d2 > 10.0D) {
			setZCloak(entity.getZ());
			setZCloakO(getZCloak());
		}

		if (d1 > 10.0D) {
			setYCloak(entity.getY());
			setYCloakO(getYCloak());
		}

		if (d0 < -10.0D) {
			setXCloak(entity.getX());
			setXCloakO(getXCloak());
		}

		if (d2 < -10.0D) {
			setZCloak(entity.getZ());
			setZCloakO(getZCloak());
		}

		if (d1 < -10.0D) {
			setYCloak(entity.getY());
			setYCloakO(getYCloak());
		}

		setXCloak(getXCloak() + (d0 * 0.25D));
		setYCloak(getYCloak() + (d1 * 0.25D));
		setZCloak(getZCloak() + (d2 * 0.25D));
	}

	default T asEntity() {
		return (T) this;
	}

}
