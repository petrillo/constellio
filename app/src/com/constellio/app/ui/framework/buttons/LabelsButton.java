package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.ReportUtils;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.LabelViewer;
import com.constellio.model.entities.records.Content;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.server.Page;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import org.apache.commons.io.FileUtils;

public class LabelsButton extends WindowButton {
    @PropertyId("startPosition")
    private ComboBox startPosition;
    @PropertyId("labelConfigurations")
    private ComboBox format;
    @PropertyId("numberOfCopies")
    private TextField copies;
    private ModelLayerFactory model;
    private String type;
    private SearchServices ss;
    private RMSchemasRecordsServices rm;
    private String collection;
    private List<String> ids;
    private AppLayerFactory factory;
    private ContentManager contentManager;
    private double size;
    private String user;

    public LabelsButton(String caption, String windowsCaption, AppLayerFactory factory, String collection, String type, String id, String user) {
        this(caption, windowsCaption, factory, collection, type, Arrays.asList(id), user);
    }

    public LabelsButton(String caption, String windowsCaption, AppLayerFactory factory, String collection, String type, List<String> idObject, String user) {
        super(caption, windowsCaption, WindowConfiguration.modalDialog("75%", "75%"));
        this.model = factory.getModelLayerFactory();
        this.collection = collection;
        this.factory = factory;
        this.ss = model.newSearchServices();
        this.type = type;
        this.ids = idObject;
        this.rm = new RMSchemasRecordsServices(this.collection, factory);
        this.contentManager = model.getContentManager();
        this.size = 0;
        this.user = user;
    }

    @Override
    protected Component buildWindowContent() {
        startPosition = new ComboBox($("LabelsButton.startPosition"));

        startPosition.setNullSelectionAllowed(false);

        List<PrintableLabel> configurations = getTemplates(type);
        if (configurations.size() > 0) {
            this.size = (Double) configurations.get(0).get(PrintableLabel.LIGNE) * (Double) configurations.get(0).get(PrintableLabel.COLONNE);
            startPosition.clear();
            for (int i = 1; i <= size; i++) {
                startPosition.addItem(i);
            }
        }

        format = new ComboBox($("LabelsButton.labelFormat"));
        for (PrintableLabel configuration : configurations) {
            format.addItem(configuration);
            format.setItemCaption(configuration, configuration.getTitle());
        }
        if (configurations.size() > 0) {
            format.select(configurations.get(0));
        }
        format.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
        format.setNullSelectionAllowed(false);
        format.setValue(configurations.get(0));
        format.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                PrintableLabel report = (PrintableLabel) event.getProperty().getValue();
                size = (Double) report.get(PrintableLabel.COLONNE) * (Double) report.get(PrintableLabel.LIGNE);
                startPosition.clear();
                startPosition.removeAllItems();
                for (int i = 1; i <= size; i++) {
                    startPosition.addItem(i);
                }
            }
        });

        copies = new TextField($("LabelsButton.numberOfCopies"));
        copies.setConverter(Integer.class);

        return new BaseForm<LabelParametersVO>(
                new LabelParametersVO(new LabelTemplate()), this, startPosition, format, copies) {
            @Override
            protected void saveButtonClick(LabelParametersVO parameters)
                    throws ValidationException {
                PrintableLabel selected = (PrintableLabel) format.getValue();
                ReportUtils ru = new ReportUtils(collection, factory, user);
                try {
                    if ((Integer) startPosition.getValue() > size) {
                        throw new Exception($("ButtonLabel.error.posisbiggerthansize"));
                    }
                    ru.setStartingPosition((Integer) startPosition.getValue() - 1);
                    ru.setNumberOfCopies(Integer.parseInt(copies.getValue()));
                    String xml = type.equals(Folder.SCHEMA_TYPE) ? ru.convertFolderWithIdentifierToXML(ids, null) : ru.convertContainerWithIdentifierToXML(ids, null);
                    Content content = selected.get(PrintableLabel.JASPERFILE);
                    InputStream inputStream = contentManager.getContentInputStream(content.getCurrentVersion().getHash(), content.getId());
                    FileUtils.copyInputStreamToFile(inputStream, new File("jasper.jasper"));
                    File file = new File("jasper.jasper");
                    Content c = ru.createPDFFromXmlAndJasperFile(xml, file, ((PrintableLabel) format.getValue()).getTitle() + ".pdf");
                    getWindow().setContent(new LabelViewer(c, ReportUtils.escapeForXmlTag(((PrintableLabel) format.getValue()).getTitle()) + ".pdf"));
                    Page.getCurrent().getJavaScript().execute("$('iframe').find('#print').remove()");
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            protected void cancelButtonClick(LabelParametersVO parameters) {
                getWindow().close();
            }
        };
    }

    public List<PrintableLabel> getTemplates(String type) {
        LogicalSearchCondition condition = from(rm.newRMReport().getSchema()).where(rm.newRMReport().getSchema().getMetadata(PrintableLabel.TYPE_LABEL)).isEqualTo(type);
        return rm.wrapRMReports(ss.search(new LogicalSearchQuery(condition)));
    }

    public static interface RecordSelector extends Serializable {
        List<String> getSelectedRecordIds();
    }

    public void setIds(List<String> ids) {
        this.ids.addAll(ids);
    }

    public void setIds(String id) {
        this.ids.add(id);
    }
}
