package window;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public class WindowUtil
{
	public static void main(String[] args)
	{
		HWND eclipse = getWindows("workspace", SearchType.CONTAINS).get(0);
		BufferedImage b = capture(eclipse);
	}
	
	/**
	 * How many characters to search in any window title
	 */
	public static final int TITLE_SEARCH_LENGTH;
	private static final Properties properties = new Properties();
	static
	{
		try (FileInputStream fis = new FileInputStream("windowUtil.config"))
		{
			properties.load(fis);
			properties.computeIfPresent("title-search-length",
				(a, b) -> Math.min(1 << 16, Math.max(1,
					readProperty(cleanComments().andThen(Integer::parseInt), b))));
		}
		catch (NumberFormatException | IOException e)
		{
			e.printStackTrace();
			System.err.println(
				"Library WindowUtil Failed to load resources: Check that config settings are correct");
		}
		TITLE_SEARCH_LENGTH = (int)properties.getOrDefault("title-search-length", 256);
	}
	
	/**
	 * @return A function which cleans comments from configuration file properties
	 */
	private static Function<String, String> cleanComments()
	{
		return (property) -> property.replaceAll("//.*", "");
	}
	
	/**
	 * reads a property of type R from {@code property} using conversion function
	 * {@code converter}
	 */
	private static <R> R readProperty(Function<String, R> converter, Object property)
	{
		return converter.apply(((String)property));
	}
	
	/**
	 * Tests window titles for matching to query strings
	 * 
	 * @param title
	 *            The window title to test
	 * @param query
	 *            The query string being tested against
	 * @param search
	 *            The match verifier
	 * @return true if {@code title} satisfies {@code query} according to the
	 *         {@link SearchType}, else false
	 */
	private static boolean matchesSearch(String title, String query, SearchType search)
	{
		switch (search)
		{
		case CONTAINS:
		case END:
		case EXACT:
		case EXACT_NO_CASE:
		case REGEX:
		case START:
			return search.test(title, query);
		default:
			throw new IllegalStateException(
				"Search Type provided but not implemented in WindowUtil.matchesSearch!");
		}
	}
	
	/**
	 * Find windows, with {@code query} in the first {@code TITLE_SEARCH_LENGTH}
	 * characters of their title
	 * 
	 * @param query
	 *            The query string to be searched for
	 * @param search
	 *            The query type on the title strings
	 * @return All windows matched to the query string and type
	 */
	public static List<HWND> getWindows(String query, SearchType search)
	{
		ArrayList<HWND> handles = new ArrayList<>();
		User32.INSTANCE.EnumWindows((HWND, Pointer) ->
		{
			char[] titleChars = new char[TITLE_SEARCH_LENGTH + 1];
			User32.INSTANCE.GetWindowText(HWND, titleChars, TITLE_SEARCH_LENGTH + 1);
			String title = new String(titleChars);
			if (matchesSearch(title, query, search))
				handles.add(HWND);
			return true;
		}, (Pointer)null);
		return handles;
	}
	
	/**
	 * Captures an image associated with the given window
	 * 
	 * @param window
	 *            A window handle to the given window
	 * @return The associated window
	 */
	public static BufferedImage capture(HWND window)
	{
		HDC dcWin = User32.INSTANCE.GetDC(window);
		HDC dcMem = GDI32.INSTANCE.CreateCompatibleDC(dcWin);
		Rectangle bounds = getBounds(window);
		System.out.println("BOUNDS: "+bounds);
		HBITMAP bmp =
			GDI32.INSTANCE.CreateCompatibleBitmap(dcWin, bounds.width, bounds.height);
		HANDLE hObj = GDI32.INSTANCE.SelectObject(dcMem, bmp);
		GDI32.INSTANCE.BitBlt(dcMem, 0, 0, bounds.width, bounds.height, dcWin, 0, 0,
			GDI32.SRCCOPY);
		GDI32.INSTANCE.SelectObject(dcMem, hObj);
		GDI32.INSTANCE.DeleteDC(dcMem);
		BITMAPINFO bmi = new BITMAPINFO();
		bmi.bmiHeader.biWidth = bounds.width;
		bmi.bmiHeader.biHeight = -bounds.height;
		bmi.bmiHeader.biPlanes = 1;
		bmi.bmiHeader.biBitCount = 32;
		bmi.bmiHeader.biCompression = 0;
		Memory buffer = new Memory((long)(bounds.width * bounds.height * 4));
		GDI32.INSTANCE.GetDIBits(dcWin, bmp, 0, bounds.height, buffer, bmi, 0);
		BufferedImage image =
			new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, bounds.width, bounds.height,
			buffer.getIntArray(0L, bounds.width * bounds.height), 0, bounds.width);
		GDI32.INSTANCE.DeleteObject(bmp);
		User32.INSTANCE.ReleaseDC(window, dcWin);
		return image;
	}
	
	/**
	 * Gets the bounds for a given window
	 * 
	 * @param window
	 *            The handle for the given window
	 * @return A rectangle containing window bound information
	 */
	public static Rectangle getBounds(HWND window)
	{
		RECT rect = new RECT();
		User32.INSTANCE.GetClientRect(window, rect);
		return new Rectangle(rect.left, rect.top, rect.right - rect.left,
			rect.bottom - rect.top);
	}
	
}
