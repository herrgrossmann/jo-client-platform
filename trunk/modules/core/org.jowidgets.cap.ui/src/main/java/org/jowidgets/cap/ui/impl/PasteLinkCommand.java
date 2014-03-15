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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jowidgets.api.clipboard.Clipboard;
import org.jowidgets.api.command.ICommand;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IEnabledChecker;
import org.jowidgets.api.command.IExceptionHandler;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.controller.IDisposeListener;
import org.jowidgets.api.controller.IDisposeObservable;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.common.api.CapCommonToolkit;
import org.jowidgets.cap.common.api.bean.Cardinality;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutionCallbackListener;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.link.ILinkCreation;
import org.jowidgets.cap.common.api.link.ILinkCreationBuilder;
import org.jowidgets.cap.common.api.link.LinkCreation;
import org.jowidgets.cap.common.api.service.ILinkCreatorService;
import org.jowidgets.cap.ui.api.CapUiToolkit;
import org.jowidgets.cap.ui.api.bean.IBeanExceptionConverter;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionProvider;
import org.jowidgets.cap.ui.api.clipboard.IBeanSelectionClipboard;
import org.jowidgets.cap.ui.api.execution.BeanMessageStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanModificationStatePolicy;
import org.jowidgets.cap.ui.api.execution.BeanSelectionPolicy;
import org.jowidgets.cap.ui.api.execution.IExecutionInterceptor;
import org.jowidgets.cap.ui.api.execution.IExecutionTask;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.tools.execution.AbstractUiResultCallback;
import org.jowidgets.i18n.api.IMessage;
import org.jowidgets.i18n.api.MessageReplacer;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IFactory;

final class PasteLinkCommand<SOURCE_BEAN_TYPE, LINK_BEAN_TYPE, LINKABLE_BEAN_TYPE> implements ICommand, ICommandExecutor {

	private static final IMessage NOTHING_SELECTED = Messages.getMessage("BeanLinkCreatorCommand.nothing_selected");
	private static final IMessage SHORT_ERROR = Messages.getMessage("BeanLinkCreatorCommand.short_error_message");

	private final ILinkCreatorService linkCreatorService;
	private final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source;
	private final IBeanListModel<LINKABLE_BEAN_TYPE> linkedModel;
	private final Cardinality linkedCardinality;
	private final IBeanExceptionConverter exceptionConverter;
	private final Object linkableBeanTypeId;
	private final IFactory<IBeanProxy<LINK_BEAN_TYPE>> linkDefaultFactory;

	private final BeanSelectionProviderEnabledChecker<SOURCE_BEAN_TYPE> enabledChecker;
	private final ExecutionObservable<List<IBeanDto>> executionObservable;

	PasteLinkCommand(
		final ILinkCreatorService linkCreatorService,
		final IBeanSelectionProvider<SOURCE_BEAN_TYPE> source,
		final boolean sourceSelectionAutoRefresh,
		final boolean sourceMultiSelection,
		final BeanModificationStatePolicy sourceModificationPolicy,
		final BeanMessageStatePolicy sourceMessageStatePolicy,
		final List<IExecutableChecker<SOURCE_BEAN_TYPE>> sourceExecutableCheckers,
		final IBeanListModel<LINKABLE_BEAN_TYPE> linkedModel,
		final Cardinality linkedCardinality,
		final Object linkBeanTypeId,
		final Class<? extends LINK_BEAN_TYPE> linkBeanType,
		final IFactory<IBeanProxy<LINK_BEAN_TYPE>> linkDefaultFactory,
		final Object linkableBeanTypeId,
		final Class<? extends LINKABLE_BEAN_TYPE> linkableBeanType,
		final List<IEnabledChecker> enabledCheckers,
		final List<IExecutionInterceptor<List<IBeanDto>>> executionInterceptors,
		final IBeanExceptionConverter exceptionConverter,
		final IDisposeObservable disposeObservable) {

		Assert.paramNotNull(linkCreatorService, "linkCreatorService");
		Assert.paramNotNull(sourceModificationPolicy, "sourceModificationPolicy");
		Assert.paramNotNull(sourceMessageStatePolicy, "sourceMessageStatePolicy");
		Assert.paramNotNull(sourceExecutableCheckers, "sourceExecutableCheckers");
		Assert.paramNotNull(enabledCheckers, "enabledCheckers");
		Assert.paramNotNull(exceptionConverter, "exceptionConverter");

		Assert.paramNotNull(linkedCardinality, "linkedCardinality");

		final ClipboardSelectionEnabledChecker clipboardEnabledChecker;
		if (linkableBeanTypeId != null && linkableBeanType != null) {
			clipboardEnabledChecker = new ClipboardSelectionEnabledChecker(linkableBeanTypeId, linkableBeanType);
		}
		else if (linkBeanTypeId != null && linkBeanType != null) {
			clipboardEnabledChecker = new ClipboardSelectionEnabledChecker(linkBeanTypeId, linkBeanType);
		}
		else {
			throw new IllegalArgumentException("Neither the linkType nor the linkable type is set");
		}

		final List<IEnabledChecker> checkers = new LinkedList<IEnabledChecker>(enabledCheckers);
		checkers.add(clipboardEnabledChecker);

		this.enabledChecker = new BeanSelectionProviderEnabledChecker<SOURCE_BEAN_TYPE>(
			source,
			sourceMultiSelection ? BeanSelectionPolicy.MULTI_SELECTION : BeanSelectionPolicy.SINGLE_SELECTION,
			sourceModificationPolicy,
			sourceMessageStatePolicy,
			checkers,
			sourceExecutableCheckers,
			false);

		this.source = source;
		this.linkCreatorService = linkCreatorService;
		this.linkedModel = linkedModel;
		this.linkedCardinality = linkedCardinality;
		this.linkDefaultFactory = linkDefaultFactory;
		this.linkableBeanTypeId = linkableBeanTypeId;

		this.executionObservable = new ExecutionObservable<List<IBeanDto>>(executionInterceptors);
		this.exceptionConverter = exceptionConverter;

		if (sourceSelectionAutoRefresh) {
			BeanSelectionProviderRefreshInterceptor<SOURCE_BEAN_TYPE, List<IBeanDto>> refreshInterceptor;
			refreshInterceptor = new BeanSelectionProviderRefreshInterceptor<SOURCE_BEAN_TYPE, List<IBeanDto>>(source);
			executionObservable.addExecutionInterceptor(refreshInterceptor);
		}

		final IDisposeListener disposeListener = new IDisposeListener() {
			@Override
			public void onDispose() {
				clipboardEnabledChecker.dispose();
			}
		};

		disposeObservable.addDisposeListener(disposeListener);
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
		if (!executionObservable.fireBeforeExecution(executionContext)) {
			return;
		}

		final List<IBeanProxy<SOURCE_BEAN_TYPE>> selection = source.getBeanSelection().getSelection();

		if (EmptyCheck.isEmpty(selection)) {
			Toolkit.getMessagePane().showWarning(executionContext, NOTHING_SELECTED.get());
			return;
		}

		final IBeanSelectionClipboard selectionClipboard = Clipboard.getData(IBeanSelectionClipboard.TRANSFER_TYPE);
		if (selectionClipboard != null) {
			linkBeans(executionContext, selection, selectionClipboard);
		}
	}

	private void linkBeans(
		final IExecutionContext executionContext,
		final List<IBeanProxy<SOURCE_BEAN_TYPE>> selection,
		final IBeanSelectionClipboard selectionClipboard) {

		final IExecutionTask executionTask = CapUiToolkit.executionTaskFactory().create();
		final IUiThreadAccess uiThreadAccess = Toolkit.getUiThreadAccess();
		executionTask.addExecutionCallbackListener(new IExecutionCallbackListener() {
			@Override
			public void canceled() {
				uiThreadAccess.invokeLater(new Runnable() {
					@Override
					public void run() {
						for (final IBeanProxy<SOURCE_BEAN_TYPE> bean : selection) {
							bean.setExecutionTask(null);
						}
						executionObservable.fireAfterExecutionCanceled(executionContext);
					}
				});
			}
		});

		final Set<ILinkCreation> linkCreations = new HashSet<ILinkCreation>();

		if (linkableBeanTypeId != null) {
			final ILinkCreationBuilder linkCreationBuilder = LinkCreation.builder();

			//add the source beans
			for (final IBeanProxy<SOURCE_BEAN_TYPE> sourceBean : selection) {
				if (sourceBean.isTransient()) {
					linkCreationBuilder.addTransientSourceBean(sourceBean.getBeanData());
				}
				else {
					linkCreationBuilder.addSourceBean(sourceBean.getBeanKey());
				}
			}

			//add the linkable beans
			for (final IBeanDto linkableBean : selectionClipboard.getBeans()) {
				if (linkableBean.getId() == null) {
					linkCreationBuilder.addTransientLinkableBean(linkableBean);
				}
				else {
					linkCreationBuilder.addLinkableBean(createBeanKey(linkableBean));
				}
			}

			if (linkDefaultFactory != null) {
				linkCreationBuilder.setAdditionalLinkProperties(linkDefaultFactory.create().getBeanData());
			}

			linkCreations.add(linkCreationBuilder.build());
		}
		else {//direct links
			for (final IBeanDto linkBean : selectionClipboard.getBeans()) {

				final ILinkCreationBuilder linkCreationBuilder = LinkCreation.builder();

				//add the source beans
				for (final IBeanProxy<SOURCE_BEAN_TYPE> sourceBean : selection) {
					if (sourceBean.isTransient()) {
						linkCreationBuilder.addTransientSourceBean(sourceBean.getBeanData());
					}
					else {
						linkCreationBuilder.addSourceBean(sourceBean.getBeanKey());
					}
				}

				//one link for each bean in the clipboard
				linkCreationBuilder.setAdditionalLinkProperties(linkBean);

				linkCreations.add(linkCreationBuilder.build());
			}
		}

		executionObservable.fireAfterExecutionPrepared(executionContext);
		linkCreatorService.create(createResultCallback(selection, executionContext), linkCreations, executionTask);
	}

	private IBeanKey createBeanKey(final IBeanDto beanDto) {
		return CapCommonToolkit.beanKeyBuilder().setBeanDto(beanDto).build();
	}

	private IResultCallback<List<IBeanDto>> createResultCallback(
		final List<IBeanProxy<SOURCE_BEAN_TYPE>> selection,
		final IExecutionContext executionContext) {
		return new AbstractUiResultCallback<List<IBeanDto>>() {

			@Override
			protected void finishedUi(final List<IBeanDto> result) {
				for (final IBeanProxy<SOURCE_BEAN_TYPE> bean : selection) {
					bean.setExecutionTask(null);
				}
				if (linkedModel != null) {
					if (Cardinality.LESS_OR_EQUAL_ONE.equals(linkedCardinality)) {
						linkedModel.removeAllBeans();
					}
					for (final IBeanDto resultBean : result) {
						linkedModel.addBeanDto(resultBean);
					}

				}
				executionObservable.fireAfterExecutionSuccess(executionContext, result);
			}

			@Override
			protected void exceptionUi(final Throwable exception) {
				for (final IBeanProxy<SOURCE_BEAN_TYPE> bean : selection) {
					bean.setExecutionTask(null);
					bean.addMessage(exceptionConverter.convert(getShortErrorMessage(), selection, bean, exception));
				}
				executionObservable.fireAfterExecutionError(executionContext, exception);
			}

			private String getShortErrorMessage() {
				final String actionText = executionContext.getAction().getText().replaceAll("\\.", "").trim();
				return MessageReplacer.replace(SHORT_ERROR.get(), actionText);
			}
		};
	}

}
