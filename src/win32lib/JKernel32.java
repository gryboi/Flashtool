package win32lib;

import java.io.IOException;

import org.logger.MyLogger;
import org.system.Device;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinBase.OVERLAPPED;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;

public class JKernel32 {

	public static Kernel32RW kernel32 = (Kernel32RW) Native.loadLibrary("kernel32", Kernel32RW.class, W32APIOptions.UNICODE_OPTIONS);
	static WinNT.HANDLE HandleToDevice = WinBase.INVALID_HANDLE_VALUE;

	public static boolean openDevice() {
        /* Kernel32RW.GENERIC_READ | Kernel32RW.GENERIC_WRITE not used in dwDesiredAccess field for system devices such a keyboard or mouse */
        int shareMode = WinNT.FILE_SHARE_READ | WinNT.FILE_SHARE_WRITE;
        int Access = WinNT.GENERIC_WRITE | WinNT.GENERIC_READ;
		HandleToDevice = Kernel32.INSTANCE.CreateFile(
                Device.getConnectedDevice().getDevPath(), 
                Access, 
                shareMode, 
                null, 
                WinNT.OPEN_EXISTING, 
                0,//WinNT.FILE_FLAG_OVERLAPPED, 
                (WinNT.HANDLE)null);
		return (HandleToDevice != WinBase.INVALID_HANDLE_VALUE);
	}
	
	public static byte[] readBytes() throws IOException {
		System.out.println("Reading file");
		int bufsize=65536;
		IntByReference nbread = new IntByReference();
		byte[] b = new byte[bufsize];
		boolean result = kernel32.ReadFile(HandleToDevice, b, bufsize, nbread, null);
		if (!result) {
			System.out.println(getLastError());
		}
		//kernel32.CancelIo(HandleToDevice);
		System.out.println("End Reading file. Read "+nbread.getValue());
		return getReply(b,nbread.getValue());
	}

	private static byte[] getReply(byte[] reply, int nbread) {
		byte[] newreply=null;
		if (nbread > 0) {
			newreply = new byte[nbread];
			System.arraycopy(reply, 0, newreply, 0, nbread);
		}
		return newreply;
	}
	
	public static boolean writeBytes(byte bytes[]) throws IOException {
		IntByReference nbwritten = new IntByReference();
		boolean result = kernel32.WriteFile(HandleToDevice, bytes, bytes.length, nbwritten, null);
		if (!result) {
			System.out.println(getLastError());
		}
		//kernel32.CancelIo(HandleToDevice);
		return result;
	}

	public static boolean closeDevice() {
		boolean result = true;
		
		if (HandleToDevice != WinBase.INVALID_HANDLE_VALUE) {
			MyLogger.getLogger().info("Closing USB device");
			result = kernel32.CloseHandle(HandleToDevice);
		}
		HandleToDevice = WinBase.INVALID_HANDLE_VALUE;
		return result;
	}
	
	public static int getLastErrorCode() {
		return Kernel32.INSTANCE.GetLastError();
	}
	
	public static String getLastError() {
		int code = Kernel32.INSTANCE.GetLastError();
	    Kernel32 lib = Kernel32.INSTANCE;
	    PointerByReference pref = new PointerByReference();
	    /*OVERLAPPED ov = new OVERLAPPED();
	    ov.Offset=0;
	    ov.OffsetHigh=0;
	    kernel32.CreateEvent(null, arg1, arg2, arg3)*/
	    lib.FormatMessage(
	        WinBase.FORMAT_MESSAGE_ALLOCATE_BUFFER | WinBase.FORMAT_MESSAGE_FROM_SYSTEM | WinBase.FORMAT_MESSAGE_IGNORE_INSERTS, 
	        null, 
	        code, 
	        0, 
	        pref, 
	        0, 
	        null);
	    String s = code + " : " +pref.getValue().getString(0, !Boolean.getBoolean("w32.ascii"));
	    lib.LocalFree(pref.getValue());
	    return s.replaceAll("[\n\r]+"," ");
	}

}
