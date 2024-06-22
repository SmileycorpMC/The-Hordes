package net.smileycorp.hordes.common.capability;

import net.neoforged.neoforge.capabilities.EntityCapability;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.infection.capability.Infection;

public class HordesCapabilities {
    
    public final static EntityCapability<ZombifyPlayer, Void> ZOMBIFY_PLAYER = EntityCapability.createVoid(Constants.loc("zombify_player"), ZombifyPlayer.class);
    
    public final static EntityCapability<HordeSpawn, Void> HORDESPAWN = EntityCapability.createVoid(Constants.loc("horde_spawn"), HordeSpawn.class);
   
    public final static EntityCapability<Infection, Void> INFECTION = EntityCapability.createVoid(Constants.loc("infection"), Infection.class);

}
