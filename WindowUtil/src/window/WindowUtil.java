package window;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
	}
	
	/**
	 * How many characters to search in any window title
	 */
	private static final int TITLE_SEARCH_LENGTH = 256;
	private static final Properties properties;
	static
	{
		properties = new Properties();
		File f = new File("windowUtil.config");
		try (FileInputStream fis = new FileInputStream(f))
		{
			properties.load(fis);
			properties.computeIfPresent("title-search-length",
				(a, b) -> Integer.parseInt((String)b));
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
			System.err.println("Library WindowUtil Failed to load resources");
			System.exit(0);
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
	public static Collection<HWND> getWindows(String query, SearchType search)
	{
		ArrayList<HWND> handles = new ArrayList<>();
		User32.INSTANCE.EnumWindows((HWND, Pointer) ->
		{
			
			return true;
		}, (Pointer)null);
		return null;
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
