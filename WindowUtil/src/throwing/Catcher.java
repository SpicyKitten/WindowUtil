package throwing;

import java.util.LinkedHashMap;
import java.util.function.Consumer;

/**
 * Catches Exceptions, in order of specification, and 
 * redirects those exceptions to the appropriate handlers
 * 
 * @author ratha
 *
 * @param <T> The type of exceptions first caught by this Catcher
 */
public class Catcher<T extends Exception> implements Consumer<Exception>
{
	private LinkedHashMap<Class<? extends Exception>, Consumer<Exception>> checker;

	@Override
	public void accept(Exception t)
	{
		checker.keySet().stream().dropWhile(k -> !k.isInstance(t)).findFirst().map(checker::get)
				.ifPresent(k -> k.accept(t));
	}

	/**
	 * Creates a Catcher catching T and redirecting exceptions to {@code cons}
	 */
	private Catcher(Class<T> cls, Consumer<Exception> cons)
	{
		checker = new LinkedHashMap<>();
		checker.put(cls, cons);
	}

	/**
	 * Creates a Catcher catching T as a copy of another
	 */
	private Catcher(Catcher<T> other)
	{
		checker = new LinkedHashMap<>(other.checker);
	}

	/**
	 * @param cons A Consumer of exceptions with type T
	 * @return A Catcher catching T and redirecting exceptions to {@code cons}
	 */
	public static <T extends Exception> Catcher<T> of(Class<T> cls, Consumer<Exception> cons)
	{
		return new Catcher<T>(cls, cons);
	}

	/**
	 * @param cons A Consumer of exceptions
	 * @return A Catcher catching Exception and redirecting exceptions to {@code cons}
	 */
	public static Catcher<Exception> of(Consumer<Exception> cons)
	{
		return Catcher.of(Exception.class, cons);
	}

	@Deprecated
	public Catcher<T> andThen(Consumer<? super Exception> after)
	{
		// Method not supported, as the type bound is too loose
		// Tightening the type bound will generate a name clash with andThen(Catcher)
		throw new IllegalStateException("Method andThen(Consumer<?>) not supported");
	}

	/**
	 * Returns a chained catcher for {@code this} Catcher followed by the
	 * {@code next} Catcher. {@link Catcher#andThen(Catcher) andThen} can be chained
	 * until a terminal Catcher (one with {@code cls == Exception.class}) is created
	 * 
	 * @param after The next catcher in the chain
	 * @return A catcher that checks this Catcher's matching for an exception. If
	 *         this catcher does not apply to the exception, then it uses
	 *         {@code after} to process the exception instead.
	 */
	public Catcher<T> andThen(Catcher<? extends Exception> after)
	{
		if (checker.containsKey(Exception.class))
			throw new IllegalStateException("All exception types have already been accounted for!");
		var ret = new Catcher<>(this);
		for(var entry : after.checker.entrySet())
			if(!ret.checker.containsKey(entry.getKey()))
				ret.checker.put(entry.getKey(), entry.getValue());
		return ret;
	}

	/**
	 * Returns a chained catcher for {@code this} Catcher and another
	 * {@literal Consumer<Exception>}. {@link Catcher#andThen(Catcher) andThen} can
	 * be chained until a terminal Catcher (one with {@code cls == Exception.class})
	 * is created
	 * 
	 * @param <S>  The type of exception caught by the next consumer
	 * @param cls  The class value of the exception type caught by the next consumer
	 * @param cons The consumer following the checks of {@code this} Catcher
	 * @return A catcher that checks this Catcher's matching for an exception. If
	 *         this catcher does not apply to the exception, then it uses
	 *         {@code cons} to process exceptions of type {@code S} instead.
	 */
	public <S extends Exception> Catcher<T> andThen(Class<S> cls, Consumer<Exception> cons)
	{
		return andThen(Catcher.of(cls, cons));
	}
}
