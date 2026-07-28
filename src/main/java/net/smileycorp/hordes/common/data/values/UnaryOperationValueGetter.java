package net.smileycorp.hordes.common.data.values;

import com.google.gson.JsonObject;
import net.smileycorp.atlas.api.data.DataType;
import net.smileycorp.atlas.api.data.UnaryOperation;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.common.data.HordesParsingException;
import net.smileycorp.hordes.common.event.HordePlayerEvent;
import net.smileycorp.hordes.hordeevent.data.HordeContext;

public class UnaryOperationValueGetter<T extends Number & Comparable<T>> implements ValueGetter<T> {
    
    private final UnaryOperation operation;
    private final ValueGetter<T> value;
    
    private UnaryOperationValueGetter(UnaryOperation operation, ValueGetter<T> value) {
        this.operation = operation;
        this.value = value;
    }
    
    @Override
    public T get(HordeContext<? extends HordePlayerEvent> ctx) {
        return (T) operation.apply(value.get(ctx));
    }
    
    public static <T extends Number & Comparable<T>> UnaryOperationValueGetter deserialize(UnaryOperation operation, DataType<T> type, JsonObject element) {
        try {
            ValueGetter<T> getter = ValueGetter.readValue(type, element.get("value"));
            if (getter == null |! type.isNumber()) {
                HordesLogger.logError("invalid value for hordes:" + operation.getName(), new HordesParsingException(element.get("value").toString()));
                return null;
            }
            return new UnaryOperationValueGetter(operation, getter);
        } catch (Exception e) {
            HordesLogger.logError("invalid values for hordes:" + operation.getName(), new HordesParsingException("missing value"));
            return null;
        }

    }
    
}
