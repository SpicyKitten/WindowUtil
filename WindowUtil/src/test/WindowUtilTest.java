package test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import window.SearchType;
import window.WindowUtil;

class WindowUtilTest
{
	
	@Test
	void testMatchesSearch() throws Exception
	{
		Class<WindowUtil> c = WindowUtil.class;
		Method m = c.getDeclaredMethod("matchesSearch", String.class, String.class,
			SearchType.class);
		m.setAccessible(true);
		assertTrue((boolean)m.invoke(null,"MatchingSearch", "Mat", SearchType.CONTAINS));
		assertTrue((boolean)m.invoke(null,"MatchingSearch", "ear", SearchType.CONTAINS));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "MarS", SearchType.CONTAINS));
		assertTrue((boolean)m.invoke(null,"MatchingSearch", "earch", SearchType.END));
		assertTrue((boolean)m.invoke(null,"MatchingSearch", "MatchingSearch", SearchType.END));
		assertTrue((boolean)m.invoke(null,"MatchingSearch", "", SearchType.END));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "dslf", SearchType.END));
		assertTrue((boolean)m.invoke(null,"MatchingSearch", "MatchingSearch", SearchType.EXACT));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "MatchingSearches", SearchType.EXACT));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "", SearchType.EXACT));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "MarS", SearchType.EXACT));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "MaTcHiNgSeArCh", SearchType.EXACT));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "MaTcHiNgSeArCheS", SearchType.EXACT));
		assertTrue((boolean)m.invoke(null,"MatchingSearch", "MatchingSearch", SearchType.EXACT_NO_CASE));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "MatchingSearches", SearchType.EXACT_NO_CASE));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "", SearchType.EXACT_NO_CASE));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "MarS", SearchType.EXACT_NO_CASE));
		assertTrue((boolean)m.invoke(null,"MatchingSearch", "MaTcHiNgSeArCh", SearchType.EXACT_NO_CASE));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "MaTcHiNgSeArCheS", SearchType.EXACT_NO_CASE));
		assertTrue((boolean)m.invoke(null,"MatchingSearch", "MatchingSearch", SearchType.EXACT));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "MatchingSearches", SearchType.EXACT));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "", SearchType.EXACT));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "MarS", SearchType.EXACT));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "", SearchType.REGEX));
		assertTrue((boolean)m.invoke(null,"MatchingSearch", ".+", SearchType.REGEX));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "MatchingSearch.+", SearchType.REGEX));
		assertTrue((boolean)m.invoke(null,"MatchingSearch", "M\\D+Search", SearchType.REGEX));
		assertFalse((boolean)m.invoke(null,"MatchingSearch", "M\\d+Search", SearchType.REGEX));
		assertTrue((boolean)m.invoke(null,"M99009900Search", "M\\d+Search", SearchType.REGEX));
		assertTrue((boolean)m.invoke(null,"M99009900Search", "M(\\d{0,4})+Search", SearchType.REGEX));
		assertFalse((boolean)m.invoke(null,"M99009900Search", "M\\D+Search", SearchType.REGEX));
	}
}
