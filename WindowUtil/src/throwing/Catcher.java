package throwing;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@FunctionalInterface
public interface Catcher<T extends Exception> extends BiConsumer<Class<? super T>, T>, Consumer<Exception>
{
	final Map<Catcher<? extends Exception>, Catcher<? extends Exception>> alternative = new HashMap<>();
	
	public abstract void accept_(Exception t);
	
	default void alternative(Exception e)
	{
		if(Catcher.alternative.containsKey(this))
			Catcher.alternative.get(this).accept_(e);
	}
	
	default void accept(Exception e)
	{
		accept_(e);
	}
	
	@Override
	default void accept(Class<? super T> t, T u)
	{
		if(t.isInstance(u))
			accept_(u);
		else if(alternative.containsKey(this))
			alternative.get(this).accept_(u);
	}
	
	public static <T extends Exception> Catcher<T> of(Catcher<T> consumer,
		Catcher<? extends Exception> other)
	{
		Catcher<T> ret = (t) ->
		{
			consumer.accept_(t);
		};
		Catcher.alternative.put(consumer, other);
		Catcher.alternative.put(other, ret::alternative);
		return ret;
	}
	
	public static Catcher<Exception> of(Catcher<Exception> consumer)
	{
		return (Exception t) ->
		{
			consumer.accept(Exception.class, t);
		};
	}
	
	@SuppressWarnings( "unchecked" )
	public static <T extends Exception> Catcher<T> of(Class<T> cls, Catcher<T> consumer)
	{
		Catcher<T> ret = (Exception t) ->
		{
			consumer.accept(cls, (T) t);
		};
		Catcher.alternative.put(consumer, ret::alternative);
		return ret;
	}
	
	default Catcher<T> andThen(Catcher<? extends Exception> after)
	{
		return Catcher.of(this, after);
	}
	
	// @Override
	// default BiConsumer<Class<? super T>, T> andThen(
	// BiConsumer<? super Class<? super T>, ? super T> after)
	// {
	// // TODO Auto-generated method stub
	// return BiConsumer.super.andThen(after);
	// }
	
	// public static BiConsumer<Class<?>, BiFunction<?,?,?>,Void> get()
	// {
	// return (c, f) -> (null);
	// }
	
	public static void main(String[] args)
	{
		Catcher<IOException> swallower = (e) -> System.out.println(e.getMessage());
		Catcher<NumberFormatException> swallow2 = (e) -> System.out
			.println("nfeeee " + e.getMessage());
		Catcher<IllegalArgumentException> swallow3 = (e) -> System.out
			.println("illegal " + e.getMessage());
		Catcher<IOException> c = Catcher.of(IOException.class, swallower)
			.andThen(Catcher.of(NumberFormatException.class, swallow2))
			.andThen(Catcher.of(IllegalArgumentException.class, swallow3))
			.andThen(f -> System.out.println("Got to alternative!"));
		c.accept(new IOException("this is io exception"));
		c.accept(new Exception("this is exception"));
		c.accept(new SocketException("this is socket exception"));
		c.accept(new NumberFormatException("this is number format exception"));
		c.accept(new IllegalArgumentException("this is illegal argument exception"));
	}
}
