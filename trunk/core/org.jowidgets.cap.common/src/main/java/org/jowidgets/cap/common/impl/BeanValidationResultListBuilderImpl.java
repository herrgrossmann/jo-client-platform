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

package org.jowidgets.cap.common.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jowidgets.cap.common.api.validation.IBeanValidationResult;
import org.jowidgets.cap.common.api.validation.IBeanValidationResultListBuilder;
import org.jowidgets.util.Assert;
import org.jowidgets.validation.IValidationResult;

final class BeanValidationResultListBuilderImpl implements IBeanValidationResultListBuilder {

	private final List<IBeanValidationResult> resultList;

	BeanValidationResultListBuilderImpl() {
		this.resultList = new LinkedList<IBeanValidationResult>();
	}

	@Override
	public IBeanValidationResultListBuilder addResult(final IBeanValidationResult validationResult) {
		Assert.paramNotNull(validationResult, "validationResult");
		resultList.add(validationResult);
		return this;
	}

	@Override
	public IBeanValidationResultListBuilder addResult(final IValidationResult validationResult) {
		resultList.add(new BeanValidationResultImpl(null, validationResult));
		return this;
	}

	@Override
	public IBeanValidationResultListBuilder addResult(
		final IValidationResult validationResult,
		final Collection<String> propertyNames) {
		Assert.paramNotEmpty(propertyNames, "propertyNames");
		addResult(validationResult, propertyNames.toArray(new String[propertyNames.size()]));
		return null;
	}

	@Override
	public IBeanValidationResultListBuilder addResult(final IValidationResult validationResult, final String... propertyNames) {
		Assert.paramNotEmpty(propertyNames, "propertyNames");
		for (final String propertyName : propertyNames) {
			resultList.add(new BeanValidationResultImpl(propertyName, validationResult));
		}
		return this;
	}

	@Override
	public List<IBeanValidationResult> build() {
		return Collections.unmodifiableList(new LinkedList<IBeanValidationResult>(resultList));
	}

}
