package net.smileycorp.hordes.common.hordeevent.data.values;

import com.google.gson.JsonElement;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.hordeevent.data.scripts.HordeScriptRegistry;

public interface ValueGetter<T extends Comparable<T>> {

    public T get(Level level, Player player, RandomSource rand);

    static ValueGetter readValue(DataType type, JsonElement value) {
        if (value.isJsonObject()) {
            return HordeScriptRegistry.readValue(type, value.getAsJsonObject());
        } else if (value.isJsonArray()) {
            return new RandomValue(type, value.getAsJsonArray());
        }
        return new StaticValue(type, value);
    }


}