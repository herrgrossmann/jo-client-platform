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

package org.jowidgets.cap.common.api.link;

import java.util.Collection;

import org.jowidgets.cap.common.api.bean.IBeanData;
import org.jowidgets.cap.common.api.bean.IBeanKey;

/**
 * A LinkCreation defines a set of links where each source bean (transient or persistent) should be
 * linked with each linkable bean (transient or persistent). Each link may have additional properties
 * that will be set on the link bean.
 */
public interface ILinkCreation {

	/**
	 * @return The keys of the source beans that should be linked, may be empty but not null
	 */
	Collection<IBeanKey> getSourceBeans();

	/**
	 * @return The bean data for the source beans that should be created, may be empty but not null
	 */
	Collection<IBeanData> getTransientSourceBeans();

	/**
	 * @return The additional properties for each created link, may be null if no additional properties are defined
	 */
	IBeanData getAdditionalLinkProperties();

	/**
	 * @return The keys of the linkable beans that should be linked, may be empty but not null
	 */
	Collection<IBeanKey> getLinkableBeans();

	/**
	 * @return The bean data for the linkable beans that should be created, may be empty but not null
	 */
	Collection<IBeanData> getTransientLinkableBeans();

}