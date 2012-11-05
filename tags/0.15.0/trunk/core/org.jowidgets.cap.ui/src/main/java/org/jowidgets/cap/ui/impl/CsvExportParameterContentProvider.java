/*
 * Copyright (c) 2012, grossmann
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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.api.convert.IObjectStringConverter;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.IButton;
import org.jowidgets.api.widgets.IComboBox;
import org.jowidgets.api.widgets.IFileChooser;
import org.jowidgets.api.widgets.IInputComponent;
import org.jowidgets.api.widgets.blueprint.IComboBoxSelectionBluePrint;
import org.jowidgets.api.widgets.blueprint.IFileChooserBluePrint;
import org.jowidgets.api.widgets.blueprint.ITextLabelBluePrint;
import org.jowidgets.api.widgets.content.IInputContentContainer;
import org.jowidgets.api.widgets.content.IInputContentCreator;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.cap.ui.api.table.ICsvExportParameter;
import org.jowidgets.common.types.DialogResult;
import org.jowidgets.common.types.FileChooserType;
import org.jowidgets.common.types.IFileChooserFilter;
import org.jowidgets.common.widgets.controller.IActionListener;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.types.FileChooserFilter;
import org.jowidgets.tools.widgets.blueprint.BPF;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidator;
import org.jowidgets.validation.ValidationResult;

final class CsvExportParameterContentProvider<BEAN_TYPE> implements IInputContentCreator<ICsvExportParameter> {

	private final String userHomePath = System.getProperty("user.home");
	private final IBeanTableModel<BEAN_TYPE> model;
	private final List<IFileChooserFilter> filterList;

	private IComboBox<String> separatorCmb;
	private IComboBox<ICsvExportParameter.ExportType> exportTypeCmb;
	private IFileChooser fileFC;
	private IInputComponent<String> filenameTf;
	private IComboBox<String> maskCmb;
	private IComboBox<Boolean> headerCmb;
	private IComboBox<Boolean> propertiesCmb;
	private IComboBox<String> encodingCmb;
	private ICsvExportParameter parameter;

	CsvExportParameterContentProvider(final IBeanTableModel<BEAN_TYPE> model) {
		this.model = model;
		this.filterList = generateFilterList();
	}

	@Override
	public void setValue(final ICsvExportParameter parameter) {
		this.parameter = parameter;

		if (parameter != null) {
			headerCmb.setValue(parameter.isExportHeader());
			propertiesCmb.setValue(parameter.isExportInvisibleProperties());
			exportTypeCmb.setValue(parameter.getExportType());
			separatorCmb.setValue(String.valueOf(parameter.getSeparator()));
			maskCmb.setValue(String.valueOf(parameter.getMask()));
			encodingCmb.setValue(parameter.getEncoding());
			if (parameter.getFilename() == null) {
				filenameTf.setValue(userHomePath
					+ File.separator
					+ model.getEntityLabelPlural()
					+ "."
					+ this.filterList.get(0).getExtensions().get(0));
			}
			else {
				filenameTf.setValue(parameter.getFilename());
			}
		}
	}

	@Override
	public ICsvExportParameter getValue() {
		if (parameter != null) {
			return new CsvExportParameter(
				exportTypeCmb.getValue(),
				headerCmb.getValue() != null ? headerCmb.getValue() : true,
				propertiesCmb.getValue() != null ? propertiesCmb.getValue() : true,
				separatorCmb.getValue() != null ? separatorCmb.getValue().charAt(0) : parameter.getSeparator(),
				maskCmb.getValue() != null ? maskCmb.getValue().charAt(0) : parameter.getMask(),
				encodingCmb.getValue(),
				filenameTf.getValue());
		}
		else {
			return parameter;
		}
	}

	@Override
	public void createContent(final IInputContentContainer container) {
		final ITextLabelBluePrint textLabelBp = BPF.textLabel().alignRight();
		container.setLayout(new MigLayoutDescriptor("[][grow, 250::]", "[][]"));

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.filename")), "alignx r");
		filenameTf = container.add(BPF.inputFieldString().setEditable(true), "growx,w 0::,split");
		filenameTf.addValidator(new FileNameValidator());
		createOpenFileButton(container);

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.header")), "alignx r");
		final IComboBoxSelectionBluePrint<Boolean> headerCmbBp = BPF.comboBoxSelectionBoolean().setAutoCompletion(false);
		headerCmb = container.add(headerCmbBp, "growx ,w  0:: , wrap");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.properties")), "alignx r, growx");
		final IComboBoxSelectionBluePrint<Boolean> propertiesCmbBp = BPF.comboBoxSelection(createExportAttributesConverter());
		propertiesCmbBp.setElements(Boolean.TRUE, Boolean.FALSE).setValue(Boolean.FALSE).setAutoCompletion(false);
		propertiesCmb = container.add(propertiesCmbBp, "growx, w 0::, wrap");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.Export")), "alignx r");
		final IComboBoxSelectionBluePrint<ICsvExportParameter.ExportType> exportTypeCmbBp = BPF.comboBoxSelection(createExportTypesConverter());
		exportTypeCmbBp.setAutoCompletion(false).setElements(ICsvExportParameter.ExportType.values());
		exportTypeCmb = container.add(exportTypeCmbBp, "growx, w 0::, wrap");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.separator")), "alignx r");
		final IComboBoxSelectionBluePrint<String> separatorCmbBp = BPF.comboBoxSelection(";", ",", "|");
		separatorCmbBp.setAutoCompletion(false);
		separatorCmb = container.add(separatorCmbBp, "growx, w 0::, wrap");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.mask")), "align r");
		final IComboBoxSelectionBluePrint<String> maskCmbBp = BPF.comboBoxSelection("*", String.valueOf('"'));
		maskCmbBp.setAutoCompletion(false);
		maskCmb = container.add(maskCmbBp, "growx, w 0::, wrap");

		container.add(textLabelBp.setText(Messages.getString("CsvExportParameterContentProvider.encoding")), "alignx r");
		final IComboBoxSelectionBluePrint<String> encodingCmbBp = BPF.comboBoxSelection("UTF-8", "Cp1250", "ISO8859_1");
		encodingCmbBp.setAutoCompletion(false);
		encodingCmb = container.add(encodingCmbBp, "growx, w 0::");

	}

	private void createOpenFileButton(final IInputContentContainer container) {
		final IButton openFileButton = container.add(BPF.button().setIcon(IconsSmall.FOLDER), "w 0::25,h 0::25, wrap");
		openFileButton.addActionListener(new IActionListener() {
			@Override
			public void actionPerformed() {
				openFileChooser();
			}
		});
	}

	private IObjectStringConverter<Boolean> createExportAttributesConverter() {
		return new IObjectStringConverter<Boolean>() {

			@Override
			public String convertToString(final Boolean value) {
				if (value != null) {
					if (value.booleanValue()) {
						return Messages.getString("CsvExportParameterContentProvider.columns_all");
					}
					else {
						return Messages.getString("CsvExportParameterContentProvider.columns_visible");
					}
				}
				return "";
			}

			@Override
			public String getDescription(final Boolean value) {
				if (value != null) {
					if (value.booleanValue()) {
						return Messages.getString("CsvExportParameterContentProvider.all_columns_exported");
					}
					else {
						return Messages.getString("CsvExportParameterContentProvider.visible_columns_exported");
					}
				}
				return "";
			}
		};
	}

	private IObjectStringConverter<ICsvExportParameter.ExportType> createExportTypesConverter() {
		return new IObjectStringConverter<ICsvExportParameter.ExportType>() {

			@Override
			public String convertToString(final ICsvExportParameter.ExportType value) {
				if (value != null) {
					if (value == ICsvExportParameter.ExportType.ALL) {
						return Messages.getString("CsvExportParameterContentProvider.all");
					}
					else {
						return Messages.getString("CsvExportParameterContentProvider.selection");
					}
				}
				return "";
			}

			@Override
			public String getDescription(final ICsvExportParameter.ExportType value) {
				if (value != null) {
					if (value == ICsvExportParameter.ExportType.ALL) {
						return Messages.getString("CsvExportParameterContentProvider.all_rows_will_be_exported");
					}
					else {
						return Messages.getString("CsvExportParameterContentProvider.selected_rows_will_be_exported");
					}
				}
				return "";
			}
		};
	}

	private void openFileChooser() {

		final IFileChooserBluePrint fcBp = BPF.fileChooser(FileChooserType.OPEN_FILE).setFilterList(filterList);
		fileFC = Toolkit.getActiveWindow().createChildWindow(fcBp);
		fileFC.setSelectedFile(new File(filenameTf.getValue()));
		final DialogResult result = fileFC.open();
		if (result == DialogResult.OK) {
			for (final File file : fileFC.getSelectedFiles()) {
				filenameTf.setValue(file.getAbsolutePath());
			}
			if (!filenameTf.getValue().contains(fileFC.getSelectedFilter().getExtensions().get(0))) {
				filenameTf.setValue(filenameTf.getValue() + "." + fileFC.getSelectedFilter().getExtensions().get(0));
			}
		}
	}

	private List<IFileChooserFilter> generateFilterList() {
		final List<IFileChooserFilter> tempList = new LinkedList<IFileChooserFilter>();
		tempList.add(new FileChooserFilter(Messages.getString("CsvExportParameterContentProvider.csv_file"), "csv"));
		tempList.add(new FileChooserFilter(Messages.getString("CsvExportParameterContentProvider.text_file"), "txt"));
		return tempList;
	}

	class FileNameValidator implements IValidator<String> {

		@Override
		public IValidationResult validate(final String value) {

			if (EmptyCheck.isEmpty(value)) {
				return ValidationResult.infoError(Messages.getString("CsvExportParameterContentProvider.choose_file"));
			}

			if (new File(filenameTf.getValue()).exists()) {
				return ValidationResult.warning(Messages.getString("CsvExportParameterContentProvider.file_overwritten"));
			}

			if (!checkFileType(value.substring(value.length() - 3))) {
				return ValidationResult.error(Messages.getString("CsvExportParameterContentProvider.incompatible_file"));
			}

			return ValidationResult.ok();
		}
	}

	private boolean checkFileType(final String filetype) {
		for (final IFileChooserFilter filter : filterList) {
			if (filetype.equals(filter.getExtensions().get(0))) {
				return true;
			}
		}
		return false;
	}

}
