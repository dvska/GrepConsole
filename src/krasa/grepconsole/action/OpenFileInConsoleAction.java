package krasa.grepconsole.action;

import java.io.*;
import java.nio.charset.Charset;

import com.intellij.compiler.server.BuildManager;
import krasa.grepconsole.tail.TailContentExecutor;

import com.intellij.execution.impl.ConsoleBuffer;
import com.intellij.execution.process.*;
import com.intellij.ide.util.BrowseFilesListener;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Vojtech Krasa
 */
public class OpenFileInConsoleAction extends DumbAwareAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		final Project project = e.getProject();
		final FileChooserDialog fileChooser = FileChooserFactory.getInstance().createFileChooser(
				BrowseFilesListener.SINGLE_FILE_DESCRIPTOR, project, null);

		final VirtualFile[] choose = fileChooser.choose(null, project);
		if (choose.length > 0) {
			final VirtualFile virtualFile = choose[0];
			final String path1 = virtualFile.getPath();
			openFileInConsole(project, new File(path1));
		}
	}

	public void openFileInConsole(final Project project, final File file) {
		final Process process = new MyProcess(file);

		final ProcessHandler osProcessHandler = new BaseOSProcessHandler(process, null, Charset.defaultCharset()) {
			@Override
			public boolean isSilentlyDestroyOnClose() {
				return true;
			}
		};
		try {
			osProcessHandler.putUserDataIfAbsent(BuildManager.ALLOW_AUTOMAKE, true);
		} catch (NoClassDefFoundError e) {
			//phpstorm does not have it
		}
		final TailContentExecutor executor = new TailContentExecutor(project, osProcessHandler);
		Disposer.register(project, executor);
		executor.withRerun(new Runnable() {
			@Override
			public void run() {
				osProcessHandler.destroyProcess();
				osProcessHandler.waitFor(2000L);
				openFileInConsole(project, file);
			}
		});
		executor.withTitle(file.getName());
		executor.run();
	}

	private class MyProcess extends Process {
		protected volatile boolean running = true;
		protected FileInputStream inputStream;

		private MyProcess(final File file) {
			try {
				inputStream = new FileInputStream(file);
				long size = inputStream.getChannel().size();
				// close enough, it does not work for binary files very well, but i hope it does at least for text
				inputStream.getChannel().position(Math.max(size - ConsoleBuffer.getCycleBufferSize(), 0));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public OutputStream getOutputStream() {
			return new OutputStream() {
				@Override
				public void write(int b) throws IOException {

				}
			};
		}

		@Override
		public InputStream getInputStream() {
			return inputStream;

		}

		@Override
		public InputStream getErrorStream() {
			return new InputStream() {
				@Override
				public int read() throws IOException {
					return 0;
				}
			};
		}

		@Override
		public int waitFor() throws InterruptedException {
			while (running) {
				Thread.sleep(1000);
			}
			return 0;
		}

		@Override
		public int exitValue() {
			return 0;
		}

		@Override
		public void destroy() {
			try {
				inputStream.close();
			} catch (IOException e) {
				// who cares
			} finally {
				running = false;
			}
		}
	}

}
