package com.constellio.app.ui.pages.management.sequence;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.UIContext;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import java.util.List;

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
        presenter.getTableSequances();
        return new VerticalLayout();
    }
}
