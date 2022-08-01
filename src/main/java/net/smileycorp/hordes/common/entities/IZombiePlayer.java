package net.smileycorp.hordes.common.entities;

import java.util.Collection;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface IZombiePlayer {

	public void setPlayer(PlayerEntity player);

	public void setPlayer(String username);

	public void setPlayer(UUID uuid);

	public void setPlayer(GameProfile profile);

	public UUID getPlayerUUID();

	public void setInventory(Collection<ItemEntity> drops);

	public void setInventory(NonNullList<ItemStack> drops);

	public NonNullList<ItemStack> getInventory();

	public void copyFrom(IZombiePlayer entity);

	public void setDisplayCape(boolean display);

	public boolean displayCape();

	public double getXCloakO();

	public double getYCloakO();

	public double getZCloakO();

	public double getXCloak();

	public double getYCloak();

	public double getZCloak();

	public void setXCloakO(double value);

	public void setYCloakO(double value);

	public void setZCloakO(double value);

	public void setXCloak(double value);

	public void setYCloak(double value);

	public void setZCloak(double value);

	public default void moveCloak(ZombieEntity entity) {
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

}
