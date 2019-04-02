package window;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT.HANDLE;

/**
 * From JNA source, until the release is updated
 */
interface WinDefExtra extends WinDef
{
	/**
	 * Handle to a input locale identifier (formerly called keyboard layout handle).
	 */
	public static class HKL extends HANDLE
	{
		
		/**
		 * Instantiates a new hkl.
		 */
		public HKL()
		{
			
		}
		
		/**
		 * Instantiates a new hkl.
		 *
		 * @param p
		 *            the p
		 */
		public HKL(Pointer p)
		{
			super(p);
		}
		
		public HKL(int i)
		{
			super(Pointer.createConstant(i));
		}
		
		/**
		 * Get the low word (unsigned short).
		 */
		public int getLanguageIdentifier()
		{
			return (int)(Pointer.nativeValue(getPointer()) & 0xFFFF);
		}
		
		public int getDeviceHandle()
		{
			return (int)(Pointer.nativeValue(getPointer()) >> 16 & 0xFFFF);
		}
		
		@Override
		public String toString()
		{
			return String.format("%08x", Pointer.nativeValue(getPointer()));
		}
	}
}
