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

package org.jowidgets.cap.remoting.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Set;

import org.jowidgets.cap.remoting.common.CapInvocationMethodNames;
import org.jowidgets.invocation.service.client.api.IInvocationServiceClient;
import org.jowidgets.invocation.service.client.api.InvocationServiceClientToolkit;
import org.jowidgets.invocation.service.common.api.IMethodInvocationService;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.tools.ServiceProviderBuilder;

final class CapClientServiceProviderBuilder extends ServiceProviderBuilder {

	CapClientServiceProviderBuilder() {
		super();
		final IInvocationServiceClient invocationServiceClient = InvocationServiceClientToolkit.getClient();
		final IMethodInvocationService<Set<? extends IServiceId<?>>, Void, Void, Void, Void> methodService;
		methodService = invocationServiceClient.getMethodService(CapInvocationMethodNames.SERVICE_LOCATOR_METHOD_NAME);
		final SyncInvocationCallback<Set<? extends IServiceId<?>>> invocationCallback = new SyncInvocationCallback<Set<? extends IServiceId<?>>>();
		methodService.invoke(invocationCallback, null, null, null);
		addServices(invocationCallback.getResultSynchronious());
	}

	private void addServices(final Set<? extends IServiceId<?>> serviceIds) {
		for (final IServiceId<?> serviceId : serviceIds) {
			addService(serviceId);
		}
	}

	private void addService(final IServiceId<?> serviceId) {
		addService(serviceId, getService(serviceId));
	}

	private Object getService(final IServiceId<?> serviceId) {
		final Class<?> serviceType = serviceId.getServiceType();
		final InvocationHandler invocationHandler = new RemoteMethodInvocationHandler(serviceId);
		return Proxy.newProxyInstance(serviceType.getClassLoader(), new Class[] {serviceType}, invocationHandler);
	}
}