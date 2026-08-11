package net.smileycorp.atlas.api.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DataType<T extends Comparable<T>> {

    private static final Map<String, DataType<?>> registry = Maps.newHashMap();

    public static DataType<Byte> BYTE = new DataType<>("byte", Byte.class, JsonElement::getAsByte, JsonPrimitive::new, CompoundTag::getByte, CompoundTag::putByte);
    public static DataType<Short> SHORT = new DataType<>("short", Short.class, JsonElement::getAsShort, JsonPrimitive::new, CompoundTag::getShort, CompoundTag::putShort);
    public static DataType<Integer> INT = new DataType<>("int", Integer.class, JsonElement::getAsInt, JsonPrimitive::new, CompoundTag::getInt, CompoundTag::putInt);
    public static DataType<Long> LONG = new DataType<>("long", Long.class, JsonElement::getAsLong, JsonPrimitive::new, CompoundTag::getLong, CompoundTag::putLong);
    public static DataType<Float> FLOAT = new DataType<>("float", Float.class, JsonElement::getAsFloat, JsonPrimitive::new, CompoundTag::getFloat, CompoundTag::putFloat);
    public static DataType<Double> DOUBLE = new DataType<>("double", Double.class, JsonElement::getAsDouble, JsonPrimitive::new, CompoundTag::getDouble, CompoundTag::putDouble);
    public static DataType<String> STRING = new DataType<>("string", String.class, JsonElement::getAsString, JsonPrimitive::new, CompoundTag::getString, CompoundTag::putString);
    public static DataType<Boolean> BOOLEAN = new DataType<>("boolean", Boolean.class, JsonElement::getAsBoolean, JsonPrimitive::new, CompoundTag::getBoolean, CompoundTag::putBoolean);

    private final String name;
    private final Class<T> clazz;
    private final Function<JsonElement, T> jsonReader;
    private final Function<T, JsonElement> jsonWriter;
    private final BiFunction<CompoundTag, String, T> nbtReader;
    private final TriConsumer<CompoundTag, String,T> nbtWriter;

    private DataType(String name, Class<T> clazz, Function<JsonElement, T> jsonReader, Function<T, JsonElement> jsonWriter, BiFunction<CompoundTag, String, T> nbtReader, TriConsumer<CompoundTag, String, T> nbtWriter) {
        this.name = name;
        this.clazz = clazz;
        this.jsonReader = jsonReader;
        this.jsonWriter = jsonWriter;
        this.nbtReader = nbtReader;
        this.nbtWriter = nbtWriter;
        registry.put(name, this);
    }

    public Class<T> getType() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    public Boolean isNumber() {
        return Number.class.isAssignableFrom(clazz);
    }

    public T cast(Comparable<?> value) {
        return clazz.cast(value);
    }

    public T readFromJson(JsonElement element) {
        return jsonReader.apply(element);
    }

    public JsonElement writeToJson(T value) {
        return jsonWriter.apply(value);
    }

    public T readFromNBT(CompoundTag nbt, String key) {
        return nbtReader.apply(nbt, key);
    }

    public void writeToNBT(CompoundTag nbt, String key, T value) {
        nbtWriter.apply(nbt, key, value);
    }

    public static <T extends Comparable<T>> DataType<T> of(String name) {
        if (registry.containsKey(name)) return (DataType<T>) registry.get(name);
        return null;
    }

    public static <T extends Comparable<T>> DataType<T> of(Class<T> clazz) {
        for (DataType<?> type : registry.values()) {
            if (type.getType() == clazz) return (DataType<T>) type;
        }
        return null;
    }

    public static <T extends Comparable<T>> DataType<T> of(T value) {
        return of(value.getClass());
    }

}
