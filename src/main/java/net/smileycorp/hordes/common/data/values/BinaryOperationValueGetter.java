package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonObject;
import net.smileycorp.atlas.api.data.BinaryOperation;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.HordesParsingException;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

public class BinaryOperationValueGetter<T extends Number & Comparable<T>> implements ValueGetter<T> {
    
    private final BinaryOperation operation;
    private final ValueGetter<T> value1, value2;
    
    private BinaryOperationValueGetter(BinaryOperation operation, ValueGetter<T> value1, ValueGetter<T> value2) {
        this.operation = operation;
        this.value1 = value1;
        this.value2 = value2;
    }
    
    @Override
    public T get(HordeContext<? extends HordePlayerEvent> ctx) {
        return (T) operation.apply(value1.get(ctx), value2.get(ctx));
    }
    
    public static <T extends Number & Comparable<T>> BinaryOperationValueGetter deserialize(BinaryOperation operation, DataType<T> type, JsonObject element) {
        try {
            ValueGetter<T> getter1 = ValueGetter.readValue(type, element.get("value1"));
            ValueGetter<T> getter2 = ValueGetter.readValue(type, element.get("value2"));
            if (getter1 == null || getter2 == null |! type.isNumber()) {
                HordesLogger.logError("invalid values for hordes:" + operation.getName(), new HordesParsingException(
                        "value1: " + element.get("value1") + ", value2: " + element.get("value2")));
                return null;
            }
            return new BinaryOperationValueGetter(operation, getter1, getter2);
        } catch (Exception e) {
            HordesLogger.logError("invalid values for hordes:" + operation.getName(), new HordesParsingException("missing values"));
            return null;
        }
    }
    
}
