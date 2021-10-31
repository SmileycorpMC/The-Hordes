package net.smileycorp.hordes.common.capability;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.smileycorp.hordes.common.entities.DrownedPlayerEntity;
import net.smileycorp.hordes.common.entities.IZombiePlayer;
import net.smileycorp.hordes.common.entities.ZombiePlayerEntity;

public class ZombifyPlayer implements IZombifyPlayer {

	private final PlayerEntity player;
	private IZombiePlayer zombie = null;

	public ZombifyPlayer() {
		player = null;
	}

	public ZombifyPlayer(PlayerEntity player) {
		this.player=player;
	}

	@Override
	public IZombiePlayer createZombie() {
		zombie = player.isUnderWater() ? new DrownedPlayerEntity(player) : new ZombiePlayerEntity(player);
		((MobEntity)zombie).setPos(player.getX(), player.getY(), player.getZ());
		((MobEntity)zombie).yBodyRotO = player.yBodyRotO;
		return zombie;
	}

	@Override
	public IZombiePlayer getZombie() {
		return zombie;
	}

	@Override
	public void clearZombie() {
		zombie=null;
	}

}