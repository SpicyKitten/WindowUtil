package throwing;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingRunnable extends Runnable, ExceptionFlowController
{
	public abstract void run_() throws Exception;
	
	@Override
	default void run()
	{
		try
		{
			run_();
		}
		catch (Exception e)
		{
			handle(e);
		}
	}
	
	static Runnable of(ThrowingRunnable tr, Consumer<Exception> h)
	{
		return new ThrowingRunnable()
		{
			public void run_() throws Exception
			{
				tr.run_();
			}
			
			public void handle(Exception e)
			{
				h.accept(e);
			}
		};
	}
	
	static Runnable of(ThrowingRunnable tr)
	{
		return tr;
	}
}
