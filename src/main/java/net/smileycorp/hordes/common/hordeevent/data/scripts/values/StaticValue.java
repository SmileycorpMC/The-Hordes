package net.smileycorp.hordes.common.hordeevent.data.scripts.values;

import com.google.gson.JsonElement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;

import java.util.Random;

public class StaticValue<T extends Comparable<T>> implements ValueGetter<T> {

    private final T value;

    public StaticValue(DataType<T> type, JsonElement value) {
        this.value = type.readFromJson(value);
    }

    @Override
    public T get(Level level, Player player, Random rand) {
        return value;
    }

}
