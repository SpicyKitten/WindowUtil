package throwing;

public class ThrowingUtil 
{
	@SuppressWarnings("unchecked")
	static <E extends Exception> void raise(Exception e) throws E {
		throw (E) e;// sneakyThrow if you google it, restricted to exceptions only
	}
}
