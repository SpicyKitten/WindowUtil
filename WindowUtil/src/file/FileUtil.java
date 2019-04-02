package file;

import java.io.File;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.VerRsrc.VS_FIXEDFILEINFO;
import com.sun.jna.platform.win32.Version;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Provides convenience methods for files
 */
public class FileUtil
{
	
	/**
	 * Convenience method generating a lower-case absolute path to a file.
	 * 
	 * @param relativePath
	 *            A relative path to a file
	 * @return An absolute path to a file, in lower-case
	 */
	public static String filePath(String relativePath)
	{
		File f = new File(relativePath);
		return f.getAbsolutePath().toLowerCase();
	}
	
	/**
	 * Returns version info for a given file.
	 * 
	 * @param relativePath
	 *            A relative path to the given file
	 * @return A string encoding version info for the given file
	 * @see <a href=
	 *      "http://stackoverflow.com/questions/6918022/get-version-info-for-exe">StackOverflow:
	 *      Get version info</a>
	 */
	public static String versionInfo(String relativePath)
	{
		String absPath = filePath(relativePath);
		IntByReference dwDummy = new IntByReference(0);
		Version ver = Version.INSTANCE;
		int versionLen = ver.GetFileVersionInfoSize(absPath, dwDummy);
		byte[] bufferarray = new byte[versionLen];
		Pointer lpData = new Memory(bufferarray.length);
		PointerByReference lplpBuffer = new PointerByReference();
		IntByReference puLen = new IntByReference();
		ver.GetFileVersionInfo(absPath, 0, versionLen, lpData);
		ver.VerQueryValue(lpData, "\\", lplpBuffer, puLen);
		var lplpBufStructure = new VS_FIXEDFILEINFO(lplpBuffer.getValue());
		lplpBufStructure.read();
		int verMS = lplpBufStructure.dwFileVersionMS.intValue();
		int verLS = lplpBufStructure.dwFileVersionLS.intValue();
		return String.format("%d.%d.%d.%d", verMS >> 16, verMS & 0xffff, verLS >> 16,
			verLS & 0xffff);
	}
}
