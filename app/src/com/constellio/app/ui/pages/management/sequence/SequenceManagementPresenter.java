package com.constellio.app.ui.pages.management.sequence;

import com.constellio.app.extensions.sequence.AvailableSequence;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.records.AvailableSequencesServices;
import com.constellio.app.ui.entities.SequenceVO;
import com.constellio.app.ui.framework.builders.SequenceToVOBuilder;
import com.constellio.data.dao.services.sequence.SequencesManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Marco on 2017-02-21.
 */
public class SequenceManagementPresenter implements Serializable {
    private AppLayerFactory appLayerFactory;
    private ModelLayerFactory modelLayerFactory;
    private String collection;
    private SequenceServices sequenceServices;
    private SequenceManagementView view;
    private AvailableSequencesServices sequencesManager;

    public SequenceManagementPresenter(AppLayerFactory appLayerFactory, String collection, SequenceManagementView view) {
        this.appLayerFactory = appLayerFactory;
        this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
        this.collection = collection;
        this.view = view;
        this.sequenceServices = new SequenceServices(ConstellioFactories.getInstance(), this.view.getSessionContext());
        this.sequencesManager = new AvailableSequencesServices(appLayerFactory);
    }

    public List<SequenceVO> getTableSequances() {
        List<AvailableSequence> listSequence = this.sequencesManager.getAvailableGlobalSequences();
        List<SequenceVO> result = new ArrayList<>();
        SequenceToVOBuilder builder = new SequenceToVOBuilder();
        for (AvailableSequence sequence : listSequence) {
            sequence.setCurrentValue(sequenceServices.getLastSequenceValue(sequence.getCode()));
            result.add(builder.build(sequence));
        }
        return result;
    }

    public void editButtonClicked(String code) {

    }

    public void displayButtonClicked(SequenceVO sequenceVO) {

    }
}
