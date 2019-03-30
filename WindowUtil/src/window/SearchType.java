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
	START(String::startsWith), ALL((a, b) -> true), NONE((a, b) -> false);
	private BiFunction<String, String, Boolean> test;
	
	private SearchType(BiFunction<String, String, Boolean> f)
	{
		test = f;
	}
	
	public boolean test(String haystack, String needle)
	{
		if (test == null)
			return false;
		return test.apply(haystack, needle);
	}
}
