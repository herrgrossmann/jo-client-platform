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

package org.jowidgets.cap.ui.impl.widgets;

import org.jowidgets.api.validation.IValidator;
import org.jowidgets.api.validation.ValidationResult;
import org.jowidgets.api.widgets.IInputComposite;
import org.jowidgets.cap.ui.api.bean.IBeanProxy;
import org.jowidgets.cap.ui.api.widgets.IBeanForm;
import org.jowidgets.common.widgets.controler.IInputListener;
import org.jowidgets.tools.widgets.wrapper.ControlWrapper;

final class BeanFormImpl<BEAN_TYPE> extends ControlWrapper implements IBeanForm<BEAN_TYPE> {

	private final BeanFormContentCreator<Object> beanFormContentCreator;

	BeanFormImpl(
		final IInputComposite<IBeanProxy<BEAN_TYPE>> composite,
		final BeanFormContentCreator<Object> beanFormContentCreator) {
		super(composite);
		this.beanFormContentCreator = beanFormContentCreator;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IInputComposite<IBeanProxy<BEAN_TYPE>> getWidget() {
		return (IInputComposite<IBeanProxy<BEAN_TYPE>>) super.getWidget();
	}

	@Override
	public boolean isEmpty() {
		return getWidget().isEmpty();
	}

	@Override
	public boolean isMandatory() {
		return getWidget().isMandatory();
	}

	@Override
	public void setMandatory(final boolean mandatory) {
		getWidget().setMandatory(mandatory);
	}

	@Override
	public void setValue(final IBeanProxy<BEAN_TYPE> value) {
		getWidget().setValue(value);
	}

	@Override
	public IBeanProxy<BEAN_TYPE> getValue() {
		return getWidget().getValue();
	}

	@Override
	public void addValidator(final IValidator<IBeanProxy<BEAN_TYPE>> validator) {
		getWidget().addValidator(validator);
	}

	@Override
	public ValidationResult validate() {
		return getWidget().validate();
	}

	@Override
	public void setEditable(final boolean editable) {
		getWidget().setEditable(editable);
	}

	@Override
	public void addInputListener(final IInputListener listener) {
		getWidget().addInputListener(listener);
	}

	@Override
	public void removeInputListener(final IInputListener listener) {
		getWidget().removeInputListener(listener);
	}

	@Override
	public void resetValidation() {
		getWidget().resetValidation();
		beanFormContentCreator.resetValidation();
	}

}
