package krasa.grepconsole.remotecall;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import krasa.grepconsole.remotecall.handler.OpenFileInConsoleMessageHandler;
import krasa.grepconsole.remotecall.notifier.MessageNotifier;
import krasa.grepconsole.remotecall.notifier.SocketMessageNotifier;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.SystemProperties;

public class GrepConsoleRemoteCallComponent implements ApplicationComponent {
	private static final Logger log = Logger.getInstance(GrepConsoleRemoteCallComponent.class);
	public static final String GREPCONSOLE_REMOTE_CALL_PORT = "grepconsole.remote.call.port";
	public static final int DEFAULT_VALUE = 8092;

	private ServerSocket serverSocket;
	private Thread listenerThread;

	public void initComponent() {
		final int port = SystemProperties.getIntProperty(GREPCONSOLE_REMOTE_CALL_PORT, DEFAULT_VALUE);
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("localhost", port));
			log.info("Listening " + port);
		} catch (IOException e) {
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {
					Messages.showMessageDialog(
							"Can't bind with "
									+ port
									+ " port. GrepConsole plugin Windows integration won't work (to change port, set property grepconsole.remote.call.port)",
							"GrepConsole Plugin Error", Messages.getErrorIcon());
				}
			});
			return;
		}

		MessageNotifier messageNotifier = new SocketMessageNotifier(serverSocket);
		messageNotifier.addMessageHandler(new OpenFileInConsoleMessageHandler());
		listenerThread = new Thread(messageNotifier);
		listenerThread.start();
	}

	public void disposeComponent() {
		try {
			if (listenerThread != null) {
				listenerThread.interrupt();
			}
			serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	public String getComponentName() {
		return "GrepConsoleRemoteCallComponent";
	}
}
