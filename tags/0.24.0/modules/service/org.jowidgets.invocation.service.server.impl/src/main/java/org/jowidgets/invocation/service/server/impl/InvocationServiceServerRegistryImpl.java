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

package org.jowidgets.invocation.service.server.impl;

import org.jowidgets.invocation.common.api.IMethod;
import org.jowidgets.invocation.server.api.IInvocationServer;
import org.jowidgets.invocation.server.api.IInvocationServerServiceRegistry;
import org.jowidgets.invocation.server.api.InvocationServerToolkit;
import org.jowidgets.invocation.service.common.api.IMethodInvocationService;
import org.jowidgets.invocation.service.server.api.IInvocationServiceServerRegistry;

final class InvocationServiceServerRegistryImpl implements IInvocationServiceServerRegistry {

	private final IInvocationServer invocationServer;
	private final IInvocationServerServiceRegistry invocationServerServiceRegistry;
	private final CancelServiceImpl cancelService;
	private final ResponseServiceImpl responseService;

	InvocationServiceServerRegistryImpl(
		final Object brokerId,
		final CancelServiceImpl cancelService,
		final ResponseServiceImpl responseService) {
		this.invocationServer = InvocationServerToolkit.getServer(brokerId);
		this.invocationServerServiceRegistry = InvocationServerToolkit.getRegistry(brokerId);
		this.cancelService = cancelService;
		this.responseService = responseService;
	}

	@Override
	public void register(final String methodName, final IMethodInvocationService<?, ?, ?, ?, ?> methodService) {
		final IMethod method = new MethodImpl(invocationServer, cancelService, responseService, methodService);
		invocationServerServiceRegistry.register(methodName, method);
	}
}
