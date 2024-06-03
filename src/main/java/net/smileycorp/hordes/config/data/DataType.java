package net.smileycorp.hordes.config.data;

import com.google.gson.JsonElement;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;


public class DataType<T extends Comparable<T>> {

	private static Map<String, DataType<?>> registry = new HashMap<String, DataType<?>>();

	public static DataType<Byte> BYTE = new DataType<Byte>("byte", Byte.class, (byte) 0, JsonElement::getAsByte, NBTTagCompound::getByte);
	public static DataType<Short> SHORT = new DataType<Short>("short", Short.class, (short) 0, JsonElement::getAsShort, NBTTagCompound::getShort);
	public static DataType<Integer> INT = new DataType<Integer>("int", Integer.class, 0, JsonElement::getAsInt, NBTTagCompound::getInteger);
	public static DataType<Long> LONG = new DataType<Long>("long", Long.class, 0l, JsonElement::getAsLong, NBTTagCompound::getLong);
	public static DataType<Float> FLOAT = new DataType<Float>("float", Float.class, 0f, JsonElement::getAsFloat, NBTTagCompound::getFloat);
	public static DataType<Double> DOUBLE = new DataType<Double>("double", Double.class, 0d, JsonElement::getAsDouble, NBTTagCompound::getDouble);
	public static DataType<String> STRING = new DataType<String>("string", String.class, "", JsonElement::getAsString, (nbt, key) -> nbt.getString(key));
	public static DataType<Boolean> BOOLEAN = new DataType<Boolean>("boolean", Boolean.class, false, JsonElement::getAsBoolean, NBTTagCompound::getBoolean);
	public static DataType<ResourceLocation> RESOURCE_LOCATION = new DataType<ResourceLocation>("resource_location", ResourceLocation.class, new ResourceLocation("", ""), json->new ResourceLocation(json.getAsString()), (nbt, key) -> new ResourceLocation(nbt.getString(key)));

	private final String name;
	private final Class<T> clazz;
	private final T defaultValue;
	private final Function<JsonElement, T> jsonReader;
	private final BiFunction<NBTTagCompound, String, T> nbtReader;
	
	private DataType(String name, Class<T> clazz, T defaultValue, Function<JsonElement, T> jsonReader, BiFunction<NBTTagCompound, String, T> nbtReader) {
		this.name = name;
		this.clazz = clazz;
		this.defaultValue = defaultValue;
		this.jsonReader = jsonReader;
		this.nbtReader = nbtReader;
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
	
	public T getDefaultValue() {
		return defaultValue;
	}

	public T readFromJson(JsonElement element) {
		return jsonReader.apply(element);
	}

	public T readFromNBT(NBTTagCompound nbt, String key) {
		return nbtReader.apply(nbt, key);
	}

	public static DataType<?> of(String name) {
		if (registry.containsKey(name)) return registry.get(name);
		return null;
	}

	public static DataType<?> of(Class<?> clazz) {
		for (DataType<?> type : registry.values()) if (type.getType() == clazz) return type;
		return null;
	}
	
}
