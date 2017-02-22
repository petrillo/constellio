package com.constellio.app.ui.pages.management.sequence;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.SequenceVO;
import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.ListSequencesButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Marco on 2017-02-20.
 */
public class SequenceManagementViewImpl extends BaseViewImpl implements SequenceManagementView {
    private SequenceManagementPresenter presenter;

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        presenter = new SequenceManagementPresenter(getConstellioFactories().getAppLayerFactory(), getCollection(), this);
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        Container elements = new BeanItemContainer<>(SequenceVO.class, presenter.getTableSequances());
        ButtonsContainer buttonsContainer = new ButtonsContainer(elements, "buttons");

        addButtons(buttonsContainer);
        elements = buttonsContainer;

        BaseTable table = new BaseTable("SequenceListView.tableTitle", $("SequenceListView.tableTitle", elements.size()), elements);
        table.setPageLength(elements.size());
        table.setVisibleColumns("sequenceTitle", "buttons");
        table.setColumnHeader("sequenceTitle", $("ListTaxonomyView.titleColumn"));
        table.setColumnHeader("buttons", "");
        table.setColumnWidth("buttons", 88);
        table.setWidth("100%");
        return table;
    }

    @Override
    protected Button.ClickListener getBackButtonClickListener() {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                navigateTo().adminModule();
            }
        };
    }

    private void addButtons(ButtonsContainer buttonsContainer) {
        buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
            @Override
            protected Button newButtonInstance(final Object itemId) {
                return new EditButton() {
                    @Override
                    protected void buttonClick(ClickEvent event) {
                        SequenceVO sequenceVO = (SequenceVO) itemId;
                        ListSequencesViewImpl view = new ListSequencesViewImpl(sequenceVO);
                        System.out.println("button Click");
                        BaseWindow window = new BaseWindow(view.getCaption());
                        window.setId(WindowButton.WINDOW_STYLE_NAME);
                        window.addStyleName(WindowButton.WINDOW_STYLE_NAME);
                        window.setModal(true);
                        window.setResizable(false);
                        window.setWidth("800");
                        window.setHeight("500");
                        Component windowContent = view.buildMainComponent(null);
                        windowContent.addStyleName(WindowButton.WINDOW_CONTENT_STYLE_NAME);
                        if (windowContent instanceof BaseViewImpl) {
                            ((BaseViewImpl) windowContent).enter(null);
                        }
                        window.setContent(windowContent);
                        UI.getCurrent().addWindow(window);
                    }
                };
            }
        });
    }
}
