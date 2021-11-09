package net.smileycorp.hordes.common.entities;

import java.awt.Color;
import java.util.Collection;
import java.util.UUID;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

import com.mojang.authlib.GameProfile;

public interface IZombiePlayer {

	public void setPlayer(PlayerEntity player);

	public void setPlayer(String username);

	public void setPlayer(UUID uuid);

	public void setPlayer(GameProfile profile);

	public UUID getPlayerUUID();

	public void setInventory(Collection<ItemEntity> drops);

	public Color getColour();

}
