package window;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.win32.W32APIOptions;

/**
 * Allows for frames to be included in window images as well. Intentionally
 * package protected.
 */
interface User32Extra extends User32
{
	User32Extra INSTANCE =
		Native.load("user32", User32Extra.class, W32APIOptions.DEFAULT_OPTIONS);
	
	public HDC GetWindowDC(HWND hWnd);
	public HWND SetActiveWindow(HWND hWnd);
}
