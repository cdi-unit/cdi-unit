package io.github.cdiunit.internal.servlet;

public final class ExceptionUtils {

	private ExceptionUtils() {
	}

	@SuppressWarnings("unchecked")
	public static <E extends Throwable> E sneaky(Throwable t) throws E {
		return (E) t;
	}

}
