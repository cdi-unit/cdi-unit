package io.github.cdiunit.internal.servlet;

public final class ExceptionUtils {

	private ExceptionUtils() {
	}

	@SuppressWarnings("unchecked")
	public static <T extends RuntimeException> T asRuntimeException(final Throwable t) throws T {
		return (T) t;
	}

}
