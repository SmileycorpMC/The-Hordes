package net.smileycorp.hordes.common.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class ComparableOperation {

	private static Map<String, ComparableOperation> registry = new HashMap<String, ComparableOperation>();

	public static ComparableOperation EQUALS = register("==", (a, b) -> a==b || a.equals(b));
	public static ComparableOperation NOT_EQUALS = register("!=", (a, b) -> !(a==b || a.equals(b)));
	public static ComparableOperation LESS_THAN = register("<", (a, b) -> numberWrapped(a, b, (c, d)->c.compareTo(d)<0));
	public static ComparableOperation GREATER_THAN = register(">", (a, b) -> numberWrapped(a, b, (c, d)->c.compareTo(d)>0));
	public static ComparableOperation LESS_OR_EQUAL = register("<=", (a, b) -> numberWrapped(a, b, (c, d)->c.compareTo(d)<=0));
	public static ComparableOperation GREATER_OR_EQUAL = register(">=", (a, b) -> numberWrapped(a, b, (c, d)->c.compareTo(d)>=0));

	protected final String symbol;
	protected final BiFunction<Comparable<?>, Comparable<?>, Boolean> operation;

	protected ComparableOperation(String symbol, BiFunction<Comparable<?>, Comparable<?>, Boolean> operation) {
		this.symbol = symbol;
		this.operation = operation;
	}

	public String getSymbol() {
		return symbol;
	}

	public boolean apply(Comparable<?> a, Comparable<?> b) {
		return operation.apply(a, b);
	}

	private static ComparableOperation register(String symbol, BiFunction<Comparable<?>, Comparable<?>, Boolean> operation) {
		ComparableOperation comparable = new ComparableOperation(symbol, operation);
		return registry.put(symbol, comparable);
	}

	public static ComparableOperation of(String symbol) {
		if (registry.containsKey(symbol)) return registry.get(symbol);
		return null;
	}

	public static ComparableOperationMod modOf(Comparable<?> comparable, ComparableOperation subOperation) {
		return new ComparableOperationMod(comparable, subOperation);
	}

	@SuppressWarnings("unchecked")
	private static boolean numberWrapped(Comparable<?> a, Comparable<?> b, BiFunction<Comparable<Number>, Number, Boolean> wrappedFunction) {
		if (a instanceof Number && b instanceof Number) {
			return wrappedFunction.apply((Comparable<Number>)a, (Number)b);
		}
		return false;
	}

	public static class ComparableOperationMod extends ComparableOperation {

		protected final Comparable<?> value;

		protected ComparableOperationMod(Comparable<?> value, ComparableOperation subOperation) {
			super("%"+value+subOperation.symbol, subOperation.operation);
			this.value=value;
		}

		@Override
		public boolean apply(Comparable<?> a, Comparable<?> b) {
			return super.apply(((Number)a).longValue() % ((Number)value).longValue(), ((Number)b).longValue());
		}

	}
}
