package window;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.win32.W32APIOptions;

/**
 * Permits potentially prompter pixel picking. Purposefully package protected.
 */
interface GDI32Extra extends GDI32
{
	GDI32Extra INSTANCE =
		Native.load("gdi32", GDI32Extra.class, W32APIOptions.DEFAULT_OPTIONS);
	
	public int GetPixel(HDC hdc, int x, int y);
}
