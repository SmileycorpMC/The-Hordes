package net.smileycorp.hordes.common.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.smileycorp.hordes.hordeevent.capability.HordeEventClient;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.infection.capability.Infection;

public class HordesCapabilities {
    
    @CapabilityInject(HordeEventClient.class)
    public final static Capability<HordeEventClient> HORDE_EVENT_CLIENT = null;
    @CapabilityInject(HordeSpawn.class)
    public final static Capability<HordeSpawn> HORDESPAWN = null;
    @CapabilityInject(ZombifyPlayer.class)
    public final static Capability<ZombifyPlayer> ZOMBIFY_PLAYER = null;
    @CapabilityInject(Infection.class)
    public final static Capability<Infection> INFECTION = null;
    
}
