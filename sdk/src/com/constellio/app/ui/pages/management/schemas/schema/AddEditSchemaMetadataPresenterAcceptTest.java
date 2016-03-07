package com.constellio.app.ui.pages.management.schemas.schema;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEnabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSortable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeCustomSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class AddEditSchemaMetadataPresenterAcceptTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	ZeCustomSchemaMetadatas zeCustomSchema = setup.new ZeCustomSchemaMetadatas();
	AddEditSchemaMetadataPresenter presenter;
	MetadataSchemasManager metadataSchemasManager;
	Map<String, String> parameters;
	@Mock AddEditSchemaMetadataView view;
	@Mock CoreViews navigator;
	SessionContext sessionContext;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection()
		);
		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		defineSchemasManager()
				.using(setup.andCustomSchema().withAStringMetadataInCustomSchema(whichIsMultivalue, whichIsSearchable)
						.withAStringMetadata(whichIsSortable, whichIsEnabled).withABooleanMetadata(whichIsEnabled)
						.withADateMetadata(whichIsEnabled));
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigateTo()).thenReturn(navigator);

		presenter = new AddEditSchemaMetadataPresenter(view);
		parameters = new HashMap<>();
		parameters.put("schemaTypeCode", setup.anotherSchemaTypeCode());
		parameters.put("schemaCode", setup.anotherDefaultSchemaCode());
		presenter.setParameters(parameters);
	}

	@Test
	public void whenAddButtonClickedThenNavigateToAddEditMetadataWithCorrectParams()
			throws Exception {

		presenter.setSchemaCode(setup.anotherDefaultSchemaCode());

		presenter.addButtonClicked();
		String params = "metadataCode=;schemaCode=anotherSchemaType_default;schemaTypeCode=anotherSchemaType";
		verify(view.navigateTo()).addMetadata("editMetadata/" + URLEncoder.encode(params));
	}

	@Test
	public void whenEditButtonClickedThenNavigateToAddEditMetadataWithCorrectParams()
			throws Exception {

		presenter.setSchemaCode(setup.anotherDefaultSchemaCode());
		MetadataVO metadataVO = new MetadataToVOBuilder()
				.build(setup.anotherDefaultSchema().getMetadata("anotherSchemaType_default_title"), null, sessionContext);

		presenter.editButtonClicked(metadataVO);

		String params = "metadataCode=anotherSchemaType_default_title;schemaCode=anotherSchemaType_default;schemaTypeCode=anotherSchemaType";
		verify(view.navigateTo()).editMetadata("editMetadata/" + URLEncoder.encode(params));
	}

	@Test
	public void whenBackButtonClickedThenNavigateToAddEditMetadataWithCorrectParams()
			throws Exception {
		presenter.setSchemaCode(setup.anotherDefaultSchemaCode());

		presenter.backButtonClicked();

		String params = "schemaCode=anotherSchemaType_default;schemaTypeCode=anotherSchemaType";

		verify(view.navigateTo()).listSchema("editSchema/" + URLEncoder.encode(params));
	}
}
