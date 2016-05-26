package com.constellio.app.ui.pages.search;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

public class SearchSimilarDocumentPresenter extends SearchPresenter<SearchSimilarDocumentView> {
    private Record document;
    private MetadataSchemaType schemaType;

    public SearchSimilarDocumentPresenter(SearchSimilarDocumentView view) {
        super(view);
        schemaType = schemaType(Document.SCHEMA_TYPE);

    }

    @Override
    public Record getTemporarySearchRecord() {
        return null;
    }

    @Override
    public SearchPresenter<SearchSimilarDocumentView> forRequestParameters(String params) {
        document = null;
        if (StringUtils.isNotBlank(params)) {
            String[] parts = params.split("/", 0);
            if (parts.length == 1) {
                document = recordServices().getDocumentById(parts[0]);

            }
        }
        return this;
    }

    @Override
    public boolean mustDisplayResults() {
        return document != null;
    }

    @Override
    public int getPageNumber() {
        return 1;
    }

    @Override
    public void setPageNumber(int pageNumber) {

    }

    @Override
    public void suggestionSelected(String suggestion) {

    }

    @Override
    public List<MetadataVO> getMetadataAllowedInSort() {
        List<MetadataSchemaType> schemaTypes = allowedSchemaTypes();
        switch (schemaTypes.size()) {
            case 0:
                return new ArrayList<>();
            case 1:
                return getMetadataAllowedInSort(schemaTypes.get(0).getCode());
            default:
                return getCommonMetadataAllowedInSort(schemaTypes);
        }
    }

    private List<MetadataVO> getCommonMetadataAllowedInSort(List<MetadataSchemaType> schemaTypes) {
        List<MetadataVO> result = new ArrayList<>();
        for (MetadataVO metadata : getMetadataAllowedInSort(schemaTypes.get(0))) {
            String localCode = MetadataVO.getCodeWithoutPrefix(metadata.getCode());
            if (isMetadataInAllTypes(localCode, schemaTypes)) {
                result.add(metadata);
            }
        }
        return result;
    }

    private boolean isMetadataInAllTypes(String localCode, List<MetadataSchemaType> types) {
        for (MetadataSchemaType each : types) {
            try {
                each.getMetadataWithAtomicCode(localCode);
            } catch (MetadataSchemasRuntimeException.NoSuchMetadataWithAtomicCode e) {
                return false;
            }
        }
        return true;
    }


    @Override
    protected LogicalSearchCondition getSearchCondition() {
        if (allowedSchemaTypes().isEmpty()) {
            return fromAllSchemasIn(view.getCollection()).returnAll();
        } else {
            return from(allowedSchemaTypes()).returnAll();
        }
    }

    private List<MetadataSchemaType> allowedSchemaTypes() {
        List<MetadataSchemaType> result = new ArrayList<>();
        for (MetadataSchemaType type : types().getSchemaTypes()) {
            SchemaTypeDisplayConfig config = schemasDisplayManager()
                    .getType(view.getSessionContext().getCurrentCollection(), type.getCode());
            if (config.isSimpleSearch()) {
                result.add(type);
            }
        }
        return result;
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }

    @Override
    protected boolean isReturnSimilarDocuments() {
        return true;
    }

    @Override
    protected void saveTemporarySearch(boolean refreshPage) {

    }

    @Override
    protected LogicalSearchCondition getSimilarityQuery() {
        LogicalSearchCondition condition = from(schemaType)
                .where(Schemas.IDENTIFIER).isEqualTo(document.getId())
                .andWhere(Schemas.COLLECTION).isEqualTo(collection);
        return condition;
    }
}
