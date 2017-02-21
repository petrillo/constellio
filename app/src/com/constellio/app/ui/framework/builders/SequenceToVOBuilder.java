package com.constellio.app.ui.framework.builders;

import com.constellio.app.extensions.sequence.AvailableSequence;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.SequenceVO;
import com.constellio.app.ui.pages.management.sequence.SequenceServices;
import com.constellio.model.entities.Language;

import java.io.Serializable;

/**
 * Created by Marco on 2017-02-21.
 */
public class SequenceToVOBuilder implements Serializable {
    public SequenceVO build(AvailableSequence sequence) {
        return new SequenceVO(sequence.getCode(), sequence.getTitles().get(Language.French), sequence.getCurrentValue());
    }
}
