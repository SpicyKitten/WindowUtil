package window;

import java.util.function.BiFunction;

/**
 * A search typing. Takes two strings and validates the first according to the
 * second
 * 
 * @author ratha
 */
public enum SearchType
{
	CONTAINS(String::contains), END(String::endsWith), EXACT(String::equals),
	EXACT_NO_CASE(String::equalsIgnoreCase), REGEX(String::matches),
	START(String::startsWith), ALL((a, b) -> true, true), NONE((a, b) -> false, true);
	private BiFunction<String, String, Boolean> test;
	private boolean nullOk;
	
	private SearchType(BiFunction<String, String, Boolean> f)
	{
		this(f, false);
	}
	
	private SearchType(BiFunction<String, String, Boolean> f, boolean nOk)
	{
		test = f;
		nullOk = nOk;
	}
	
	/**
	 * Tests true if test validates haystack.op(needle), or false if values are
	 * undesirably null. Intentionally package protected.
	 */
	boolean test(String haystack, String needle)
	{
		if (test == null)
			return false;
		if (!nullOk && (haystack == null || needle == null))
			return false;
		return test.apply(haystack, needle);
	}
}
