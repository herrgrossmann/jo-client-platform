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
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.common.api.bean.IBeanDto;
import org.jowidgets.cap.common.api.exception.BeanValidationException;
import org.jowidgets.cap.common.api.exception.ExecutableCheckException;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.execution.IExecutableState;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.common.api.validation.IBeanValidator;
import org.jowidgets.cap.common.tools.validation.BeanValidationResultUtil;
import org.jowidgets.cap.service.api.CapServiceToolkit;
import org.jowidgets.cap.service.api.adapter.ISyncCreatorService;
import org.jowidgets.cap.service.api.bean.IBeanDtoFactory;
import org.jowidgets.cap.service.api.bean.IBeanInitializer;
import org.jowidgets.util.Assert;

final class SyncJpaCreatorServiceImpl<BEAN_TYPE extends IBean> implements ISyncCreatorService {

	private final Class<? extends BEAN_TYPE> beanType;
	private final IBeanDtoFactory<BEAN_TYPE> dtoFactory;
	private final IBeanInitializer<BEAN_TYPE> beanInitializer;
	private final IExecutableChecker<BEAN_TYPE> executableChecker;
	private final IBeanValidator<BEAN_TYPE> beanValidator;

	SyncJpaCreatorServiceImpl(
		final Class<? extends BEAN_TYPE> beanType,
		final IBeanDtoFactory<BEAN_TYPE> dtoFactory,
		final IBeanInitializer<BEAN_TYPE> beanInitializer,
		final IExecutableChecker<BEAN_TYPE> executableChecker,
		final IBeanValidator<BEAN_TYPE> beanValidator) {

		Assert.paramNotNull(beanType, "beanType");
		Assert.paramNotNull(dtoFactory, "dtoFactory");
		Assert.paramNotNull(beanInitializer, "beanInitializer");

		this.beanType = beanType;
		this.dtoFactory = dtoFactory;
		this.beanInitializer = beanInitializer;
		this.executableChecker = executableChecker;
		this.beanValidator = beanValidator;
	}

	@Override
	public List<IBeanDto> create(final Collection<? extends IBeanData> beansData, final IExecutionCallback executionCallback) {
		final List<IBeanDto> result = new LinkedList<IBeanDto>();
		for (final IBeanData beanData : beansData) {
			final BEAN_TYPE bean;
			try {
				bean = beanType.newInstance();
			}
			catch (final InstantiationException e) {
				throw new RuntimeException(e);
			}
			catch (final IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			final EntityManager entityManager = EntityManagerProvider.get();

			CapServiceToolkit.checkCanceled(executionCallback);
			beanInitializer.initialize(bean, beanData);

			checkExecutableStates(bean);
			validate(bean);

			CapServiceToolkit.checkCanceled(executionCallback);

			if (bean.getId() == null) {
				entityManager.persist(bean);
			}

			CapServiceToolkit.checkCanceled(executionCallback);

			entityManager.flush();

			result.add(dtoFactory.createDto(bean));
		}
		return result;
	}

	private void checkExecutableStates(final BEAN_TYPE bean) {
		if (executableChecker != null) {
			final IExecutableState checkResult = executableChecker.getExecutableState(bean);
			if (checkResult != null && !checkResult.isExecutable()) {
				throw new ExecutableCheckException(bean.getId(), checkResult.getReason());
			}
		}
	}

	private void validate(final BEAN_TYPE bean) {
		if (beanValidator != null) {
			final Collection<IBeanValidationResult> validationResults = beanValidator.validate(bean);
			final IBeanValidationResult worstFirst = BeanValidationResultUtil.getWorstFirst(validationResults);
			if (worstFirst != null && !worstFirst.getValidationResult().isValid()) {
				throw new BeanValidationException(bean.getId(), worstFirst);
			}
		}
	}

}