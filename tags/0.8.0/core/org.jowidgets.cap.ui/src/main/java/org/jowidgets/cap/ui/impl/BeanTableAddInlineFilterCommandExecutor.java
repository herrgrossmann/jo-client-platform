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

package org.jowidgets.cap.ui.impl;

import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.filter.IFilterSupport;
import org.jowidgets.cap.ui.api.filter.IIncludingFilterFactory;
import org.jowidgets.cap.ui.api.filter.IUiFilter;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.widgets.IBeanTable;
import org.jowidgets.common.widgets.controller.ITableCellPopupEvent;

final class BeanTableAddInlineFilterCommandExecutor implements ICommandExecutor {

	private final IBeanTableModel<?> model;
	private final int columnIndex;
	private final boolean invert;

	BeanTableAddInlineFilterCommandExecutor(final IBeanTableModel<?> model, final int columnIndex, final boolean invert) {
		this.model = model;
		this.columnIndex = columnIndex;
		this.invert = invert;
	}

	@Override
	public void execute(final IExecutionContext executionContext) throws Exception {
		final ITableCellPopupEvent cellPopupEvent = executionContext.getValue(IBeanTable.CELL_POPUP_EVENT_CONTEXT_KEY);

		final Object cellValue = model.getValue(cellPopupEvent.getRowIndex(), columnIndex);

		final IAttribute<Object> attribute = model.getAttribute(columnIndex);
		final IFilterSupport<Object> filterSupport = attribute.getCurrentControlPanel().getFilterSupport();
		final IIncludingFilterFactory<Object> includingFilterFactory = filterSupport.getIncludingFilterFactory();
		IUiFilter includingFilter = includingFilterFactory.getIncludingFilter(cellValue);

		if (invert) {
			includingFilter = CapUiToolkit.filterToolkit().filterTools().invert(includingFilter);
		}

		model.addFilter(IBeanTableModel.UI_FILTER_ID, includingFilter);

		model.load();
	}

}