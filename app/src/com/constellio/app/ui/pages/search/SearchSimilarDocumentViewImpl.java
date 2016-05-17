package com.constellio.app.ui.pages.search;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class SearchSimilarDocumentViewImpl extends SearchViewImpl<SearchSimilarDocumentPresenter> implements SearchSimilarDocumentView {

    public SearchSimilarDocumentViewImpl(){
        presenter = new SearchSimilarDocumentPresenter(this);

    }

    @Override
    protected Component buildSearchUI() {
        return new VerticalLayout();
    }

    @Override
    public Boolean computeStatistics() {
        return false;
    }
}
