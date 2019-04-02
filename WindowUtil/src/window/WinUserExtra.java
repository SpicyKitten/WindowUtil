package window;

import com.sun.jna.platform.win32.WinUser;

/**
 * From JNA source, until the release is updated
 */
interface WinUserExtra extends WinUser
{
	/**
	 * Bitmask for the SHIFT key modifier.
	 */
	int MODIFIER_SHIFT_MASK = 1;
	/**
	 * Bitmask for the CTRL key modifier.
	 */
	int MODIFIER_CTRL_MASK = 2;
	/**
	 * Bitmask for the ALT key modifier.
	 */
	int MODIFIER_ALT_MASK = 4;
	/**
	 * Bitmask for the HANKAKU key modifier.
	 */
	int MODIFIER_HANKAKU_MASK = 8;
	/**
	 * Bitmask for the RESERVED1 key modifier.
	 */
	int MODIFIER_RESERVED1_MASK = 16;
	/**
	 * Bitmask for the RESERVED2 key modifier.
	 */
	int MODIFIER_RESERVED2_MASK = 32;
}
