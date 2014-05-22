package krasa.grepconsole.integration.windows;

import com.intellij.openapi.diagnostic.Logger;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

/**
 * @author Vojtech Krasa
 */
public class WindowsRegistryChange {
	private static final Logger log = Logger.getInstance(WindowsRegistryChange.class);

	public static final String GREP_CONSOLE = "GrepConsoleIntelliJPlugin";
	public static final String HTTP_CLIENT_JAR = "GrepConsole-http-client.jar";

	public static final String CLASSES = "Software\\Classes\\*\\shell\\";
	public static final String CLASSES_GREPCONSOLE = CLASSES + GREP_CONSOLE;
	public static final String CLASSES_GREPCONSOLE_COMMAND = CLASSES_GREPCONSOLE + "\\command";
	public static final WinReg.HKEY CURRENT_USER = WinReg.HKEY_CURRENT_USER;

	public static void main(String[] args) {
		final String jarPath = "F:\\workspace\\_projekty\\Github\\" + GREP_CONSOLE + "\\lib\\" + HTTP_CLIENT_JAR;
		setup(jarPath, 8093);
	}

	public static void setup(String jarPath, final int port) {
		log.info("registering " + jarPath + ",  port=" + port);

		Advapi32Util.registryCreateKey(CURRENT_USER, "Software\\Classes\\*\\shell");
		Advapi32Util.registryCreateKey(CURRENT_USER, CLASSES_GREPCONSOLE);
		Advapi32Util.registrySetStringValue(CURRENT_USER, CLASSES_GREPCONSOLE, null, "Open in IntelliJ console");
		Advapi32Util.registryCreateKey(CURRENT_USER, CLASSES_GREPCONSOLE_COMMAND);
		Advapi32Util.registrySetStringValue(CURRENT_USER, CLASSES_GREPCONSOLE_COMMAND, null, getCommand(jarPath, port));
	}

	public static String getCommand(String jarPath, int port) {
		return "javaw -jar " + jarPath + " " + port + " %1";
	}

	public static boolean isSetupped(String jarPath, final int port) {
		if (!Advapi32Util.registryKeyExists(CURRENT_USER, CLASSES_GREPCONSOLE)) {
			return false;
		}

		String s = Advapi32Util.registryGetStringValue(CURRENT_USER, CLASSES_GREPCONSOLE_COMMAND, null);

		return s.equals(getCommand(jarPath, port));
	}

	public static void remove() {
		Advapi32Util.registryDeleteKey(CURRENT_USER, CLASSES_GREPCONSOLE_COMMAND);
		Advapi32Util.registryDeleteKey(CURRENT_USER, CLASSES_GREPCONSOLE);
	}
}
