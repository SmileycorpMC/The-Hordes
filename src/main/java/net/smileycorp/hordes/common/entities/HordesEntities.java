package net.smileycorp.hordes.common.entities;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.integration.oe.EntityDrownedPlayer;

@Mod.EventBusSubscriber(modid=Constants.MODID)
public class HordesEntities {

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        IForgeRegistry<EntityEntry> registry = event.getRegistry();
        int ID = 201;
        register(registry, "zombie_player", EntityZombiePlayer.class, ID++);
        register(registry, "husk_player", EntityHuskPlayer.class, ID++);
        if (Loader.isModLoaded("oe")) register(registry, "drowned_player", EntityDrownedPlayer.class, ID++);
    }

    private static void register(IForgeRegistry<EntityEntry> registry, String name, Class<? extends Entity> clazz, int id) {
        registry.register(EntityEntryBuilder.create().entity(clazz).id(Constants.loc(name), id)
                .name(Constants.name(name)).tracker(80, 3, true).build());
    }

}
