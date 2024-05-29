package net.smileycorp.hordes.common.data;

import java.util.function.BiFunction;

public enum LogicalOperation {

	AND("and", "&&", (a, b) -> a&&b),
	NAND("nand", "!&", (a, b) -> !(a&&b)),
	OR("or", "||", (a, b) -> a||b),
	NOR("nor", "!|", (a, b) -> !(a||b)),
	XOR("xor", "^", (a, b) -> a^b);

	private final String name, symbol;
	private final BiFunction<Boolean, Boolean, Boolean> operation;

	LogicalOperation(String name, String symbol, BiFunction<Boolean, Boolean, Boolean> operation) {
		this.name = name;
		this.symbol = symbol;
		this.operation = operation;
	}

	public String getName() {
		return name;
	}

	public String getSymbol() {
		return symbol;
	}

	public boolean apply(boolean a, boolean b) {
		return operation.apply(a, b);
	}

	public static LogicalOperation fromName(String name) {
		for (LogicalOperation operation : values()) {
			if (operation.getName().equals(name)) return operation;
		}
		return null;
	}


	public static LogicalOperation of(String symbol) {
		for (LogicalOperation operation : values()) {
			if (operation.getSymbol().equals(symbol)) return operation;
		}
		return null;
	}

}
