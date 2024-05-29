package net.smileycorp.hordes.common.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.smileycorp.hordes.common.IZombifyPlayer;
import net.smileycorp.hordes.hordeevent.capability.HordeEventClient;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.infection.capability.IInfection;

public class HordesCapabilities {
    
    @CapabilityInject(HordeEventClient.class)
    public final static Capability<HordeEventClient> HORDE_EVENT_CLIENT = null;
    @CapabilityInject(HordeSpawn.class)
    public final static Capability<HordeSpawn> HORDESPAWN = null;
    @CapabilityInject(IZombifyPlayer.class)
    public final static Capability<IZombifyPlayer> ZOMBIFY_PLAYER = null;
    @CapabilityInject(IInfection.class)
    public final static Capability<IInfection> INFECTION = null;
    
}
