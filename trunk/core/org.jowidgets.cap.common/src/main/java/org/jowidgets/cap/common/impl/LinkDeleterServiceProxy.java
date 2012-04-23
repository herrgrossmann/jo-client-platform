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

package org.jowidgets.cap.common.impl;

import java.io.Serializable;
import java.util.Collection;

import org.jowidgets.cap.common.api.execution.IExecutionCallback;
import org.jowidgets.cap.common.api.execution.IResultCallback;
import org.jowidgets.cap.common.api.link.ILinkDeletion;
import org.jowidgets.cap.common.api.service.ILinkDeleterService;
import org.jowidgets.service.api.IServiceId;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.util.Assert;

final class LinkDeleterServiceProxy implements ILinkDeleterService, Serializable {

	private static final long serialVersionUID = 850203997764661850L;

	private final IServiceId<ILinkDeleterService> serviceId;

	private ILinkDeleterService original;

	public LinkDeleterServiceProxy(final IServiceId<ILinkDeleterService> serviceId) {
		Assert.paramNotNull(serviceId, "serviceId");
		this.serviceId = serviceId;
	}

	@Override
	public void delete(
		final IResultCallback<Void> result,
		final Collection<? extends ILinkDeletion> linksDeletions,
		final IExecutionCallback executionCallback) {
		getOriginal().delete(result, linksDeletions, executionCallback);
	}

	private ILinkDeleterService getOriginal() {
		if (original == null) {
			original = ServiceProvider.getService(serviceId);
			if (original == null) {
				throw new IllegalStateException("No link deleter service found for the entity id '" + serviceId + "'.");
			}
		}
		return original;
	}
}
