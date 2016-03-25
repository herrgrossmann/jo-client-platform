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

package org.jowidgets.cap.service.tools.exception;

import org.jowidgets.cap.service.api.exception.IServiceExceptionLogger;
import org.jowidgets.logging.api.ILogger;
import org.jowidgets.logging.api.LoggerProvider;

public class DefaultServiceExceptionLogger implements IServiceExceptionLogger {

	private static final String THIS_WRAPPER_FQCN = DefaultServiceExceptionLogger.class.getName();

	private final ILogger logger;

	public DefaultServiceExceptionLogger(final String loggerName) {
		this(loggerName, null);
	}

	public DefaultServiceExceptionLogger(final String loggerName, final String wrapperFQCN) {
		this.logger = LoggerProvider.get(loggerName, wrapperFQCN != null ? wrapperFQCN : THIS_WRAPPER_FQCN);
	}

	@Override
	public void log(final Class<?> serviceType, final Throwable original, final Throwable decorated) {
		//TODO log with different log levels

		logger.error("Error invoking service of type '" + serviceType + "'", decorated);
		if (original != decorated) {
			logger.error("Caused by: ", original);
		}

	}

}
