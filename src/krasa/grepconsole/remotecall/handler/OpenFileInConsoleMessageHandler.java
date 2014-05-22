package krasa.grepconsole.remotecall.handler;

import java.awt.*;
import java.util.*;
import java.util.List;

import krasa.grepconsole.action.OpenFileInConsoleAction;
import krasa.grepconsole.utils.FocusUtils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.openapi.wm.impl.IdeFrameImpl;

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
					String selectedProject = null;
					if (values.size() > 1) {
						String initialProject = getInitiallySelectedProject(values);

						int i = Messages.showChooseDialog("Select Project Frame", "Select Project Frame",
								values.toArray(new String[values.size()]), initialProject, Messages.getQuestionIcon());
						if (i >= 0) {
							selectedProject = values.get(i);
						}

					} else if (values.size() == 1) {
						selectedProject = values.get(0);
					} else {
						log.warn("Cannot open file, no projects opened");
						return;
					}
					if (selectedProject != null) {
						lastProject = selectedProject;
						Project project = getProject(allProjectFrames, selectedProject);
						if (project != null) {
							new OpenFileInConsoleAction().openFileInConsole(project, message);
						}
					}
				}

				public String getInitiallySelectedProject(List<String> values) {
					WindowManagerEx instance = (WindowManagerEx) WindowManager.getInstance();
					Window focusedWindow = instance.getMostRecentFocusedWindow();
					String initialProject = null;
					if (focusedWindow instanceof IdeFrameImpl) {
						Project project = ((IdeFrameImpl) focusedWindow).getProject();
						if (project != null) {
							initialProject = project.getName();
							FocusUtils.requestFocus(project);
						}
					}

					if (initialProject == null) {
						initialProject = lastProject;
					}
					if (initialProject == null || !values.contains(initialProject)) {
						initialProject = values.get(0);
					}
					return initialProject;
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
					Collections.sort(values);
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
