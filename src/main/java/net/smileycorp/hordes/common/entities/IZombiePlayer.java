package net.smileycorp.hordes.common.entities;

import java.util.Collection;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IZombiePlayer {

	public void setPlayer(Player player);

	public void setPlayer(String username);

	public void setPlayer(UUID uuid);

	public void setPlayer(GameProfile profile);

	public UUID getPlayerUUID();

	public void setInventory(Collection<ItemEntity> drops);

	public void setInventory(NonNullList<ItemStack> drops);

	public NonNullList<ItemStack> getInventory();

	public void copyFrom(IZombiePlayer entity);

}
