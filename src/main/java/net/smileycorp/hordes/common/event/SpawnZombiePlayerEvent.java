package net.smileycorp.hordes.common.event;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.smileycorp.hordes.common.entities.PlayerZombie;

@Cancelable
public class SpawnZombiePlayerEvent extends PlayerEvent {

	private EntityType<? extends PlayerZombie> type;

	public SpawnZombiePlayerEvent(PlayerEntity player, EntityType<? extends PlayerZombie> type) {
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
