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

package org.jowidgets.cap.service.api.executor;

import java.util.List;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.execution.IExecutableChecker;
import org.jowidgets.cap.common.api.service.IExecutorService;

public interface IExecutorServiceBuilder<BEAN_TYPE extends IBean, PARAM_TYPE> {

	IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(
		IBeanExecutor<? extends BEAN_TYPE, ? extends PARAM_TYPE> beanExecutor);

	IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setExecutor(
		IBeanListExecutor<? extends BEAN_TYPE, ? extends PARAM_TYPE> beanListExecutor);

	IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setExecutableChecker(IExecutableChecker<? extends BEAN_TYPE> executableChecker);

	IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setPropertyNames(List<String> propertyNames);

	IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setAllowDeletedBeans(boolean allowDeletedBeans);

	IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setAllowStaleBeans(boolean allowStaleBeans);

	IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setExecutionCallbackDelay(long delay);

	IExecutorServiceBuilder<BEAN_TYPE, PARAM_TYPE> setDelayedExecutionCallback(boolean delayedCallback);

	ISyncExecutorService<PARAM_TYPE> buildSyncExecuterService();

	IExecutorService<PARAM_TYPE> build();

}
