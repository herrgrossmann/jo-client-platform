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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionEvent;
import org.jowidgets.cap.ui.api.bean.IBeanSelectionListener;
import org.jowidgets.cap.ui.api.model.IBeanListModel;
import org.jowidgets.cap.ui.api.model.IModificationStateListener;
import org.jowidgets.cap.ui.api.model.IProcessStateListener;
import org.jowidgets.cap.ui.api.model.LinkType;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModel;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModelBluePrint;
import org.jowidgets.cap.ui.api.tree.IBeanRelationNodeModelConfigurator;
import org.jowidgets.cap.ui.api.tree.IBeanRelationTreeModel;
import org.jowidgets.cap.ui.api.tree.IEntityTypeId;
import org.jowidgets.cap.ui.tools.model.ModificationStateObservable;
import org.jowidgets.cap.ui.tools.model.ProcessStateObservable;
import org.jowidgets.tools.validation.ValidationCache;
import org.jowidgets.tools.validation.ValidationCache.IValidationResultCreator;
import org.jowidgets.util.Assert;
import org.jowidgets.util.EmptyCheck;
import org.jowidgets.util.IProvider;
import org.jowidgets.util.Tuple;
import org.jowidgets.validation.IValidationConditionListener;
import org.jowidgets.validation.IValidationResult;
import org.jowidgets.validation.IValidationResultBuilder;
import org.jowidgets.validation.ValidationResult;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BeanRelationTreeModelImpl<CHILD_BEAN_TYPE> implements
		IBeanRelationTreeModel<CHILD_BEAN_TYPE>,
		IValidationResultCreator {

	private final IBeanRelationNodeModel<Void, CHILD_BEAN_TYPE> root;
	private final IBeanRelationNodeModelConfigurator nodeConfigurator;
	private final IBeanSelectionListener<Object> parentSelectionListener;
	private final BeanSelectionObservable<Object> beanSelectionObservable;
	private final ValidationCache validationCache;
	private final IValidationConditionListener validationConditionListener;
	private final ModificationStateObservable modificationStateObservable;
	private final IModificationStateListener modificationStateListener;
	private final ProcessStateObservable processStateObservable;
	private final IProcessStateListener processStateListener;

	private final BeanSelectionListener beanSelectionListener;
	private final Map relationNodes;

	private ArrayList<IBeanProxy<?>> selection;

	public BeanRelationTreeModelImpl(
		final IBeanRelationNodeModel<Void, CHILD_BEAN_TYPE> root,
		final IBeanRelationNodeModelConfigurator nodeConfigurator,
		final IBeanListModel<Object> parent,
		final LinkType linkType,
		final Long listenerDelay) {

		Assert.paramNotNull(root, "root");
		Assert.paramNotNull(nodeConfigurator, "nodeConfigurator");

		this.root = root;

		this.nodeConfigurator = nodeConfigurator;
		if (parent != null) {
			Assert.paramNotNull(linkType, "linkType");
			final IProvider<Object> parentBeanProvider = new IProvider<Object>() {
				@Override
				public Object get() {
					final List<IBeanProxy<?>> parentBeans = new LinkedList<IBeanProxy<?>>();
					for (final Integer selected : parent.getSelection()) {
						parentBeans.add(parent.getBean(selected.intValue()));
					}
					return parentBeans;
				}
			};
			this.parentSelectionListener = new ParentSelectionListener<Object>(this, parentBeanProvider, listenerDelay);
			parent.addBeanSelectionListener(parentSelectionListener);
		}
		else {
			this.parentSelectionListener = null;
		}

		this.relationNodes = new HashMap();

		this.beanSelectionObservable = new BeanSelectionObservable<Object>();
		this.beanSelectionListener = new BeanSelectionListener<Object>();

		this.validationCache = new ValidationCache(this);
		this.validationConditionListener = new ValidationConditionListener();

		this.modificationStateObservable = new ModificationStateObservable();
		this.modificationStateListener = new ModificationStateListener();

		this.processStateObservable = new ProcessStateObservable();
		this.processStateListener = new ProcessStateListener();

		registerListeners(root);
	}

	@Override
	public IBeanRelationNodeModel<Void, CHILD_BEAN_TYPE> getRoot() {
		return root;
	}

	@Override
	public <METHOD_PARENT_BEAN_TYPE, METHOD_CHILD_BEAN_TYPE> IBeanRelationNodeModel<METHOD_PARENT_BEAN_TYPE, METHOD_CHILD_BEAN_TYPE> getNode(
		final IEntityTypeId<METHOD_PARENT_BEAN_TYPE> parentEntityTypeId,
		final IBeanProxy<METHOD_PARENT_BEAN_TYPE> parentBean,
		final IEntityTypeId<METHOD_CHILD_BEAN_TYPE> childEntityTypeId) {

		final Tuple<IBeanProxy<METHOD_PARENT_BEAN_TYPE>, IEntityTypeId<METHOD_CHILD_BEAN_TYPE>> key;
		key = new Tuple<IBeanProxy<METHOD_PARENT_BEAN_TYPE>, IEntityTypeId<METHOD_CHILD_BEAN_TYPE>>(parentBean, childEntityTypeId);

		Object result = relationNodes.get(key);
		if (result == null) {
			BeanRelationNodeModelBuilder<METHOD_PARENT_BEAN_TYPE, METHOD_CHILD_BEAN_TYPE, IBeanRelationNodeModelBluePrint<?, ?>> builder;
			builder = new BeanRelationNodeModelBuilder<METHOD_PARENT_BEAN_TYPE, METHOD_CHILD_BEAN_TYPE, IBeanRelationNodeModelBluePrint<?, ?>>(
				parentEntityTypeId,
				parentBean,
				childEntityTypeId);
			nodeConfigurator.configureNode(childEntityTypeId, builder);
			final IBeanRelationNodeModel nodeModel = builder.build();
			registerListeners(nodeModel);
			result = nodeModel;
			relationNodes.put(key, result);
		}
		return (IBeanRelationNodeModel<METHOD_PARENT_BEAN_TYPE, METHOD_CHILD_BEAN_TYPE>) result;
	}

	private void registerListeners(final IBeanRelationNodeModel<?, ?> nodeModel) {
		nodeModel.addBeanSelectionListener(beanSelectionListener);
		nodeModel.addModificationStateListener(modificationStateListener);
		nodeModel.addProcessStateListener(processStateListener);
		nodeModel.addValidationConditionListener(validationConditionListener);
	}

	@Override
	public void load() {
		clear();
		root.load();
	}

	@Override
	public void clear() {
		root.clear();
		for (final Object relationModelObject : relationNodes.values()) {
			final IBeanRelationNodeModel<?, ?> relationModel = (IBeanRelationNodeModel<?, ?>) relationModelObject;
			relationModel.dispose();
		}
		relationNodes.clear();
	}

	@Override
	public void save() {
		if (root.hasModifications()) {
			root.save();
		}
		for (final Object relationModelObject : relationNodes.values()) {
			final IBeanRelationNodeModel<?, ?> relationModel = (IBeanRelationNodeModel<?, ?>) relationModelObject;
			if (relationModel.hasModifications()) {
				relationModel.save();
			}
		}
	}

	@Override
	public void undo() {
		if (root.hasModifications()) {
			root.undo();
		}
		for (final Object relationModelObject : relationNodes.values()) {
			final IBeanRelationNodeModel<?, ?> relationModel = (IBeanRelationNodeModel<?, ?>) relationModelObject;
			if (relationModel.hasModifications()) {
				relationModel.undo();
			}
		}
	}

	@Override
	public boolean hasModifications() {
		if (root.hasModifications()) {
			return true;
		}
		for (final Object relationModelObject : relationNodes.values()) {
			final IBeanRelationNodeModel<?, ?> relationModel = (IBeanRelationNodeModel<?, ?>) relationModelObject;
			if (relationModel.hasModifications()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasExecutions() {
		if (root.hasExecutions()) {
			return true;
		}
		for (final Object relationModelObject : relationNodes.values()) {
			final IBeanRelationNodeModel<?, ?> relationModel = (IBeanRelationNodeModel<?, ?>) relationModelObject;
			if (relationModel.hasExecutions()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void cancelExecutions() {
		root.cancelExecutions();
		for (final Object relationModelObject : relationNodes.values()) {
			final IBeanRelationNodeModel<?, ?> relationModel = (IBeanRelationNodeModel<?, ?>) relationModelObject;
			relationModel.cancelExecutions();
		}
	}

	@Override
	public IValidationResult validate() {
		return validationCache.validate();
	}

	@Override
	public IValidationResult createValidationResult() {
		final IValidationResultBuilder builder = ValidationResult.builder();
		builder.addResult(root.validate());
		for (final Object relationModelObject : relationNodes.values()) {
			final IBeanRelationNodeModel<?, ?> relationModel = (IBeanRelationNodeModel<?, ?>) relationModelObject;
			builder.addResult(relationModel.validate());
		}
		return builder.build();
	}

	@Override
	public void addValidationConditionListener(final IValidationConditionListener listener) {
		validationCache.addValidationConditionListener(listener);
	}

	@Override
	public void removeValidationConditionListener(final IValidationConditionListener listener) {
		validationCache.removeValidationConditionListener(listener);
	}

	@Override
	public void addModificationStateListener(final IModificationStateListener listener) {
		modificationStateObservable.addModificationStateListener(listener);
	}

	@Override
	public void removeModificationStateListener(final IModificationStateListener listener) {
		modificationStateObservable.removeModificationStateListener(listener);
	}

	@Override
	public void addProcessStateListener(final IProcessStateListener listener) {
		processStateObservable.addProcessStateListener(listener);
	}

	@Override
	public void removeProcessStateListener(final IProcessStateListener listener) {
		processStateObservable.removeProcessStateListener(listener);
	}

	@Override
	public void addBeanSelectionListener(final IBeanSelectionListener<Object> listener) {
		beanSelectionObservable.addBeanSelectionListener(listener);
	}

	@Override
	public void removeBeanSelectionListener(final IBeanSelectionListener<Object> listener) {
		beanSelectionObservable.removeBeanSelectionListener(listener);
	}

	@Override
	public ArrayList<IBeanProxy<?>> getSelection() {
		if (selection == null) {
			return new ArrayList<IBeanProxy<?>>();
		}
		else {
			return new ArrayList<IBeanProxy<?>>(selection);
		}
	}

	@Override
	public void setSelection(final Collection<? extends IBeanProxy<?>> selection) {
		if (EmptyCheck.isEmpty(selection)) {
			for (final Object object : getRelationNodes()) {
				final IBeanRelationNodeModel<Object, Object> relationNode = (IBeanRelationNodeModel<Object, Object>) object;
				final List<Integer> emptySelection = Collections.emptyList();
				relationNode.removeBeanSelectionListener(beanSelectionListener);
				relationNode.setSelection(emptySelection);
				relationNode.addBeanSelectionListener(beanSelectionListener);

			}
			this.selection = new ArrayList<IBeanProxy<?>>();
			beanSelectionObservable.fireBeanSelectionEvent(BeanRelationTreeModelImpl.this, null, null, getSelection());
		}
		//		else {
		//			TODO MG implement programmatic selection changed != emptySelection
		//		}
	}

	private Collection getRelationNodes() {
		final Collection result = new LinkedList(relationNodes.values());
		result.add(root);
		return result;
	}

	private final class BeanSelectionListener<BEAN_TYPE> implements IBeanSelectionListener<BEAN_TYPE> {

		@Override
		public void selectionChanged(final IBeanSelectionEvent<BEAN_TYPE> selectionEvent) {
			IBeanRelationNodeModel<?, ?> changedNode = null;
			for (final Object object : getRelationNodes()) {
				final IBeanRelationNodeModel<Object, Object> relationNode = (IBeanRelationNodeModel<Object, Object>) object;
				final List<Integer> emptySelection = Collections.emptyList();
				if (selectionEvent.getSource() != relationNode) {
					relationNode.removeBeanSelectionListener(beanSelectionListener);
					relationNode.setSelection(emptySelection);
					relationNode.addBeanSelectionListener(beanSelectionListener);
				}
				else {
					changedNode = relationNode;
				}
			}
			if (changedNode != null) {
				selection = new ArrayList<IBeanProxy<?>>(changedNode.getSelectedBeans());
				beanSelectionObservable.fireBeanSelectionEvent(
						BeanRelationTreeModelImpl.this,
						selectionEvent.getBeanType(),
						selectionEvent.getEntityId(),
						selectionEvent.getSelection());
			}

		}
	}

	private final class ModificationStateListener implements IModificationStateListener {
		@Override
		public void modificationStateChanged() {
			modificationStateObservable.fireModificationStateChanged();
		}
	}

	private final class ProcessStateListener implements IProcessStateListener {
		@Override
		public void processStateChanged() {
			processStateObservable.fireProcessStateChanged();
		}
	}

	private final class ValidationConditionListener implements IValidationConditionListener {
		@Override
		public void validationConditionsChanged() {
			validationCache.setDirty();
		}
	}

}
