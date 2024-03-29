package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.UnaryOperation;
import net.smileycorp.hordes.common.HordesLogger;

public class UnaryOperationValueGetter<T extends Number & Comparable<T>> implements ValueGetter<T> {
    
    private final UnaryOperation operation;
    private final ValueGetter<T> value;
    
    private UnaryOperationValueGetter(UnaryOperation operation, ValueGetter<T> value) {
        this.operation = operation;
        this.value = value;
    }
    
    @Override
    public T get(Level level, LivingEntity entity, ServerPlayer player, RandomSource rand) {
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
