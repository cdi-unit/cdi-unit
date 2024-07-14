package io.github.cdiunit.internal.servlet.common;

/**
 * This product includes software developed at
 * The Apache Software Foundation (https://www.apache.org/).
 */
public class ExceptionUtils {

    private ExceptionUtils() throws IllegalAccessException {
        throw new IllegalAccessException("don't instantiate me");
    }

    /**
     * Reinterprets the given (usually checked) exception without adding the exception to the throws
     * clause of the calling method. This method prevents throws clause
     * inflation and reduces the clutter of "Caused by" exceptions in the
     * stack trace.
     * <p>
     * The use of this technique may be controversial, but useful.
     * </p>
     *
     * <pre>
     *  // There is no throws clause in the method signature.
     *  public int propagateExample {
     *      try {
     *          // Throws IOException
     *          invocation();
     *      } catch (Exception e) {
     *          // Propagates a checked exception.
     *          throw ExceptionUtils.asRuntimeException(e);
     *      }
     *      // more processing
     *      ...
     *      return value;
     *  }
     * </pre>
     * <p>
     * This is an alternative to the more conservative approach of wrapping the
     * checked exception in a RuntimeException:
     * </p>
     *
     * <pre>
     *  // There is no throws clause in the method signature.
     *  public int wrapExample() {
     *      try {
     *          // throws IOException.
     *          invocation();
     *      } catch (Error e) {
     *          throw e;
     *      } catch (RuntimeException e) {
     *          // Throws an unchecked exception.
     *          throw e;
     *      } catch (Exception e) {
     *          // Wraps a checked exception.
     *          throw new UndeclaredThrowableException(e);
     *      }
     *      // more processing
     *      ...
     *      return value;
     *  }
     * </pre>
     * <p>
     * One downside to using this approach is that the Java compiler will not
     * allow invoking code to specify a checked exception in a catch clause
     * unless there is some code path within the try block that has invoked a
     * method declared with that checked exception. If the invoking site wishes
     * to catch the shaded checked exception, it must either invoke the shaded
     * code through a method re-declaring the desired checked exception, or
     * catch Exception and use the {@code instanceof} operator. Either of these
     * techniques are required when interacting with non-Java JVM code such as
     * Jython, Scala, or Groovy, since these languages do not consider any
     * exceptions as checked.
     * </p>
     *
     * @param throwable The throwable to reinterpret as a {@link RuntimeException}.
     * @param <T> The type of the returned value.
     * @return throwable to reinterpreted as a {@link RuntimeException}.
     */
    public static <T extends RuntimeException> T asRuntimeException(final Throwable throwable) {
        // claim that the eraseType invocation returns a RuntimeException
        return ExceptionUtils.<T, RuntimeException> eraseType(throwable);
    }

    /**
     * Claims a Throwable is another Throwable type using type erasure. This
     * hides a checked exception from the Java compiler, allowing a checked
     * exception to be thrown without having the exception in the method's throw
     * clause.
     */
    @SuppressWarnings("unchecked")
    private static <R, T extends Throwable> R eraseType(final Throwable throwable) throws T {
        throw (T) throwable;
    }

}
