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

package org.jowidgets.cap.service.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Executor;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.util.Assert;
import org.jowidgets.util.IDecorator;

final class GenericServiceAsyncDecorator implements IDecorator<Object> {

	private final Executor executor;
	private final Class<?> serviceType;

	GenericServiceAsyncDecorator(final Class<?> serviceType, final Executor executor) {
		this.serviceType = serviceType;
		this.executor = executor;
	}

	@Override
	public Object decorate(final Object original) {
		Assert.paramNotNull(original, "original");
		final InvocationHandler invocationHandler = new AsyncInvocationHandler(original);
		return Proxy.newProxyInstance(serviceType.getClassLoader(), new Class[] {serviceType}, invocationHandler);
	}

	private final class AsyncInvocationHandler implements InvocationHandler {

		private final Object original;

		private AsyncInvocationHandler(final Object original) {
			this.original = original;
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			try {
				return doInvoke(method, args);
			}
			catch (final Exception e) {
				throw new RuntimeException("Error while invoking method '" + method.getName() + "' on '" + proxy + "'", e);
			}
		}

		private Object doInvoke(final Method method, final Object[] args) throws Exception {
			final Class<?>[] parameterTypes = method.getParameterTypes();

			final int resultCallbackIndex = getFirstMatchingIndex(IResultCallback.class, parameterTypes);

			if (resultCallbackIndex == -1) {
				return method.invoke(original, args);
			}
			else {
				final int executionCallbackIndex = getFirstMatchingIndex(IExecutionCallback.class, parameterTypes);
				final IExecutionCallback executionCallback = (IExecutionCallback) args[executionCallbackIndex];

				@SuppressWarnings("unchecked")
				final IResultCallback<Object> resultCallback = (IResultCallback<Object>) args[resultCallbackIndex];

				args[resultCallbackIndex] = getDecoratedResultCallback(resultCallback, executionCallback);
				invokeAsync(resultCallback, method, args);
				return null;
			}
		}

		private void invokeAsync(final IResultCallback<Object> resultCallback, final Method method, final Object[] args) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						method.invoke(original, args);
					}
					catch (final Exception exception) {
						resultCallback.exception(exception);
					}
				}
			});
		}

		private IResultCallback<Object> getDecoratedResultCallback(
			final IResultCallback<Object> original,
			final IExecutionCallback executionCallback) {
			if (executionCallback == null) {
				return original;
			}
			else {
				return new IResultCallback<Object>() {

					@Override
					public void finished(final Object result) {
						if (!executionCallback.isCanceled()) {
							original.finished(result);
						}
					}

					@Override
					public void exception(final Throwable exception) {
						if (!executionCallback.isCanceled()) {
							original.exception(exception);
						}
					}

					@Override
					public void timeout() {
						if (!executionCallback.isCanceled()) {
							original.timeout();
						}
					}
				};
			}
		}

		private int getFirstMatchingIndex(final Class<?> interfaceType, final Class<?>[] paramTypes) {
			if (paramTypes != null) {
				for (int i = 0; i < paramTypes.length; i++) {
					if (interfaceType.isAssignableFrom(paramTypes[i])) {
						return i;
					}
				}
			}
			return -1;
		}

	}
}
