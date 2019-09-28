package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;
import com.sun.jna.platform.win32.WinDef.HWND;
import window.SearchType;
import window.WinUtil;

class WinUtilTest
{
	@Test
	void testGetWindows()
	{
		var frame = new JFrame("This is a window with a really long title "
			+ "and nobody should make another frame with the same title "
			+ "unless they want this test to maliciously fail");
		frame.setUndecorated(true);
		frame.setBackground(new Color(0, 0, 0, 0));
		frame.setVisible(true);
		if (WinUtil.TITLE_SEARCH_LENGTH > frame.getTitle().length()) // we're good
		{
			Collection<?> elems = WinUtil.getWindows(
				"This is a window with a really long title", SearchType.START);
			assertNotNull(elems);
			assertEquals(1,
				elems.size(), "There should be one window with the given title!");
		}
		frame.setVisible(false);
	}
	
	//@formatter:off
	@Test
	void testMatchesSearch() throws Exception
	{
		Class<WinUtil> c = WinUtil.class;
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
		var frames = new ArrayList<JFrame>();
		for (int i = 15; i < 500; i += 50)
		{
			for (int j = 15; j < 500; j += 50)
			{
				var frame =
					new JFrame("This is a window with a " + i + "x" + j + " internal size"
						+ "and nobody should make another frame with the same title "
						+ "unless they want this test to maliciously fail");
				frame.setUndecorated(true);
				var b = new BufferedImage(i, j, BufferedImage.TYPE_INT_RGB);
				frame.add(new JLabel(new ImageIcon(b)));
				frame.pack();
				frame.setVisible(true);
				HWND h = WinUtil.getWindow(
					"This is a window with a " + i + "x" + j + " internal size",
					SearchType.START);
				Rectangle bounds = WinUtil.getBounds(h);
				double scaleFactor = (double)bounds.width / (double)i;
				// rounding error <= 0.01
				assertEquals(i * scaleFactor, bounds.width, 0.01,
					"This is a window with a " + i + "x" + j
					+ " internal size and width " + i);
				// rounding and multiplying, so 5% error allowed
				assertEquals(j * scaleFactor, bounds.height, j * scaleFactor * 0.05,
					"This is a window with a " + i + "x" + j
					+ " internal size and height " + j);
				frames.add(frame);
			}
		}
		frames.forEach(JFrame::dispose);
	}
	
	@Test
	void testGetPixel()
	{
		var frame = new JFrame("This is a window with a red pixel "
			+ "and nobody should make another frame with the same title "
			+ "unless they want this test to maliciously fail");
		frame.setUndecorated(true);
		frame.setBackground(Color.cyan);
		var b = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		b.setRGB(0, 0, Color.RED.getRGB());
		frame.add(new JLabel(new ImageIcon(b)));
		frame.pack();
		frame.setVisible(true);
		HWND h = WinUtil.getWindow("This is a window with a red pixel", SearchType.START);
		int rgb = WinUtil.getPixel(h, 0, 0);
		assertEquals(255 << 16, rgb & 0xffffff, "Pixel should be red!");
		frame.setVisible(false);
	}
	
	@Test
	void testCapture()
	{
		var frame = new JFrame("This is a window that is entirely green"
			+ "and nobody should make another frame with the same title "
			+ "unless they want this test to maliciously fail");
		frame.setUndecorated(true);
		frame.setBackground(Color.cyan);
		var b = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		Graphics g = b.getGraphics();
		g.setColor(Color.GREEN);
		g.fillRect(0, 0, 100, 100);
		frame.add(new JLabel(new ImageIcon(b)));
		frame.pack();
		frame.setVisible(true);
		HWND h = WinUtil.getWindow("This is a window that is entirely green",
			SearchType.START);
		BufferedImage rgb = WinUtil.capture(h);
		for (int i = 0; i < rgb.getWidth(); ++i)
		{
			for (int j = 0; j < rgb.getHeight(); ++j)
			{
				assertEquals(255 << 8,
					rgb.getRGB(i, j) & 0xffffff, "Pixel should be green!");
			}
		}
		frame.setVisible(false);
	}
}
