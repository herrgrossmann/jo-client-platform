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

package org.jowidgets.invocation.service.client.impl;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jowidgets.invocation.client.api.IInvocationClient;
import org.jowidgets.invocation.client.api.InvocationClientToolkit;
import org.jowidgets.invocation.common.api.ICancelService;
import org.jowidgets.invocation.common.api.IInvocationCallbackService;
import org.jowidgets.invocation.service.common.api.ICancelListener;
import org.jowidgets.invocation.service.common.api.IInterimRequestCallback;
import org.jowidgets.invocation.service.common.api.IInterimResponseCallback;
import org.jowidgets.invocation.service.common.api.IInvocationCallback;

final class InvocationCallbackService implements IInvocationCallbackService {

	private final Map<Object, MethodInvocationContext> invocationContexts;

	InvocationCallbackService() {
		this.invocationContexts = new ConcurrentHashMap<Object, MethodInvocationContext>();
	}

	@Override
	public void interimResponse(final Object invocationId, final Object progress) {
		final MethodInvocationContext context = invocationContexts.get(invocationId);
		if (context != null && context.getInterimResponseCallback() != null) {
			context.getInterimResponseCallback().response(progress);
		}
	}

	@Override
	public void interimRequest(final Object invocationId, final Object questionId, final Object question) {
		final MethodInvocationContext context = invocationContexts.get(invocationId);
		if (context != null && context.getInterimRequestCallback() != null) {
			final IInterimResponseCallback<Object> resultCallback = new IInterimResponseCallback<Object>() {
				@Override
				public void response(final Object result) {
					InvocationClientToolkit.getClient().getResponseService(context.getServerId());
				}
			};
			context.getInterimRequestCallback().request(resultCallback, question);
		}
	}

	@Override
	public void finished(final Object invocationId, final Object result) {
		final MethodInvocationContext context = invocationContexts.get(invocationId);
		if (context != null && context.getResultCallback() != null) {
			context.getResultCallback().finished(result);
			invocationContexts.remove(invocationId);
		}
	}

	@Override
	public void exeption(final Object invocationId, final Throwable exception) {
		final MethodInvocationContext context = invocationContexts.get(invocationId);
		if (context != null && context.getResultCallback() != null) {
			context.getResultCallback().exeption(exception);
			invocationContexts.remove(invocationId);
		}
	}

	Object registerInvocation(
		final IInvocationCallback<?> invocationCallback,
		final IInterimResponseCallback<?> interimResponseCallback,
		final IInterimRequestCallback<?, ?> interimRequestCallback,
		final long timeout,
		final Object serverId,
		final IInvocationClient invocationClient) {

		final Object invocationId = UUID.randomUUID();

		final MethodInvocationContext methodInvocation = new MethodInvocationContext(
			serverId,
			invocationCallback,
			interimResponseCallback,
			interimRequestCallback,
			timeout,
			System.currentTimeMillis());

		if (invocationCallback != null) {
			invocationCallback.addCancelListener(new ICancelListener() {
				@Override
				public void canceled() {
					final ICancelService cancelService = invocationClient.getCancelService(serverId);
					if (cancelService != null) {
						cancelService.canceled(invocationId);
					}
				}
			});
		}

		invocationContexts.put(invocationId, methodInvocation);

		return invocationId;
	}
}
