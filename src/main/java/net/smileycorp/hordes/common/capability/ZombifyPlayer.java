package net.smileycorp.hordes.common.capability;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.entities.IZombiePlayer;
import net.smileycorp.hordes.common.event.SpawnZombiePlayerEvent;
import net.smileycorp.hordes.common.infection.HordesInfection;

public class ZombifyPlayer implements IZombifyPlayer {

	private Mob zombie = null;

	@Override
	public Mob createZombie(Player player) {
		EntityType<? extends IZombiePlayer> type = (player.isUnderWater() && (CommonConfigHandler.drownedPlayers.get() || CommonConfigHandler.drownedGraves.get()))
				? HordesInfection.DROWNED_PLAYER.get() : HordesInfection.ZOMBIE_PLAYER.get();
		SpawnZombiePlayerEvent event = new SpawnZombiePlayerEvent(player, type);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled()) return null;
		type = event.getEntityType();
		zombie = (Mob) type.create(player.level);
		((IZombiePlayer) zombie).setPlayer(player);
		zombie.setPos(player.getX(), player.getY(), player.getZ());
		zombie.yBodyRotO = player.yBodyRotO;
		return zombie;
	}

	@Override
	public Mob getZombie() {
		return zombie;
	}

	@Override
	public void clearZombie() {
		zombie=null;
	}

}