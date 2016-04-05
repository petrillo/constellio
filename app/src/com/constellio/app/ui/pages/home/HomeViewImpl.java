package com.constellio.app.ui.pages.home;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.entities.navigation.PageItem;
import com.constellio.app.entities.navigation.PageItem.CustomItem;
import com.constellio.app.entities.navigation.PageItem.RecentItemTable;
import com.constellio.app.entities.navigation.PageItem.RecentItemTable.RecentItem;
import com.constellio.app.entities.navigation.PageItem.RecordTable;
import com.constellio.app.entities.navigation.PageItem.RecordTree;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenu;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.tree.RecordLazyTree;
import com.constellio.app.ui.framework.components.tree.RecordLazyTreeTabSheet;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;

public class HomeViewImpl extends BaseViewImpl implements HomeView {
	private final HomePresenter presenter;
	private List<PageItem> tabs;
	private TabSheet tabSheet;

	public HomeViewImpl() {
		presenter = new HomePresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		tabs = presenter.forParams(event.getParameters()).getTabs();
	}

	@Override
	protected String getTitle() {
		return $("HomeView.viewTitle");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);
		for (final NavigationItem item : presenter.getMenuItems()) {
			ComponentState state = presenter.getStateFor(item);
			Button button = new Button($("HomeView." + item.getCode()));
			button.setVisible(state.isVisible());
			button.setEnabled(state.isEnabled());
			button.addStyleName(item.getCode());
			button.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					item.activate(navigate());
				}
			});
			buttons.add(button);
		}
		return buttons;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		tabSheet = new TabSheet();
		tabSheet.addStyleName("records-management");

		tabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				Tab currentTab = tabSheet.getTab(tabSheet.getSelectedTab());
				selectTab(currentTab);
			}
		});

		Map<String, Tab> tabsByCode = new HashMap<>();
		for (PageItem item : tabs) {
			Tab tab = tabSheet.addTab(new PlaceHolder(), $("HomeView.tab." + item.getCode()));
			tabsByCode.put(item.getCode(), tab);
		}

		selectTab(tabsByCode.get(presenter.getDefaultTab()));

		return tabSheet;
	}

	private void selectTab(Tab tab) {
		if (tab == null) {
			return;
		}

		int position = tabSheet.getTabPosition(tab);
		tabSheet.setSelectedTab(position);
		PageItem item = tabs.get(position);

		PlaceHolder tabComponent = (PlaceHolder) tab.getComponent();
		if (tabComponent.getComponentCount() == 0) {
			tabComponent.setCompositionRoot(buildComponentFor(tab));
		}

		ParamUtils.setParams(item.getCode());
	}

	private Component buildComponentFor(Tab tab) {
		int indexOfSelectedTab = tabSheet.getTabPosition(tab);
		PageItem tabSource = tabs.get(indexOfSelectedTab);
		switch (tabSource.getType()) {
		case RECENT_ITEM_TABLE:
			return buildRecentItemTable((RecentItemTable) tabSource);
		case RECORD_TABLE:
			return buildRecordTable((RecordTable) tabSource);
		case RECORD_TREE:
			return buildRecordTreeOrRecordMultiTree((RecordTree) tabSource);
		case CUSTOM_ITEM:
			return buildCustomComponent((CustomItem) tabSource);
		default:
			throw new RuntimeException("Unsupported tab type : " + tabSource.getType());
		}
	}

	private Component buildRecentItemTable(RecentItemTable recentItems) {
		RecentTable table = new RecentTable(
				recentItems.getItems(getConstellioFactories().getModelLayerFactory(), getSessionContext()));
		table.setSizeFull();
		table.addStyleName("record-table");
		return table;
	}

	private Table buildRecordTable(RecordTable recordTable) {
		Table table = new RecordVOTable(
				recordTable.getDataProvider(getConstellioFactories().getModelLayerFactory(), getSessionContext()));
		table.addStyleName("record-table");
		table.setSizeFull();
		for (Object item : table.getContainerPropertyIds()) {
			MetadataVO property = (MetadataVO) item;
			if (property.getCode() != null && property.getCode().contains(Schemas.MODIFIED_ON.getLocalCode())) {
				table.setColumnWidth(property, 180);
			}
		}
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getButton() == MouseButton.LEFT) {
					RecordVOItem recordItem = (RecordVOItem) event.getItem();
					RecordVO recordVO = recordItem.getRecord();
					presenter.recordClicked(recordVO.getId());
				}
			}
		});
		return table;
	}

	private Component buildRecordTreeOrRecordMultiTree(RecordTree recordTree) {
		List<RecordLazyTreeDataProvider> providers = recordTree.getDataProviders(
				getConstellioFactories().getModelLayerFactory(), getSessionContext());
		return providers.size() > 1 ?
				buildRecordMultiTree(recordTree, providers) :
				buildRecordTree(recordTree, providers.get(0));
	}

	private RecordLazyTreeTabSheet buildRecordMultiTree(final RecordTree recordTree, List<RecordLazyTreeDataProvider> providers) {
		RecordLazyTreeTabSheet tabSheet = new RecordLazyTreeTabSheet(providers) {
			@Override
			protected RecordLazyTree newLazyTree(RecordLazyTreeDataProvider dataProvider, int bufferSize) {
				return buildRecordTree(recordTree, dataProvider);
			}
		};
		return tabSheet;
	}

	private RecordLazyTree buildRecordTree(RecordTree recordTree, RecordLazyTreeDataProvider provider) {
		RecordLazyTree tree = new RecordLazyTree(provider, 20);
		tree.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getButton() == MouseButton.LEFT) {
					String recordId = (String) event.getItemId();
					presenter.recordClicked(recordId);
				}
			}
		});
		BaseContextMenu menu = recordTree.getContextMenu();
		if (menu != null) {
			menu.setAsTreeContextMenu(tree.getNestedTree());
		}
		return tree;
	}

	private Component buildCustomComponent(CustomItem tabSource) {
		Component component = tabSource.buildCustomComponent(getConstellioFactories(), getSessionContext());
		if (component instanceof BaseViewImpl) {
			((BaseViewImpl) component).enter(null);
		}
		component.setSizeFull();
		return component;
	}

	private static class PlaceHolder extends CustomComponent {
		@Override
		public void setCompositionRoot(Component compositionRoot) {
			super.setCompositionRoot(compositionRoot);
		}
	}

	private class RecentTable extends Table {
		public RecentTable(List<RecentItem> recentItems) {
			setContainerDataSource(new BeanItemContainer<>(RecentItem.class, recentItems));

			addStyleName(RecordVOTable.CLICKABLE_ROW_STYLE_NAME);

			setVisibleColumns(RecentItem.CAPTION, RecentItem.LAST_ACCESS);
			setColumnHeader(RecentItem.CAPTION, $("HomeView.recentItem.caption"));
			setColumnHeader(RecentItem.LAST_ACCESS, $("HomeView.recentItem.lastAccess"));
			setColumnExpandRatio(RecentItem.CAPTION, 1);

			addItemClickListener(new ItemClickListener() {
				@Override
				public void itemClick(ItemClickEvent event) {
					if (event.getButton() == MouseButton.LEFT) {
						@SuppressWarnings("unchecked")
						BeanItem<RecentItem> item = (BeanItem<RecentItem>) event.getItem();
						presenter.recordClicked(item.getBean().getId());
					}
				}
			});

			setCellStyleGenerator(new CellStyleGenerator() {
				@Override
				public String getStyle(Table source, Object itemId, Object propertyId) {
					if (RecentItem.CAPTION.equals(propertyId)) {
						@SuppressWarnings("unchecked")
						BeanItem<RecentItem> recordVOItem = (BeanItem<RecentItem>) getItem(itemId);
						RecordVO recordVO = recordVOItem.getBean().getRecord();
						try {
							String extension = FileIconUtils.getExtension(recordVO);
							if (extension != null) {
								return "file-icon-" + extension;
							}
						} catch (Exception e) {
							// Ignore the exception
						}
					}
					return null;
				}
			});
		}

		@Override
		public Property<?> getContainerProperty(Object itemId, Object propertyId) {
			if (RecentItem.LAST_ACCESS.equals(propertyId)) {
				RecentItem recentItem = (RecentItem) itemId;
				String value = new JodaDateTimeToStringConverter()
						.convertToPresentation(recentItem.getLastAccess(), String.class, getSessionContext().getCurrentLocale());
				return new ObjectProperty<>(value);
			}
			return super.getContainerProperty(itemId, propertyId);
		}
	}
}
