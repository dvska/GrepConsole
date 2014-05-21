package krasa.grepconsole.remotecall.handler;

import java.util.ArrayList;
import java.util.List;

import krasa.grepconsole.action.OpenFileInConsoleAction;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;

/**
 * @author Vojtech Krasa
 */

public class OpenFileInConsoleMessageHandler implements MessageHandler {
	private static final Logger log = Logger.getInstance(OpenFileInConsoleMessageHandler.class);

	protected String lastProject;

	public void handleMessage(final String message) {
		if (message != null && !message.isEmpty()) {
			log.info("Opening file=" + message);
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {
					IdeFrame[] allProjectFrames = WindowManager.getInstance().getAllProjectFrames();
					List<String> values = getValues(allProjectFrames);
					final String s;
					if (values.size() > 1) {
						if (lastProject == null) {
							lastProject = values.get(0);
						}
						s = Messages.showEditableChooseDialog("Project frame", "Project Frame",
								Messages.getQuestionIcon(), values.toArray(new String[values.size()]), lastProject,
								new NonEmptyInputValidator());
					} else if (values.size() == 1) {
						s = values.get(0);
					} else {
						log.warn("Cannot open file, no projects opened");
						return;
					}
					if (s != null) {
						lastProject = s;
						Project project1 = getProject(allProjectFrames, s);
						if (project1 != null) {
							new OpenFileInConsoleAction().openFileInConsole(project1, message);
						}
					}
				}

				private List<String> getValues(IdeFrame[] allProjectFrames) {
					List<String> values = new ArrayList<String>();
					for (int i = 0; i < allProjectFrames.length; i++) {
						IdeFrame allProjectFrame = allProjectFrames[i];
						final Project project = allProjectFrame.getProject();
						if (project != null) {
							values.add(project.getName());
						}
					}
					return values;
				}

				private Project getProject(IdeFrame[] allProjectFrames, String projectName) {
					Project project1 = null;
					for (int i = 0; i < allProjectFrames.length; i++) {
						IdeFrame allProjectFrame = allProjectFrames[i];
						final Project project = allProjectFrame.getProject();
						if (project != null && projectName.equals(project.getName())) {
							project1 = project;
							break;
						}
					}
					return project1;
				}
			});
		}
	}
}
