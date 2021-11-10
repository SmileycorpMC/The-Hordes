package net.smileycorp.hordes.common.capability;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.entities.IZombiePlayer;
import net.smileycorp.hordes.common.event.SpawnZombiePlayerEvent;
import net.smileycorp.hordes.common.infection.HordesInfection;

public class ZombifyPlayer implements IZombifyPlayer {

	private MobEntity zombie = null;

	@Override
	public MobEntity createZombie(PlayerEntity player) {
		EntityType<? extends IZombiePlayer> type = (player.isUnderWater() && (CommonConfigHandler.drownedPlayers.get() || CommonConfigHandler.drownedGraves.get()))
				? HordesInfection.DROWNED_PLAYER.get() : HordesInfection.ZOMBIE_PLAYER.get();
		SpawnZombiePlayerEvent event = new SpawnZombiePlayerEvent(player, type);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled()) return null;
		type = event.getEntityType();
		zombie = (MobEntity) type.create(player.level);
		((IZombiePlayer) zombie).setPlayer(player);
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