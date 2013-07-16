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

package org.jowidgets.cap.service.hibernate.api;

import java.util.Collection;

import org.jowidgets.service.api.IServicesDecoratorProvider;

public interface ICancelServicesDecoratorProviderBuilder {

	int DEFAULT_ORDER = 2;

	/**
	 * Sets the services that should be decorated,
	 * so that the HibernateSession will be canceled when execution callback was canceled.
	 * 
	 * By default, the following services are set:
	 * ICreatorService.class,
	 * IReaderService.class,
	 * IRefreshService.class,
	 * IUpdaterService.class,
	 * IExecutorService.class,
	 * IDeleterService.class
	 * 
	 * Remark: Invoking this method will override the default. If services should be added to the default list,
	 * use the method {@link #addServices(Class...)} instead.
	 * 
	 * @param services The services (must be interfaces) that should be decorated
	 * 
	 * @return This builder
	 */
	ICancelServicesDecoratorProviderBuilder setServices(Collection<? extends Class<?>> services);

	/**
	 * Adds a service(s) to the list of services to decorate
	 * 
	 * @param services The service(s) to add, must not be null
	 * 
	 * @return This builder
	 */
	ICancelServicesDecoratorProviderBuilder addServices(Class<?>... services);

	/**
	 * Sets the order for the service decoration. Decorators with higher order will be invoked later.
	 * 
	 * By default, the {@link #DEFAULT_ORDER} will be used
	 * 
	 * @param order The order to set.
	 * 
	 * @return This builder
	 */
	ICancelServicesDecoratorProviderBuilder setOrder(int order);

	/**
	 * @return A new {@link IServicesDecoratorProvider}
	 */
	IServicesDecoratorProvider build();

}
