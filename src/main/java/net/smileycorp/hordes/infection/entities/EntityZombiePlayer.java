package net.smileycorp.hordes.infection.entities;

import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

public class EntityZombiePlayer extends EntityZombie {
	
	protected EntityPlayer player = null;
	protected InventoryPlayer inv;
	
	public EntityZombiePlayer(World world) {
		super(world);
	}
	
	public EntityZombiePlayer(World world, EntityPlayer player) {
		this(world);
		setPlayer(player);
	}
	
	public void setPlayer(EntityPlayer player) {
		this.player = player;
		inv = new InventoryPlayer(player);
		inv.copyInventory(player.inventory);
	}

	public EntityPlayer getPlayer() {
		if (player != null) {
			return player;
		}
		return null;
	}
	

}
