package net.smileycorp.hordes.common.capability;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.infection.capability.Infection;

public class HordesCapabilities {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MODID);

    public final static EntityCapability<ZombifyPlayer, Void> ZOMBIFY_PLAYER = EntityCapability.createVoid(Constants.loc("zombify_player"), ZombifyPlayer.class);

    public final static EntityCapability<HordeSpawn, Void> HORDESPAWN = EntityCapability.createVoid(Constants.loc("horde_spawn"), HordeSpawn.class);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> HORDE_SPAWN_PLAYER = ATTACHMENT_TYPES.register("horde_spawn_player", () -> AttachmentType.builder(() -> "").serialize(Codec.string(0, 36)).build());
   
    public final static EntityCapability<Infection, Void> INFECTION = EntityCapability.createVoid(Constants.loc("infection"), Infection.class);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> INFECTION_COUNT = ATTACHMENT_TYPES.register("infection_count", () -> AttachmentType.builder(() -> 1).serialize(Codec.INT).build());

}
