package net.smileycorp.hordes.common.data;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

public class BinaryOperation<T extends Number & Comparable<T>> {
    
    private static final Map<String, BinaryOperation> registry = Maps.newHashMap();
    
    public static final BinaryOperation ADD = register("add", "+", DataType.DOUBLE,(a, b) -> a + b);
    public static final BinaryOperation MINUS = register("minus", "-", DataType.DOUBLE,(a, b) -> a - b);
    public static final BinaryOperation DIVIDE = register("divide", "/", DataType.DOUBLE,(a, b) -> a / b);
    public static final BinaryOperation MULTIPLY = register("multiply", "*", DataType.DOUBLE,(a, b) -> a * b);
    public static final BinaryOperation MOD = register("mod", "%", DataType.DOUBLE,(a, b) -> a % b);
    public static final BinaryOperation POW = register("power", "pow", DataType.DOUBLE, Math::pow);
    public static final BinaryOperation BITWISE_AND = register("bitwise_and", "&", DataType.INT, (a, b) -> a & b);
    public static final BinaryOperation BITWISE_OR = register("bitwise_or", "|", DataType.INT, (a, b) -> a | b);
    public static final BinaryOperation BITWISE_XOR = register("bitwise_xor", "^", DataType.INT, (a, b) -> a ^ b);
    public static final BinaryOperation LEFT_SHIFT = register("left_shift", "<<", DataType.INT, (a, b) -> a << b);
    public static final BinaryOperation RIGHT_SHIFT = register("right_shift", ">>", DataType.INT, (a, b) -> a >> b);
    public static final BinaryOperation UNSIGNED_RIGHT_SHIFT = register("unsigned_right_shift", ">>>", DataType.INT, (a, b) -> a >>> b);
    
    private final String name;
    private final String symbol;
    private final DataType<T> type;
    private final BiFunction<T, T, T> function;
    
    private BinaryOperation(String name, String symbol, DataType<T> type, BiFunction<T, T, T> function) {
        this.name = name;
        this.symbol = symbol;
        this.type = type;
        this.function = function;
    }
    
    public String getName() {
        return name;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public T apply(Comparable a, Comparable b) {
        return function.apply(type.cast(a), type.cast(b));
    }
    
    private static <T extends Number & Comparable<T>> BinaryOperation register(String name, String symbol, DataType<T> type, BiFunction<T, T, T> function) {
        BinaryOperation operation = new BinaryOperation(name, symbol, type, function);
        return registry.put(symbol, operation);
    }
    
    public static BinaryOperation of(String symbol) {
        if (registry.containsKey(symbol)) return registry.get(symbol);
        return null;
    }
    
    public static Collection<BinaryOperation> values() {
        return registry.values();
    }

}
