package net.smileycorp.hordes.common.hordeevent.data.functions;

import net.minecraft.resources.ResourceLocation;
import net.smileycorp.hordes.common.event.HordeBuildSpawntableEvent;
import net.smileycorp.hordes.common.hordeevent.data.HordeTableLoader;
import net.smileycorp.hordes.common.hordeevent.data.values.ValueGetter;

public class SetSpawntableFunction implements HordeFunction<HordeBuildSpawntableEvent> {

    private final ValueGetter<String> getter;

    public SetSpawntableFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }

    @Override
    public void apply(HordeBuildSpawntableEvent event) {
        event.setSpawnTable(HordeTableLoader.INSTANCE.getTable(
                new ResourceLocation(getter.get(event.getEntityWorld(),
                        event.getPlayer(), event.getEntityWorld().random))));
    }
}
