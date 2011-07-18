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

package org.jowidgets.cap.ui.api;

import java.util.Map;
import java.util.WeakHashMap;

import javax.validation.Validator;

import org.jowidgets.api.toolkit.IToolkit;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.blueprint.IValidationResultLabelBluePrint;
import org.jowidgets.api.widgets.blueprint.defaults.IDefaultInitializer;
import org.jowidgets.cap.ui.api.attribute.IAttributeToolkit;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanKeyFactory;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanMessageFixBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.bean.IBeansStateTracker;
import org.jowidgets.cap.ui.api.command.IActionFactory;
import org.jowidgets.cap.ui.api.execution.IExecutionTaskFactory;
import org.jowidgets.cap.ui.api.form.IBeanFormToolkit;
import org.jowidgets.cap.ui.api.sort.ISortModelConfigBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableConfigBuilder;
import org.jowidgets.cap.ui.api.table.IBeanTableModelBuilder;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanTableSettingsDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.ICapApiBluePrintFactory;
import org.jowidgets.cap.ui.impl.DefaultCapUiToolkit;
import org.jowidgets.cap.ui.impl.widgets.BeanFormFactory;
import org.jowidgets.cap.ui.impl.widgets.BeanTableFactory;
import org.jowidgets.cap.ui.impl.widgets.BeanTableSettingsDialogFactory;
import org.jowidgets.common.types.TableSelectionPolicy;
import org.jowidgets.common.widgets.factory.IGenericWidgetFactory;

public final class CapUiToolkit {

	private static Map<IToolkit, ICapUiToolkit> toolkits = new WeakHashMap<IToolkit, ICapUiToolkit>();

	private CapUiToolkit() {}

	public static ICapUiToolkit getInstance() {
		final IToolkit currentToolkit = Toolkit.getInstance();
		ICapUiToolkit result = toolkits.get(currentToolkit);
		if (result == null) {
			result = createDefaultInstance(currentToolkit);
			toolkits.put(currentToolkit, result);
		}
		return result;
	}

	public static ICapApiBluePrintFactory bluePrintFactory() {
		return getInstance().bluePrintFactory();
	}

	public static IActionFactory actionFactory() {
		return getInstance().actionFactory();
	}

	public static IAttributeToolkit attributeToolkit() {
		return getInstance().attributeToolkit();
	}

	public static IExecutionTaskFactory executionTaskFactory() {
		return getInstance().executionTaskFactory();
	}

	public static <BEAN_TYPE> IBeansStateTracker<BEAN_TYPE> beansStateTracker() {
		return getInstance().beansStateTracker();
	}

	public static <BEAN_TYPE> IBeanProxyFactory<BEAN_TYPE> beanProxyFactory(final Class<? extends BEAN_TYPE> proxyType) {
		return getInstance().beanProxyFactory(proxyType);
	}

	public static IBeanKeyFactory beanKeyFactory() {
		return getInstance().beanKeyFactory();
	}

	public static <BEAN_TYPE> IBeanTableModelBuilder<BEAN_TYPE> beanTableModelBuilder(final Class<BEAN_TYPE> beanType) {
		return getInstance().beanTableModelBuilder(beanType);
	}

	public static IBeanTableConfigBuilder beanTableConfigBuilder() {
		return getInstance().beanTableConfigBuilder();
	}

	public static IBeanMessageBuilder beanMessageBuilder(final BeanMessageType type) {
		return getInstance().beanMessageBuilder(type);
	}

	public static IBeanMessageFixBuilder beanMessageFixBuilder() {
		return getInstance().beanMessageFixBuilder();
	}

	public static ISortModelConfigBuilder sortModelConfigBuilder() {
		return getInstance().sortModelConfigBuilder();
	}

	public static IBeanFormToolkit beanFormToolkit() {
		return getInstance().beanFormToolkit();
	}

	public static Validator beanValidator() {
		return getInstance().beanValidator();
	}

	private static ICapUiToolkit createDefaultInstance(final IToolkit toolkit) {
		registerWidgets(toolkit);
		return new DefaultCapUiToolkit();
	}

	@SuppressWarnings("unchecked")
	private static void registerWidgets(final IToolkit toolkit) {
		final IGenericWidgetFactory genericWidgetFactory = toolkit.getWidgetFactory();
		genericWidgetFactory.register(IBeanTableBluePrint.class, new BeanTableFactory());
		genericWidgetFactory.register(IBeanFormBluePrint.class, new BeanFormFactory());
		genericWidgetFactory.register(IBeanTableSettingsDialogBluePrint.class, new BeanTableSettingsDialogFactory());

		toolkit.getBluePrintFactory().addDefaultsInitializer(
				IBeanTableBluePrint.class,
				new IDefaultInitializer<IBeanTableBluePrint<?>>() {

					@Override
					public void initialize(final IBeanTableBluePrint<?> bluePrint) {
						bluePrint.setSelectionPolicy(TableSelectionPolicy.MULTI_ROW_SELECTION);
						bluePrint.setColumnsMoveable(true);
						bluePrint.setColumnsResizeable(true);
						bluePrint.setDefaultMenus(true);
					}
				});

		toolkit.getBluePrintFactory().addDefaultsInitializer(
				IBeanFormBluePrint.class,
				new IDefaultInitializer<IBeanFormBluePrint<?>>() {

					@Override
					public void initialize(final IBeanFormBluePrint<?> bluePrint) {
						bluePrint.setAutoResetValidation(true);
						bluePrint.setContentScrolled(true);
						final IValidationResultLabelBluePrint validationLabelBp = Toolkit.getBluePrintFactory().validationResultLabel();
						bluePrint.setValidationLabel(validationLabelBp);
					}
				});
	}

}
