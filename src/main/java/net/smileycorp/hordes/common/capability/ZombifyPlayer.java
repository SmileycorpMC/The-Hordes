package net.smileycorp.hordes.common.capability;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.entities.DrownedPlayerEntity;
import net.smileycorp.hordes.common.entities.ZombiePlayerEntity;

public class ZombifyPlayer implements IZombifyPlayer {

	private MobEntity zombie = null;

	@Override
	public MobEntity createZombie(PlayerEntity player) {
		zombie = (player.isUnderWater() && (CommonConfigHandler.drownedPlayers.get() || CommonConfigHandler.drownedGraves.get()))
				? new DrownedPlayerEntity(player) : new ZombiePlayerEntity(player);
		zombie.setPos(player.getX(), player.getY(), player.getZ());
		zombie.yBodyRotO = player.yBodyRotO;
		return zombie;
	}

	@Override
	public MobEntity getZombie() {
		return zombie;
	}

	@Override
	public void clearZombie() {
		zombie=null;
	}

}