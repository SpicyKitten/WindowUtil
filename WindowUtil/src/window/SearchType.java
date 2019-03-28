package window;

import java.util.function.*;

public enum SearchType
{
	CONTAINS(String::contains),
	END(String::endsWith),
	EXACT(String::equals),EXACT_NO_CASE(String::equalsIgnoreCase),
	REGEX(String::matches),START(String::startsWith);
	private BiFunction<String, String, Boolean> test;
	private SearchType(BiFunction<String, String, Boolean> f)
	{
		test = f;
	}
	public boolean test(String haystack, String needle)
	{
		return test.apply(haystack, needle);
	}
}
