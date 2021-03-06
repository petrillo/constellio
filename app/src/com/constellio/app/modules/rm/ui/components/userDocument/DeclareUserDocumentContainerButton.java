package com.constellio.app.modules.rm.ui.components.userDocument;

import static com.constellio.app.ui.i18n.i18n.$;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class DeclareUserDocumentContainerButton extends ContainerButton {

	@Override
	protected Button newButtonInstance(final Object itemId) {
		Button declareUserDocumentButton;
		final UserDocumentVO userDocumentVO = (UserDocumentVO) itemId;
		String fileName = userDocumentVO.getFileName();
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
		Resource icon = new ThemeResource("images/icons/folder/folder_into.png");
		if ("eml".equals(extension) || "msg".equals(extension)) {
			declareUserDocumentButton = new WindowButton(icon, $("ListUserDocumentsView.declareDocument"),
					$("ListUserDocumentsView.declareEmailWindowTitle"), true, WindowConfiguration.modalDialog("50%", "50%")) {
				@Override
				protected Component buildWindowContent() {
					return new DeclareEmailWindowContent(userDocumentVO);
				}
			};
		} else {
			return newDefaultClassifyButton(icon, userDocumentVO);
		}
		return declareUserDocumentButton;
	}

	private static class DeclareEmailWindowContent extends CustomComponent {

		public DeclareEmailWindowContent(final UserDocumentVO userDocumentVO) {
			super();

			setHeight("100%");

			VerticalLayout mainLayout = new VerticalLayout();
			mainLayout.setSpacing(true);
			mainLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

			Button declareEmailButton = new BaseButton($("ListUserDocumentsView.declareEmail")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					String userDocumentId = userDocumentVO.getId();
					ConstellioUI.getCurrent().navigateTo().declareUserDocument(userDocumentId);
					for (Window window : ConstellioUI.getCurrent().getWindows()) {
						window.close();
					}
				}
			};
			declareEmailButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

			Button declareEmailAttachmentsButton = new BaseButton($("ListUserDocumentsView.declareEmailAttachments")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					String userDocumentId = userDocumentVO.getId();
					ConstellioUI.getCurrent().navigateTo().addEmailAttachmentsToFolder(userDocumentId);
					for (Window window : ConstellioUI.getCurrent().getWindows()) {
						window.close();
					}
				}
			};

			mainLayout.addComponents(declareEmailButton, declareEmailAttachmentsButton);
			setCompositionRoot(mainLayout);
		}
	}

	protected Button newDefaultClassifyButton(Resource icon, final UserDocumentVO userDocumentVO) {
		return new BaseButton($("ListUserDocumentsView.declareDocument")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				String userDocumentId = userDocumentVO.getId();
				ConstellioUI.getCurrent().navigateTo().declareUserDocument(userDocumentId);
				for (Window window : ConstellioUI.getCurrent().getWindows()) {
					window.close();
				}

			}

		};
	}
}
