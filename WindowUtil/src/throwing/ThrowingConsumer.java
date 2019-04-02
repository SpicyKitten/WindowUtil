package throwing;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingConsumer<T> extends Consumer<T>, ExceptionFlowController
{
	public abstract void accept_(T t) throws Exception;
	
	@Override
	default void accept(T t)
	{
		try
		{
			accept_(t);
		}
		catch (Exception e)
		{
			handle(e);
		}
	}
	
	static <T> Consumer<T> of(ThrowingConsumer<T> tc, Consumer<Exception> h)
	{
		return new ThrowingConsumer<T>()
		{
			public void accept_(T t) throws Exception
			{
				tc.accept_(t);
			}
			
			public void handle(Exception e)
			{
				h.accept(e);
			}
		};
	}
	
	static <T> Consumer<T> of(ThrowingConsumer<T> tc)
	{
		return tc;
	}
}
