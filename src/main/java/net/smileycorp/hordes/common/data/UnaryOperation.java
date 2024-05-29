package net.smileycorp.hordes.common.data;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class UnaryOperation<T extends Number & Comparable<T>> {
    
    private static final Map<String, UnaryOperation> registry = Maps.newHashMap();

    public static final UnaryOperation ABS = register("abs", "abs", DataType.DOUBLE, Math::abs);
    public static final UnaryOperation LOG = register("log", "ln", DataType.DOUBLE, Math::log);
    public static final UnaryOperation SIN = register("sin", "sin", DataType.DOUBLE, Math::sin);
    public static final UnaryOperation COS = register("cos", "cos", DataType.DOUBLE, Math::cos);
    public static final UnaryOperation TAN = register("tan", "tan", DataType.DOUBLE, Math::tan);
    public static final UnaryOperation ARCSIN = register("arcsin", "asin", DataType.DOUBLE, Math::asin);
    public static final UnaryOperation ARCCOS = register("arccos", "acos", DataType.DOUBLE, Math::acos);
    public static final UnaryOperation ARCTAN = register("arctan", "atan", DataType.DOUBLE, Math::atan);
    public static final UnaryOperation SINH = register("sinh", "sinh", DataType.DOUBLE, Math::sinh);
    public static final UnaryOperation COSH = register("cosh", "cosh", DataType.DOUBLE, Math::cosh);
    public static final UnaryOperation TANH = register("tanh", "tanh", DataType.DOUBLE, Math::tanh);
    public static final UnaryOperation ROUND = register("round", "round", DataType.DOUBLE, a -> (double)Math.round(a));
    public static final UnaryOperation FLOOR = register("floor", "floor", DataType.DOUBLE, Math::floor);
    public static final UnaryOperation CEILING = register("ceiling", "ceil", DataType.DOUBLE, Math::ceil);
    public static final UnaryOperation TRUNCATE = register("truncate", "trunc", DataType.DOUBLE, a -> (double)a.intValue());
    public static final UnaryOperation RAND = register("random", "rand", DataType.INT, new Random()::nextInt);
    public static final UnaryOperation BITWISE_NOT = register("bitwise_not", "!", DataType.INT, a -> ~a);
    
    private final String name;
    private final String symbol;
    private final DataType<T> type;
    private final Function<T, T> function;
    
    private UnaryOperation(String name, String symbol, DataType<T> type, Function<T, T> function) {
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
    
    public T apply(Comparable a) {
        return function.apply(type.cast(a));
    }
    
    private static <T extends Number & Comparable<T>> UnaryOperation register(String name, String symbol, DataType<T> type, Function<T, T> function) {
        UnaryOperation operation = new UnaryOperation(name, symbol, type, function);
        return registry.put(symbol, operation);
    }
    
    public static UnaryOperation of(String symbol) {
        if (registry.containsKey(symbol)) return registry.get(symbol);
        return null;
    }
    
    public static Collection<UnaryOperation> values() {
        return registry.values();
    }

}
