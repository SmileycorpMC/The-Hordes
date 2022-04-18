package net.smileycorp.hordes.common.event;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.smileycorp.hordes.common.entities.IZombiePlayer;

@Cancelable
public class SpawnZombiePlayerEvent extends PlayerEvent {

	private EntityType<? extends IZombiePlayer> type;

	public SpawnZombiePlayerEvent(Player player, EntityType<? extends IZombiePlayer> type) {
		super(player);
		this.type = type;
	}

	public EntityType<? extends IZombiePlayer> getEntityType() {
		return type;
	}

	public void setEntityType(EntityType<? extends IZombiePlayer> type) {
		this.type = type;
	}

}
