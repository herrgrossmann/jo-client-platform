/*
 * Copyright (c) 2011, grossmann
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * * Neither the name of the jo-widgets.org nor the
 *   names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL jo-widgets.org BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package org.jowidgets.cap.ui.impl.widgets;

import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.IActionBuilder;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.convert.IStringObjectConverter;
import org.jowidgets.api.model.item.IMenuModel;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IPopupMenu;
import org.jowidgets.api.widgets.ITable;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.icons.CapIcons;
import org.jowidgets.cap.ui.api.sort.ISortModel;
import org.jowidgets.cap.ui.api.table.IBeanTableConfig;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialog;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.common.types.Modifier;
import org.jowidgets.common.widgets.controler.ITableCellEditEvent;
import org.jowidgets.common.widgets.controler.ITableCellPopupDetectionListener;
import org.jowidgets.common.widgets.controler.ITableCellPopupEvent;
import org.jowidgets.common.widgets.controler.ITableColumnMouseEvent;
import org.jowidgets.common.widgets.controler.ITableColumnPopupDetectionListener;
import org.jowidgets.common.widgets.controler.ITableColumnPopupEvent;
import org.jowidgets.tools.command.ActionWrapper;
import org.jowidgets.tools.command.ExecutionContextWrapper;
import org.jowidgets.tools.controler.TableCellEditorAdapter;
import org.jowidgets.tools.controler.TableColumnAdapter;
import org.jowidgets.tools.model.item.MenuModel;
import org.jowidgets.tools.widgets.wrapper.TableWrapper;
import org.jowidgets.util.IDecorator;
import org.jowidgets.util.ITypedKey;

final class BeanTableImpl<BEAN_TYPE> extends TableWrapper implements IBeanTable<BEAN_TYPE> {

	private final IBeanTableModel<BEAN_TYPE> model;
	private final IMenuModel cellPopupMenuModel;
	private final IMenuModel headerPopupMenuModel;
	private final IMenuModel tablePopupMenuModel;

	private IBeanTableSettingsDialog settingsDialog;
	private ITableCellPopupEvent currentCellEvent;
	private ITableColumnPopupEvent currentColumnEvent;

	BeanTableImpl(final ITable table, final IBeanTableBluePrint<BEAN_TYPE> bluePrint) {
		super(table);
		this.model = bluePrint.getModel();
		this.cellPopupMenuModel = new MenuModel();
		this.headerPopupMenuModel = new MenuModel();
		this.tablePopupMenuModel = new MenuModel();

		table.setPopupMenu(tablePopupMenuModel);

		if (bluePrint.hasDefaultMenus()) {
			//cell popup menu
			final IAction settingsDialogAction = createSettingsDialogAction(model);
			cellPopupMenuModel.addAction(settingsDialogAction);

			//header popup menu
			final IAction hideColumnAction = createHideColumnAction(model);
			final IAction unhideColumnsActions = createUnhideAllColumnAction(model);
			headerPopupMenuModel.addAction(hideColumnAction);
			headerPopupMenuModel.addAction(unhideColumnsActions);

			//table popup menu
			tablePopupMenuModel.addAction(unhideColumnsActions);
			tablePopupMenuModel.addSeparator();
			tablePopupMenuModel.addAction(settingsDialogAction);
			table.setPopupMenu(tablePopupMenuModel);
		}

		//add cell popup menu
		final IPopupMenu cellPopupMenu = table.createPopupMenu();
		cellPopupMenu.setModel(cellPopupMenuModel);
		cellPopupMenuModel.addDecorator(createDecorator(false));
		table.addTableCellPopupDetectionListener(new ITableCellPopupDetectionListener() {
			@Override
			public void popupDetected(final ITableCellPopupEvent event) {
				if (cellPopupMenu.getChildren().size() > 0) {
					currentCellEvent = event;
					cellPopupMenu.show(event.getPosition());
				}
			}
		});

		//add column popup menu
		final IPopupMenu columnPopupMenu = table.createPopupMenu();
		columnPopupMenu.setModel(headerPopupMenuModel);
		headerPopupMenuModel.addDecorator(createDecorator(true));
		table.addTableColumnPopupDetectionListener(new ITableColumnPopupDetectionListener() {
			@Override
			public void popupDetected(final ITableColumnPopupEvent event) {
				if (columnPopupMenu.getChildren().size() > 0) {
					currentColumnEvent = event;
					columnPopupMenu.show(event.getPosition());
				}
			}
		});

		table.addTableCellEditorListener(new TableCellEditorListener());
		table.addTableColumnListener(new TableSortColumnListener());

	}

	@Override
	protected ITable getWidget() {
		return super.getWidget();
	}

	@Override
	public void showSettingsDialog() {
		final IBeanTableSettingsDialog dialog = getSettingsDialog();
		final IBeanTableConfig tableConfig = dialog.show();
		if (dialog.isOkPressed()) {
			model.setConfig(tableConfig);
		}
	}

	@Override
	public IMenuModel getCellPopMenu() {
		return cellPopupMenuModel;
	}

	@Override
	public IMenuModel getHeaderPopMenu() {
		return headerPopupMenuModel;
	}

	private IAction createSettingsDialogAction(final IBeanTableModel<?> beanTableModel) {
		final IActionBuilder builder = Toolkit.getActionBuilderFactory().create();
		builder.setText("Settings ...");
		builder.setIcon(CapIcons.TABLE_SETTINGS);
		builder.setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				showSettingsDialog();
			}
		});
		return builder.build();
	}

	private IAction createHideColumnAction(final IBeanTableModel<?> beanTableModel) {
		final IActionBuilder builder = Toolkit.getActionBuilderFactory().create();
		builder.setText("Hide column");
		builder.setIcon(CapIcons.TABLE_HIDE_COLUMN);
		builder.setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				beanTableModel.getAttribute(currentColumnEvent.getColumnIndex()).setVisible(false);
			}
		});
		return builder.build();
	}

	private IAction createUnhideAllColumnAction(final IBeanTableModel<?> beanTableModel) {
		final IActionBuilder builder = Toolkit.getActionBuilderFactory().create();
		builder.setText("Unhide all columns");
		builder.setIcon(CapIcons.TABLE_UNHIDE_ALL_COLUMNS);
		builder.setCommand(new ICommandExecutor() {
			@Override
			public void execute(final IExecutionContext executionContext) throws Exception {
				for (int i = 0; i < beanTableModel.getColumnCount(); i++) {
					beanTableModel.getAttribute(i).setVisible(true);
				}
			}
		});
		return builder.build();
	}

	private IBeanTableSettingsDialog getSettingsDialog() {
		if (settingsDialog == null) {
			final ICapApiBluePrintFactory bpf = CapUiToolkit.bluePrintFactory();
			settingsDialog = Toolkit.getActiveWindow().createChildWindow(bpf.beanTableSettingsDialog(model));
		}
		return settingsDialog;
	}

	private IDecorator<IAction> createDecorator(final boolean header) {
		return new IDecorator<IAction>() {
			@Override
			public IAction decorate(final IAction original) {
				return new ActionWrapper(original) {

					@Override
					public void execute(final IExecutionContext executionContext) throws Exception {
						super.execute(getDecoratedExecutionContext(executionContext, header));
					}
				};
			}
		};
	}

	private IExecutionContext getDecoratedExecutionContext(final IExecutionContext executionContext, final boolean header) {
		return new ExecutionContextWrapper(executionContext) {
			@SuppressWarnings("unchecked")
			@Override
			public <VALUE_TYPE> VALUE_TYPE getValue(final ITypedKey<VALUE_TYPE> key) {
				if (header && IBeanTable.COLUMN_POPUP_EVENT_CONTEXT_KEY == key) {
					return (VALUE_TYPE) currentColumnEvent;
				}
				else if (IBeanTable.CELL_POPUP_EVENT_CONTEXT_KEY == key) {
					return (VALUE_TYPE) currentCellEvent;
				}
				else {
					return executionContext.getValue(key);
				}
			}
		};
	}

	private class TableCellEditorListener extends TableCellEditorAdapter {
		@Override
		public void editFinished(final ITableCellEditEvent event) {
			final IBeanProxy<BEAN_TYPE> bean = model.getBean(event.getRowIndex());
			final IAttribute<Object> attribute = model.getAttribute(event.getColumnIndex());
			if (bean != null && attribute != null && !attribute.isCollectionType()) {
				final IStringObjectConverter<Object> converter = attribute.getCurrentControlPanel().getStringObjectConverter();
				if (converter != null) {
					if (converter.getStringValidator() == null
						|| converter.getStringValidator().validate(event.getCurrentText()).isValid()) {
						final Object value = converter.convertToObject(event.getCurrentText());
						bean.setValue(attribute.getPropertyName(), value);
					}
				}
			}
		}
	}

	private class TableSortColumnListener extends TableColumnAdapter {
		@Override
		public void mouseClicked(final ITableColumnMouseEvent event) {
			final int modelColumn = event.getColumnIndex();
			if (modelColumn < 0) {
				return;
			}

			final IAttribute<?> attribute = model.getAttribute(modelColumn);
			if (attribute != null && attribute.isSortable()) {
				final ISortModel sortModel = model.getSortModel();
				final String propertyName = attribute.getPropertyName();
				if (event.getModifiers().contains(Modifier.CTRL)) {
					sortModel.addOrToggleCurrentProperty(propertyName);
				}
				else {
					sortModel.setOrToggleCurrentProperty(propertyName);
				}
			}
		}
	}

}
