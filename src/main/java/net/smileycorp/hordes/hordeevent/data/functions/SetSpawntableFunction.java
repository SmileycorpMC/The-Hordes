package net.smileycorp.hordes.hordeevent.data.functions;

import net.minecraft.resources.ResourceLocation;
import net.smileycorp.hordes.common.data.values.ValueGetter;
import net.smileycorp.hordes.common.event.HordeBuildSpawntableEvent;
import net.smileycorp.hordes.hordeevent.data.HordeTableLoader;

public class SetSpawntableFunction implements HordeFunction<HordeBuildSpawntableEvent> {

    private final ValueGetter<String> getter;

    public SetSpawntableFunction(ValueGetter<String> getter) {
        this.getter = getter;
    }

    @Override
    public void apply(HordeBuildSpawntableEvent event) {
        event.setSpawnTable(HordeTableLoader.INSTANCE.getTable(
                new ResourceLocation(getter.get(event.getEntityWorld(),
                        event.getEntity(), event.getEntityWorld().random))));
    }
}
