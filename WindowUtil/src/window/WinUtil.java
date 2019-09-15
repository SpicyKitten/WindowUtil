package window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HKL;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.WINDOWPLACEMENT;

/**
 * Provides helpful window and screen image capture utilities
 * 
 * @author ratha
 */
public class WinUtil
{
	/**
	 * How many characters to search in any window title
	 */
	public static final int TITLE_SEARCH_LENGTH;
	static final Properties properties = new Properties();
	private static final User32 U32 = User32.INSTANCE;
	private static final GDI32 G32 = GDI32.INSTANCE;
	private static final User32Extra U32X = User32Extra.INSTANCE;
	private static final GDI32Extra G32X = GDI32Extra.INSTANCE;
	
	static
	{
		try (var fis = new FileInputStream("winUtil.config"))
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
				"Library WinUtil Failed to load resources: Check that config settings are correct");
		}
		TITLE_SEARCH_LENGTH = (int)properties.getOrDefault("title-search-length", 256);
	}
	
	/*
	 * Cannot be instantiated
	 */
	private WinUtil()
	{
	}
	
	/**
	 * @return A function which cleans comments from configuration file properties
	 */
	static Function<String, String> cleanComments()
	{
		return (property) -> property.replaceAll("//.*", "");
	}
	
	/**
	 * Reads a property of type R from {@code property} using conversion function
	 * {@code converter}
	 */
	static <R> R readProperty(Function<String, R> converter, Object property)
	{
		return converter.apply(((String)property));
	}
	
	/**
	 * Convenience method for getting the ID of the thread that created the given
	 * window
	 * 
	 * @param window
	 *            A window handle to the given window
	 * @throws IllegalArgumentException
	 *             If {@code window} is null
	 * @return The identifier of the thread that created the given window
	 */
	public static int getThread(HWND window)
	{
		if (window == null)
			throw new IllegalArgumentException(
				"The provided window handle cannot be null!");
		return U32.GetWindowThreadProcessId(window, null);
	}
	
	/**
	 * Convenience method for getting the input locale identifier for the given
	 * window.
	 * 
	 * @param window
	 *            A window handle to the given window
	 * @return Data specifying the input locale identifier
	 */
	public static HKL getLocaleIdentifier(HWND window)
	{
		if (window == null)
			return null;
		return U32.GetKeyboardLayout(getThread(window));
	}
	
	/**
	 * You want to send a keystroke (or more) to a given window. You have the window
	 * handle. You have the representations. You wish to know the <b>virtual</b>
	 * keycodes for your keystrokes. What now? You call this function. Beware: using
	 * PostMessage to send keys is not a good/effective idea, so such functionality
	 * is not supported. Virtual keycode functionality is only provided for
	 * convenience purposes.
	 * 
	 * @param window
	 *            A window handle to the given window
	 * @param chars
	 *            The sequence of characters you wish to send to the window
	 * @return A sequence of <b>virtual</b> keycodes corresponding to each of the
	 *         provided characters
	 */
	public static short[] virtualKeyCodes(HWND window, char... chars)
	{
		if (window == null || chars == null || chars.length < 1)
			return null;
		short[] codes = new short[chars.length];
		HKL localeIdentifier = getLocaleIdentifier(window);
		if (localeIdentifier == null)
			return null;
		for (int i = 0; i < chars.length; ++i)
		{
			codes[i] = U32.VkKeyScanExA((byte)chars[i], localeIdentifier);
		}
		return codes;
	}
	
	/**
	 * Restores the given window (from minimization or maximization)
	 * 
	 * @param window
	 *            A window handle to the given window
	 */
	public static void restore(HWND window)
	{
		if (window == null)
			return;
		U32.ShowWindow(window, WinUser.SW_RESTORE);
	}
	
	/**
	 * Minimizes the given window
	 * 
	 * @param window
	 *            A window handle to the given window
	 */
	public static void minimize(HWND window)
	{
		if (window == null)
			return;
		U32.ShowWindow(window, WinUser.SW_MINIMIZE);
	}
	
	/**
	 * Maximizes the given window
	 * 
	 * @param window
	 *            A window handle to the given window
	 */
	public static void maximize(HWND window)
	{
		if (window == null)
			return;
		U32.ShowWindow(window, WinUser.SW_MAXIMIZE);
	}
	
	/**
	 * Hides the given window
	 * 
	 * @param window
	 *            A window handle to the given window
	 */
	public static void hide(HWND window)
	{
		if (window == null)
			return;
		U32.ShowWindow(window, WinUser.SW_HIDE);
	}
	
	/**
	 * Shows the given window
	 * 
	 * @param window
	 *            A window handle to the given window
	 */
	public static void show(HWND window)
	{
		if (window == null)
			return;
		U32.ShowWindow(window, WinUser.SW_SHOW);
	}
	
	/**
	 * Sets the state of the given window, according to the data in {@code flags}
	 * 
	 * @param window
	 *            A window handle to the given window
	 * @param flags
	 *            An integer parameter specifying the new state of the given window.
	 *            Further details of the states permitted can be found in
	 *            {@link WinUser}, as possible values of
	 *            {@link WINDOWPLACEMENT#showCmd}
	 */
	public static void showWindow(HWND window, int flags)
	{
		if (window == null)
			return;
		U32.ShowWindow(window, flags);
	}
	
	/**
	 * Determines whether the given window is enabled for mouse/keyboard inputs
	 * 
	 * @param window
	 *            A window handle to the given window
	 * @return Whether the window is enabled for inputs, or false if handle
	 *         {@code window} is null
	 */
	public static boolean isEnabled(HWND window)
	{
		if (window == null)
			return false;
		return U32.IsWindowEnabled(window);
	}
	
	/**
	 * Instead of iterating over windows, this method conveniently generates a
	 * handle to the current active window. For situations where the foreground
	 * window may not be the active window, see {@link WinUtil#getForegroundWindow()
	 * getForegroundWindow}.
	 * 
	 * @return A window handle to the currently active window.
	 */
	public static HWND getActiveWindow()
	{
		return U32.GetActiveWindow();
	}
	
	/**
	 * Sets the active window to the given window. Unless you know what this does,
	 * it probably won't help you. In that case you should see
	 * {@link WinUtil#setForeground(HWND) setForeground}, which is probably
	 * something closer to what you want.
	 * 
	 * @param window
	 *            A window handle to the window being activated
	 * @return A window handle to the previously active window
	 */
	public static HWND setActiveWindow(HWND window)
	{
		if (window == null)
			return getActiveWindow();
		return U32X.SetActiveWindow(window);
	}
	
	/**
	 * Instead of iterating over windows, this method conveniently generates a
	 * handle to the current foreground window. For situations where the active
	 * window may not be the foreground window, see {@link WinUtil#getActiveWindow()
	 * getActiveWindow}.
	 * 
	 * @return A window handle to the current foreground window.
	 */
	public static HWND getForegroundWindow()
	{
		return U32.GetForegroundWindow();
	}
	
	/**
	 * Changes the Z-order of the given window and brings it to the foreground.
	 * 
	 * @param window
	 *            A window handle to the given window
	 */
	public static void setForeground(HWND window)
	{
		if (window == null)
			return;
		U32.SetForegroundWindow(window);
	}
	
	/**
	 * Convenience method returning a window handle of the desktop itself.
	 * 
	 * @return A window handle to the desktop "window".
	 */
	public static HWND getDesktop()
	{
		return U32.GetDesktopWindow();
	}
	
	/**
	 * Closes the given window by sending a "Close" message
	 * 
	 * @param window
	 *            A window handle to the given window
	 * @see WinUtil#quit
	 */
	public static void close(HWND window)
	{
		if (window == null)
			return;
		U32.PostMessage(window, WinUser.WM_CLOSE, null, null);
	}
	
	/**
	 * Closes the given window, forcibly. This could be useful for closing
	 * applications that prompt on close if you want to ignore such prompts.
	 * 
	 * @param window
	 *            A window handle to the given window
	 * @see WinUtil#close
	 */
	public static void quit(HWND window)
	{
		if (window == null)
			return;
		U32.PostMessage(window, WinUser.WM_QUIT, null, null);
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
				"Search Type provided but not implemented in WinUtil.matchesSearch!");
		}
	}
	
	/**
	 * Enumerates all windows and computes an operation to generate output
	 * 
	 * @param op
	 *            A mapping operation generating desired data of type {@code T} from
	 *            window handles
	 * @param <T>
	 *            The desired type of output data, generated by {@code op}
	 * @return All non-null results generated by applying {@code op} to all window
	 *         handles
	 */
	public static <T> List<T> enumerateWindows(Function<HWND, T> op)
	{
		var output = new ArrayList<T>();
		if (op == null)
			return output;
		U32.EnumWindows((HWND, Pointer) ->
		{
			T result = op.apply(HWND);
			if (result != null)
				output.add(result);
			return true;
		}, (Pointer)null);
		return output;
	}
	
	/**
	 * Gets the window text associated with the given window. Equivalent to
	 * {@link WinUtil#getTitle(HWND, int) getTitle(window,
	 * WinUtil.TITLE_SEARCH_LENGTH)}
	 * 
	 * @param window
	 *            A window handle to the given window
	 * @return A trimmed string that is the title of the given window, with maximum
	 *         {@code TITLE_SEARCH_LENGTH} characters
	 */
	public static String getTitle(HWND window)
	{
		return getTitle(window, TITLE_SEARCH_LENGTH);
	}
	
	/**
	 * Gets the window text associated with the given window
	 * 
	 * @param window
	 *            A window handle to the given window
	 * @param n
	 *            The maximum number of characters in the text buffer(not including
	 *            the null character)
	 * @return A trimmed string that is the title of the given window, with maximum
	 *         n characters
	 */
	public static String getTitle(HWND window, int n)
	{
		if (window == null || n <= 0)
			return "";
		var buffer = new char[n + 1];
		U32.GetWindowText(window, buffer, n + 1);
		return new String(buffer).trim();
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
		var handles = new ArrayList<HWND>();
		U32.EnumWindows((HWND, Pointer) ->
		{
			String title = getTitle(HWND, TITLE_SEARCH_LENGTH);
			if (matchesSearch(title, query, search))
			{
				handles.add(HWND);
			}
			return true;
		}, (Pointer)null);
		return handles;
	}
	
	/**
	 * Find a window with {@code query} in the first {@code TITLE_SEARCH_LENGTH}
	 * characters of its title
	 * 
	 * @param query
	 *            The query string to be searched for
	 * @param search
	 *            The query type on the title strings
	 * @return One window matched to the query string and type, or {@code null} if
	 *         there is no such window
	 */
	public static HWND getWindow(String query, SearchType search)
	{
		var handle = new HWND[1];
		U32.EnumWindows((HWND, Pointer) ->
		{
			String title = getTitle(HWND, TITLE_SEARCH_LENGTH);
			if (matchesSearch(title, query, search))
			{
				handle[0] = HWND;
				return false;
			}
			return true;
		}, (Pointer)null);
		return handle[0];
	}
	
	/**
	 * Captures an image associated with the current screen
	 * 
	 * @return The associated screen image
	 */
	public static BufferedImage capture()
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle region = new Rectangle(0, 0, screenSize.width, screenSize.height);
		return CaptureRobot.INSTANCE.screenshot(region);
	}
	
	/**
	 * Captures an image associated with the given window
	 * 
	 * @param window
	 *            A window handle to the given window
	 * @return The associated window image, excluding the frame
	 * @see WinUtil#capture(HWND, boolean)
	 */
	public static BufferedImage capture(HWND window)
	{
		return capture(window, false);
	}
	
	/**
	 * Captures an image associated with the given window. Be aware that in more
	 * recent versions of Windows, frames may not be visible, resulting in capture
	 * of extraneous elements.
	 * 
	 * @param window
	 *            A window handle to the given window
	 * @param fullWindow
	 *            Whether to include the frame in the image
	 * @return The associated window image, possibly including the frame
	 */
	public static BufferedImage capture(HWND window, boolean fullWindow)
	{
		if (window == null)
			return null;
		Rectangle bounds = getBounds(window, fullWindow);
		if (bounds.width == 0 || bounds.height == 0)
			return null;
		HDC dcWin = fullWindow ? U32X.GetWindowDC(window) : U32.GetDC(window);
		HDC dcMem = G32.CreateCompatibleDC(dcWin);
		HBITMAP bmp = G32.CreateCompatibleBitmap(dcWin, bounds.width, bounds.height);
		HANDLE hObj = G32.SelectObject(dcMem, bmp);
		G32.BitBlt(dcMem, 0, 0, bounds.width, bounds.height, dcWin, 0, 0, GDI32.SRCCOPY);
		G32.SelectObject(dcMem, hObj);
		G32.DeleteDC(dcMem);
		var bmi = new BITMAPINFO();
		bmi.bmiHeader.biWidth = bounds.width;
		bmi.bmiHeader.biHeight = -bounds.height;
		bmi.bmiHeader.biPlanes = 1;
		bmi.bmiHeader.biBitCount = 32;
		bmi.bmiHeader.biCompression = 0;
		var buffer = new Memory(bounds.width * bounds.height * 4L);
		G32.GetDIBits(dcWin, bmp, 0, bounds.height, buffer, bmi, 0);
		var image =
			new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, bounds.width, bounds.height,
			buffer.getIntArray(0L, bounds.width * bounds.height), 0, bounds.width);
		G32.DeleteObject(bmp);
		U32.ReleaseDC(window, dcWin);
		return image;
	}
	
	/**
	 * Captures an image associated with the current screen
	 * 
	 * @return The associated multi-resolution screen image
	 */
	public static MultiResolutionImage multiResolutionCapture()
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle region = new Rectangle(0, 0, screenSize.width, screenSize.height);
		return CaptureRobot.INSTANCE.multiResolutionScreenshot(region);
	}
	
	/**
	 * Gets a pixel on the screen as a Color
	 * 
	 * @param x
	 *            The x-coordinate of the pixel
	 * @param y
	 *            The y-coordinate of the pixel
	 * @return The pixel's Color
	 */
	public Color getPixel(int x, int y)
	{
		return CaptureRobot.INSTANCE.getPixel(x, y);
	}
	
	/**
	 * Captures a pixel associated with the given window
	 * 
	 * @param window
	 *            A window handle to the given window
	 * @param x
	 *            The x-coordinate of the pixel in the window image
	 * @param y
	 *            The y-coordinate of the pixel in the window image
	 * @return A pixel sampled from the associated window image, excluding the frame
	 */
	public static int getPixel(HWND window, int x, int y)
	{
		HDC dcWin = U32.GetDC(window);
		int pixel = G32X.GetPixel(dcWin, x, y);
		U32.ReleaseDC(window, dcWin);
		return (pixel & ~0xFF00FF) | ((pixel & 0xFF) << 16) | ((pixel & 0xFF0000) >> 16);
	}
	
	/**
	 * Gets a pixel on the screen in RGB integer format
	 * 
	 * @param x
	 *            The x-coordinate of the pixel on the screen
	 * @param y
	 *            The y-coordinate of the pixel on the screen
	 * @return The pixel's RGB value
	 */
	public int getPixelRGB(int x, int y)
	{
		return CaptureRobot.INSTANCE.getPixelRGB(x, y);
	}
	
	/**
	 * Gets the bounds for a given window, not including its frame
	 * 
	 * @param window
	 *            The handle for the given window
	 * @return A rectangle containing window bound information
	 * @see WinUtil#getBounds(HWND, boolean)
	 */
	public static Rectangle getBounds(HWND window)
	{
		var rect = new RECT();
		U32.GetClientRect(window, rect);
		return new Rectangle(rect.left, rect.top, rect.right - rect.left,
			rect.bottom - rect.top);
	}
	
	/**
	 * Gets the bounds for a given window, possibly including its frame. Be aware
	 * that in more recent versions of Windows, frames may not be visible, resulting
	 * in bounds that seem too large (this can be visualized through the
	 * {@link WinUtil#capture(HWND, boolean) capture} method with
	 * {@code fullWindow=true}).
	 * 
	 * @param window
	 *            The handle for the given window
	 * @param fullWindow
	 *            Whether or not to include the window frame in the window bounds
	 * @return A rectangle containing window bound information
	 */
	public static Rectangle getBounds(HWND window, boolean fullWindow)
	{
		var rect = new RECT();
		if (fullWindow)
			U32.GetWindowRect(window, rect);
		else
			U32.GetClientRect(window, rect);
		return new Rectangle(rect.left, rect.top, rect.right - rect.left,
			rect.bottom - rect.top);
	}
	
}
