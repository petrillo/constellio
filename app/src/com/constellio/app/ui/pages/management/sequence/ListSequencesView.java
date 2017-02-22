package com.constellio.app.ui.pages.management.sequence;

import java.util.List;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SequenceVO;
import com.constellio.app.ui.pages.base.BaseView;

public interface ListSequencesView extends BaseView {
	
	String getRecordId();

	RecordVO getRecordVO();
	
	void setSequenceVOs(List<SequenceVO> sequenceVOs);

	List<SequenceVO> getSequanceVOs();
	
	void showErrorMessage(String message);
	
	void closeWindow();

}
