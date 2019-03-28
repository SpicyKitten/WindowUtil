package window;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

public class WindowUtil
{
	public static void main(String[] args)
	{
		String s = new String("TestString");
		System.out.println(s.matches(".*tS.*"));
		System.out.println(properties);
		System.out.println(properties.getProperty("title-search-length"));
		System.out.println(properties.get("title-search-length"));
		HWND eclipse = getWindows("workspace", SearchType.START).get(0);
		capture(eclipse);
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
		int width = 1;
		int height = 1;
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}
	
}
