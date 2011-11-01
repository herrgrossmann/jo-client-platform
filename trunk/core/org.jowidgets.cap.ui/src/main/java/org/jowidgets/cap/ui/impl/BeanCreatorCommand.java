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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.common.api.bean.IBeanDataBuilder;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.service.ICreatorService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.bean.BeanMessageType;
import org.jowidgets.cap.ui.api.bean.IBeanExecptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanMessageBuilder;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanProxyFactory;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.widgets.IBeanDialog;
import org.jowidgets.cap.ui.api.widgets.IBeanDialogBluePrint;
import org.jowidgets.cap.ui.api.widgets.IBeanFormBluePrint;
import org.jowidgets.common.types.Rectangle;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;

final class BeanCreatorCommand<BEAN_TYPE> implements ICommand, ICommandExecutor {

	private static final int INITIAL_MIN_WIDTH = 450;

	private final IBeanListModel<BEAN_TYPE> model;

	private final IBeanFormBluePrint<BEAN_TYPE> beanFormBp;

	private final ICreatorService creatorService;

	@SuppressWarnings("unused")
	private final IBeanExecptionConverter exceptionConverter;

	private final BeanListModelEnabledChecker<BEAN_TYPE> enabledChecker;
	private final IBeanProxyFactory<BEAN_TYPE> beanFactory;

	private final Map<String, Object> defaultValues;
	private final List<String> properties;

	private Rectangle dialogBounds;

	BeanCreatorCommand(
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanListModel<BEAN_TYPE> model,
		final IBeanFormBluePrint<BEAN_TYPE> beanFormBp,
		final List<IEnabledChecker> enabledCheckers,
		final boolean anySelection,
		final ICreatorService creatorService,
		final IBeanExecptionConverter exceptionConverter) {

		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(model, "model");
		Assert.paramNotNull(beanFormBp, "beanFormBp");
		Assert.paramNotNull(beanFormBp.getAttributes(), "beanFormBp.getAttributes()");
		Assert.paramNotNull(enabledCheckers, "enabledCheckers");
		Assert.paramNotNull(anySelection, "anySelection");
		Assert.paramNotNull(creatorService, "creatorService");
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");

		this.enabledChecker = new BeanListModelEnabledChecker<BEAN_TYPE>(
			model,
			anySelection ? BeanSelectionPolicy.ANY_SELECTION : BeanSelectionPolicy.NO_SELECTION,
			BeanModificationStatePolicy.ANY_MODIFICATION,
			null,
			enabledCheckers,
			null,
			true);

		this.beanFactory = CapUiToolkit.beanProxyFactory(beanType);

		this.model = model;
		this.beanFormBp = beanFormBp;
		this.creatorService = creatorService;
		this.exceptionConverter = exceptionConverter;

		this.defaultValues = new HashMap<String, Object>();
		this.properties = new LinkedList<String>();
		for (final IAttribute<?> attribute : beanFormBp.getAttributes()) {
			final String propertyName = attribute.getPropertyName();
			properties.add(propertyName);
			final Object defaultValue = attribute.getDefaultValue();
			if (defaultValue != null) {
				defaultValues.put(propertyName, defaultValue);
			}
		}
	}

	@Override
	public ICommandExecutor getCommandExecutor() {
		return this;
	}

	@Override
	public IEnabledChecker getEnabledChecker() {
		return enabledChecker;
	}

	@Override
	public IExceptionHandler getExceptionHandler() {
		return null;
	}

	@Override
	public void execute(final IExecutionContext executionContext) throws Exception {
		final IBeanProxy<BEAN_TYPE> proxy = beanFactory.createTransientProxy(properties, defaultValues);

		final IBeanDialogBluePrint<BEAN_TYPE> beanDialogBp = CapUiToolkit.bluePrintFactory().beanDialog(beanFormBp);
		beanDialogBp.autoPackOff();
		if (dialogBounds != null) {
			beanDialogBp.setPosition(dialogBounds.getPosition()).setSize(dialogBounds.getSize());
			beanDialogBp.autoPackOff().autoCenterOff();
		}
		beanDialogBp.setExecutionContext(executionContext);
		final IBeanDialog<BEAN_TYPE> dialog = Toolkit.getActiveWindow().createChildWindow(beanDialogBp);
		dialog.pack();
		dialog.setBean(proxy);

		dialog.setSize(Math.max(dialog.getSize().getWidth(), INITIAL_MIN_WIDTH), dialog.getSize().getHeight());

		dialog.setVisible(true);
		if (dialog.isOkPressed()) {
			createBean(createUnmodifiedBean(proxy));
		}
		dialogBounds = dialog.getBounds();
		dialog.dispose();
	}

	private IBeanProxy<BEAN_TYPE> createUnmodifiedBean(final IBeanProxy<BEAN_TYPE> proxy) {
		final IBeanProxy<BEAN_TYPE> result = beanFactory.createProxy(proxy, properties);
		result.setTransient(true);
		return result;
	}

	private void createBean(final IBeanProxy<BEAN_TYPE> proxy) {

		final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create();
		proxy.setExecutionTask(executionTask);

		//add bean to the model
		model.addBean(proxy);
		model.setSelection(Collections.singletonList(Integer.valueOf(model.getSize() - 1)));

		final IBeanData beanData = createBeanData(proxy);
		final List<IBeanData> data = Collections.singletonList(beanData);
		final IUiThreadAccess uiThreadAccess = Toolkit.getUiThreadAccess();
		final IResultCallback<List<IBeanDto>> resultCallback = new IResultCallback<List<IBeanDto>>() {

			@Override
			public void finished(final List<IBeanDto> result) {
				if (!EmptyCheck.isEmpty(result)) {
					uiThreadAccess.invokeLater(new Runnable() {
						@Override
						public void run() {
							proxy.setExecutionTask(null);
							proxy.update(result.get(0));
							proxy.setTransient(false);
							model.fireBeansChanged();
						}
					});
					//CHECKSTYLE:OFF
					System.out.println("Created: " + result);
					//CHECKSTYLE:ON
				}
				else {
					uiThreadAccess.invokeLater(new Runnable() {
						@Override
						public void run() {
							proxy.setExecutionTask(null);
							final IBeanMessageBuilder messageBuilder = CapUiToolkit.beanMessageBuilder(BeanMessageType.ERROR);
							messageBuilder.setMessage("Object not created");
							messageBuilder.setDescription("The object was not created");
							proxy.addMessage(messageBuilder.build());
							model.fireBeansChanged();
						}
					});
				}

			}

			@Override
			public void exception(final Throwable exception) {
				//CHECKSTYLE:OFF
				exception.printStackTrace();
				//CHECKSTYLE:ON
			}

			@Override
			public void timeout() {
				//CHECKSTYLE:OFF
				System.out.println("Timeout");
				//CHECKSTYLE:ON
			}
		};
		creatorService.create(resultCallback, data, executionTask);
	}

	private IBeanData createBeanData(final IBeanProxy<BEAN_TYPE> proxy) {
		final IBeanDataBuilder builder = CapCommonToolkit.beanDataBuilder();
		for (final String propertyName : properties) {
			if (propertyName != IBean.ID_PROPERTY) {
				builder.setProperty(propertyName, proxy.getValue(propertyName));
			}
		}
		return builder.build();
	}
}
