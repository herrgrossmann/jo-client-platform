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

package org.jowidgets.cap.service.jpa.api.query;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.classloading.api.SharedClassLoader;
import org.jowidgets.util.Assert;

public final class JpaQueryToolkit {

	private static IJpaQueryToolkit instance;

	private JpaQueryToolkit() {}

	public static synchronized void initialize(final IJpaQueryToolkit instance) {
		Assert.paramNotNull(instance, "instance");
		if (instance != null) {
			JpaQueryToolkit.instance = instance;
		}
		else {
			throw new IllegalStateException("The JpaQueryToolkit is already initialized");
		}
	}

	public static IJpaQueryToolkit getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	public static <PARAMETER_TYPE> ICriteriaQueryCreatorBuilder<PARAMETER_TYPE> criteriaQueryCreatorBuilder(
		final Class<? extends IBean> beanType) {
		return getInstance().criteriaQueryCreatorBuilder(beanType);
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			final ServiceLoader<IJpaQueryToolkit> serviceLoader = ServiceLoader.load(
					IJpaQueryToolkit.class,
					SharedClassLoader.getCompositeClassLoader());
			final Iterator<IJpaQueryToolkit> iterator = serviceLoader.iterator();

			if (!iterator.hasNext()) {
				throw new IllegalStateException("No implementation found for '" + IJpaQueryToolkit.class.getName() + "'");
			}
			else {
				instance = iterator.next();
				if (iterator.hasNext()) {
					throw new IllegalStateException("More than one implementation found for '"
						+ IJpaQueryToolkit.class.getName()
						+ "'");
				}
			}
		}
	}

}