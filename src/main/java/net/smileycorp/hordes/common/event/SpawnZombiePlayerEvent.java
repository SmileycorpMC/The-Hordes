package net.smileycorp.hordes.common.event;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.smileycorp.hordes.common.entities.PlayerZombie;

public class SpawnZombiePlayerEvent extends PlayerEvent implements ICancellableEvent {

	private EntityType<? extends PlayerZombie> type;

	public SpawnZombiePlayerEvent(Player player, EntityType<? extends PlayerZombie> type) {
		super(player);
		this.type = type;
	}

	public EntityType<? extends PlayerZombie> getEntityType() {
		return type;
	}

	public void setEntityType(EntityType<? extends PlayerZombie> type) {
		this.type = type;
	}

}
