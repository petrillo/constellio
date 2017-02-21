package com.constellio.app.ui.pages.management.sequence;

import com.constellio.app.extensions.sequence.AvailableSequence;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.records.AvailableSequencesServices;
import com.constellio.data.dao.services.sequence.SequencesManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.io.Serializable;
import java.util.List;

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

    public List<Record> getTableSequances() {
        List<AvailableSequence> listSequence = this.sequencesManager.getAvailableGlobalSequences();
        return null;
    }
}
