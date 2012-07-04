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

package org.jowidgets.cap.service.jpa.impl;

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.cap.common.api.service.IReaderService;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncReaderService;
import org.jowidgets.cap.service.api.bean.IBeanAccess;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.cap.service.api.bean.IBeanModifier;
import org.jowidgets.cap.service.api.creator.ICreatorServiceBuilder;
import org.jowidgets.cap.service.api.deleter.IDeleterServiceBuilder;
import org.jowidgets.cap.service.api.entity.IBeanServicesProviderBuilder;
import org.jowidgets.cap.service.api.updater.IUpdaterServiceBuilder;
import org.jowidgets.cap.service.jpa.api.IJpaServiceFactory;
import org.jowidgets.cap.service.jpa.api.query.ICriteriaQueryCreatorBuilder;
import org.jowidgets.cap.service.jpa.api.query.IQueryCreator;
import org.jowidgets.cap.service.jpa.api.query.JpaQueryToolkit;
import org.jowidgets.cap.service.tools.factory.AbstractBeanServiceFactory;
import org.jowidgets.service.api.IServiceRegistry;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IAdapterFactory;

public class JpaServiceFactoryImpl extends AbstractBeanServiceFactory implements IJpaServiceFactory {

	@Override
	public <BEAN_TYPE extends IBean> IBeanAccess<BEAN_TYPE> beanAccess(final Class<? extends BEAN_TYPE> beanType) {
		return new JpaBeanAccessImpl<BEAN_TYPE>(beanType);
	}

	@Override
	public <BEAN_TYPE extends IBean> IBeanServicesProviderBuilder beanServicesBuilder(
		final IServiceRegistry registry,
		final Object entityTypeId,
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer,
		final IBeanModifier<BEAN_TYPE> beanModifier) {

		Assert.paramNotNull(registry, "registry");
		Assert.paramNotNull(entityTypeId, "entityTypeId");
		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(beanDtoFactory, "beanDtoFactory");
		Assert.paramNotNull(beanInitializer, "beanInitializer");
		Assert.paramNotNull(beanModifier, "beanModifier");

		final IBeanServicesProviderBuilder builder;
		builder = CapServiceToolkit.beanServicesProviderBuilder(registry, IEntityService.ID, entityTypeId);

		//create bean access
		final IBeanAccess<BEAN_TYPE> beanAccess = beanAccess(beanType);

		//set creator service
		final ICreatorServiceBuilder<BEAN_TYPE> creatorServiceBuilder = creatorServiceBuilder(beanType);
		creatorServiceBuilder.setBeanDtoFactory(beanDtoFactory).setBeanInitializer(beanInitializer);
		builder.setCreatorService(creatorServiceBuilder.build());

		//set reader service
		final ICriteriaQueryCreatorBuilder<Void> queryCreatorBuilder = JpaQueryToolkit.criteriaQueryCreatorBuilder(beanType);
		builder.setReaderService(readerService(queryCreatorBuilder.build(), beanDtoFactory));

		//set refresh
		builder.setRefreshService(CapServiceToolkit.refreshServiceBuilder(beanAccess).setBeanDtoFactory(beanDtoFactory).build());

		//set updater service
		final IUpdaterServiceBuilder<BEAN_TYPE> updaterServiceBuilder = CapServiceToolkit.updaterServiceBuilder(beanAccess);
		updaterServiceBuilder.setBeanDtoFactory(beanDtoFactory).setBeanModifier(beanModifier);
		builder.setUpdaterService(updaterServiceBuilder.build());

		//set deleter service
		builder.setDeleterService(deleterService(beanType));

		return builder;
	}

	@Override
	public <BEAN_TYPE extends IBean> ICreatorServiceBuilder<BEAN_TYPE> creatorServiceBuilder(
		final Class<? extends BEAN_TYPE> beanType) {
		Assert.paramNotNull(beanType, "beanType");
		return new JpaCreatorServiceBuilderImpl<BEAN_TYPE>(beanType);
	}

	@Override
	public <BEAN_TYPE extends IBean, PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory) {
		final ICriteriaQueryCreatorBuilder<PARAM_TYPE> queryCreatorBuilder = JpaQueryToolkit.criteriaQueryCreatorBuilder(beanType);
		return readerService(queryCreatorBuilder.build(), beanDtoFactory);
	}

	@Override
	public <PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		final Class<? extends IBean> beanType,
		final IQueryCreator<PARAM_TYPE> queryCreator,
		final Collection<String> propertyNames) {
		return readerService(queryCreator, CapServiceToolkit.dtoFactory(beanType, propertyNames));
	}

	@Override
	public <BEAN_TYPE extends IBean, PARAM_TYPE> IReaderService<PARAM_TYPE> readerService(
		final IQueryCreator<PARAM_TYPE> queryCreator,
		final IBeanDtoFactory<BEAN_TYPE> beanDtoFactory) {
		final ISyncReaderService<PARAM_TYPE> result = new SyncJpaReaderServiceImpl<PARAM_TYPE>(queryCreator, beanDtoFactory);
		final IAdapterFactory<IReaderService<PARAM_TYPE>, ISyncReaderService<PARAM_TYPE>> adapterFactory;
		adapterFactory = CapServiceToolkit.adapterFactoryProvider().reader();
		return adapterFactory.createAdapter(result);

	}

	@Override
	public <BEAN_TYPE extends IBean> IDeleterServiceBuilder<BEAN_TYPE> deleterServiceBuilder(
		final Class<? extends BEAN_TYPE> beanType) {
		return new JpaDeleterServiceBuilderImpl<BEAN_TYPE>(beanAccess(beanType));
	}

}
