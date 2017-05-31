/*
 * Copyright (c) 2016, grossmann
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

package org.jowidgets.cap.service.tools.crud;

import java.util.Collection;
import java.util.List;

import org.jowidgets.cap.common.api.bean.IBeanKey;
import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.service.api.creator.IBeanDataMapper;
import org.jowidgets.cap.service.api.crud.ICrudServiceInterceptor;
import org.jowidgets.cap.service.api.updater.IBeanModificationsMap;

public class CrudServiceInterceptorAdapter<BEAN_TYPE> implements ICrudServiceInterceptor<BEAN_TYPE> {

	@Override
	public void beforeInitializeForCreation(
		final List<IBeanKey> parentBeanKeys,
		final Collection<BEAN_TYPE> beans,
		final IBeanDataMapper<BEAN_TYPE> beanDataMapper,
		final IExecutionCallback executionCallback) {}

	@Override
	public void afterInitializeForCreation(
		final List<IBeanKey> parentBeanKeys,
		final Collection<BEAN_TYPE> beans,
		final IBeanDataMapper<BEAN_TYPE> beanDataMapper,
		final IExecutionCallback executionCallback) {}

	@Override
	public void afterCreation(
		final List<IBeanKey> parentBeanKeys,
		final Collection<BEAN_TYPE> beans,
		final IBeanDataMapper<BEAN_TYPE> beanDataMapper,
		final IExecutionCallback executionCallback) {}

	@Override
	public void beforeUpdate(
		final Collection<BEAN_TYPE> beans,
		final IBeanModificationsMap<BEAN_TYPE> modifications,
		final IExecutionCallback executionCallback) {}

	@Override
	public void afterUpdate(
		final Collection<BEAN_TYPE> beans,
		final IBeanModificationsMap<BEAN_TYPE> modifications,
		final IExecutionCallback executionCallback) {}

	@Override
	public void beforeDelete(final Collection<BEAN_TYPE> beans, final IExecutionCallback executionCallback) {}

	@Override
	public void afterDelete(final Collection<BEAN_TYPE> beans, final IExecutionCallback executionCallback) {}

}