package window;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
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
		getWindows("Ecli", SearchType.CONTAINS);
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
			properties.computeIfPresent("title-search-length", (a, b) -> Math.min(1 << 16,
				Math.max(1, Integer.parseInt(((String)b).replaceAll("//.*", "")))));
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
	 * Find windows, with {@code query} in the first {@code TITLE_SEARCH_LENGTH}
	 * characters of their title
	 * 
	 * @param query
	 *            The query string to be searched for
	 * @param search
	 *            The query type on the title strings
	 * @return All windows matched to the query string and type
	 */
	public static Collection<HWND> getWindows(String query, SearchType search)
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
}
