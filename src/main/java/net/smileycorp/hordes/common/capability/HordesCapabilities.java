package net.smileycorp.hordes.common.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.smileycorp.hordes.hordeevent.capability.IHordeSpawn;
import net.smileycorp.hordes.infection.capability.IInfection;

public class HordesCapabilities {

    public final static Capability<IHordeSpawn> HORDESPAWN = CapabilityManager.get(new CapabilityToken<IHordeSpawn>() {});
    public final static Capability<IZombifyPlayer> ZOMBIFY_PLAYER = CapabilityManager.get(new CapabilityToken<IZombifyPlayer>() {});
    public final static Capability<IInfection> INFECTION = CapabilityManager.get(new CapabilityToken<IInfection>() {});

}
