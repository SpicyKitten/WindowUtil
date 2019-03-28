package window;

import java.util.Collection;
import com.sun.jna.platform.win32.WinDef.HWND;

public class WindowUtil
{
	public static void main(String[] args)
	{
		String s = new String("TestString");
		System.out.println(s.matches(".*tS.*"));
	}
	public static Collection<HWND> getWindows(String query, SearchType search)
	{
		return null;
	}
	
	private static boolean matchesSearch(String title, String query, SearchType search)
	{
		switch(search)
		{
		case CONTAINS:
		case END:
		case EXACT:
		case EXACT_NO_CASE:
		case REGEX:
		case START:
			return search.test(title, query);
		default:
			throw new IllegalStateException("Search Type provided but not implemented in WindowUtil.matchesSearch!");
		}
	}
}

