package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.UnaryOperation;

import java.util.Random;

public class UnaryOperationValueGetter<T extends Number & Comparable<T>> implements ValueGetter<T> {
    
    private final UnaryOperation operation;
    private final ValueGetter<T> value;
    
    private UnaryOperationValueGetter(UnaryOperation operation, ValueGetter<T> value) {
        this.operation = operation;
        this.value = value;
    }
    
    @Override
    public T get(World level, LivingEntity entity, ServerPlayerEntity player, Random rand) {
        return (T) operation.apply(value.get(level, entity, player, rand));
    }
    
    public static <T extends Number & Comparable<T>> UnaryOperationValueGetter deserialize(UnaryOperation operation, DataType<T> type, JsonObject element) {
        ValueGetter getter = ValueGetter.readValue(type, element.get("value"));
        if (getter == null |! type.isNumber()) {
            HordesLogger.logError("invalid value for hordes:" + operation.getName(), new NullPointerException());
            return null;
        }
        return new UnaryOperationValueGetter(operation, getter);
    }
    
}
