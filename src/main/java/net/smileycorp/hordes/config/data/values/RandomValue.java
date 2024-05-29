package net.smileycorp.hordes.config.data.values;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.data.DataType;

import java.util.List;
import java.util.Random;

public class RandomValue<T extends Comparable<T>> implements ValueGetter<T> {

    private final List<ValueGetter<T>> values = Lists.newArrayList();

    public RandomValue(DataType<T> type, JsonArray json) {
        json.forEach(element -> values.add(ValueGetter.readValue(type, element)));
    }

    @Override
    public T get(World level, EntityLiving entity, EntityPlayerMP player, Random rand) {
        return values.get(rand.nextInt(values.size())).get(level, entity, player, rand);
    }

}
