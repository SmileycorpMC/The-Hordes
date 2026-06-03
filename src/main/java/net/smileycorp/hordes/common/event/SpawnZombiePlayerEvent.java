package net.smileycorp.hordes.common.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.smileycorp.hordes.common.entities.PlayerZombie;

@Cancelable
public class SpawnZombiePlayerEvent extends PlayerEvent {

    private Class<? extends PlayerZombie<?>> type;

    public SpawnZombiePlayerEvent(EntityPlayer player, Class<? extends PlayerZombie<?>> type) {
        super(player);
        this.type = type;
    }

    public Class<? extends PlayerZombie<?>> getEntityType() {
        return type;
    }

    public void setEntityType(Class<? extends PlayerZombie<?>> type) {
        this.type = type;
    }

}
