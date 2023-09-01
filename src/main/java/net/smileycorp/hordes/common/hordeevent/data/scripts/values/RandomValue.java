package net.smileycorp.hordes.common.hordeevent.data.scripts.values;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;

import java.util.List;
import java.util.Random;

public class RandomValue<T extends Comparable<T>> implements ValueGetter<T> {

    private final List<T> values = Lists.newArrayList();

    public RandomValue(DataType<T> type, JsonArray json) {
        for (JsonElement element : json) values.add(type.readFromJson(element));
    }

    @Override
    public T get(Level level, Player player, Random rand) {
        return values.get(rand.nextInt(values.size()));
    }

}
