package net.smileycorp.hordes.common.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.smileycorp.hordes.hordeevent.capability.HordeEventClient;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.infection.capability.Infection;

public class HordesCapabilities {

    public final static Capability<HordeEventClient> HORDE_EVENT_CLIENT = CapabilityManager.get(new CapabilityToken<HordeEventClient>() {});
    public final static Capability<HordeSpawn> HORDESPAWN = CapabilityManager.get(new CapabilityToken<HordeSpawn>() {});
    public final static Capability<ZombifyPlayer> ZOMBIFY_PLAYER = CapabilityManager.get(new CapabilityToken<ZombifyPlayer>() {});
    public final static Capability<Infection> INFECTION = CapabilityManager.get(new CapabilityToken<Infection>() {});

}
