package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.util.Collection;
import javax.swing.JFrame;
import org.junit.jupiter.api.Test;
import com.sun.jna.platform.win32.WinDef.HWND;
import window.SearchType;
import window.WindowUtil;

class WindowUtilTest
{
	@Test
	void testGetWindows()
	{
		Frame frame = new JFrame("This is a window with a really long title "
			+ "and nobody should make another frame with the same title "
			+ "unless they want this test to maliciously fail");
		frame.setUndecorated(true);
		frame.setBackground(new Color(0, 0, 0, 0));
		frame.setVisible(true);
		if (WindowUtil.TITLE_SEARCH_LENGTH > frame.getTitle().length()) // we're good
		{
			Collection<?> elems = WindowUtil.getWindows(
				"This is a window with a really long title", SearchType.START);
			assertNotNull(elems);
			assertEquals("There should be one window with the given title!", 1,
				elems.size());
		}
		frame.setVisible(false);
	}
	
	//@formatter:off
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
	}//@formatter:on
	
	@Test
	void testGetBounds()
	{
		// hard to test now, but somehow it works perfectly
		// when the test was written, it passed but didn't do the correct thing
	}
}
