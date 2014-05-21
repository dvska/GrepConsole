package krasa.grepconsole.integration.windows;

import krasa.grepconsole.remotecall.GrepConsoleRemoteCallComponent;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.SystemProperties;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

/**
 * @author Vojtech Krasa
 */
public class WindowsRegistryChange {
	private static final Logger log = Logger.getInstance(WindowsRegistryChange.class);

	public static void main(String[] args) {
		final String jarPath = "F:\\workspace\\_projekty\\Github\\GrepConsole\\lib\\http-client.jar";
		setup(jarPath);
	}

	public static void setup(String jarPath) {
		final int port = SystemProperties.getIntProperty(GrepConsoleRemoteCallComponent.GREPCONSOLE_REMOTE_CALL_PORT,
				GrepConsoleRemoteCallComponent.DEFAULT_VALUE);
		log.info("registering " + jarPath + ",  port=" + port);
		// Read a string
		System.out.printf("Product Name: %s\n", Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
				"SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion", "ProductName"));

		// // Create a key and write a string
		Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, "Software\\Classes\\*\\shell");
		Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, "Software\\Classes\\*\\shell\\GrepConsole");
		Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Classes\\*\\shell\\GrepConsole", null,
				"Open in IntelliJ console");
		Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, "Software\\Classes\\*\\shell\\GrepConsole\\command");

		Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER,
				"Software\\Classes\\*\\shell\\GrepConsole\\command", null, "javaw -jar " + jarPath + " " + port + " %1");
	}

}
