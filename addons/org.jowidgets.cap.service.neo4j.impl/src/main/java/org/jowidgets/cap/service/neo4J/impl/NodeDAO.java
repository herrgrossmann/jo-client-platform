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

package org.jowidgets.cap.service.neo4J.impl;

import org.jowidgets.cap.common.api.bean.IBean;
import org.jowidgets.cap.common.api.exception.ServiceException;
import org.jowidgets.cap.service.neo4j.api.GraphDBConfig;
import org.jowidgets.util.Assert;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

final class NodeDAO {

	private final String beanTypeId;
	private final Index<Node> nodeIndex;
	private final String beanTypePropertyName;

	NodeDAO(final String beanTypeId) {
		Assert.paramNotNull(beanTypeId, "beanTypeId");
		this.beanTypeId = BeanTypeIdUtil.toString(beanTypeId);
		this.nodeIndex = GraphDBConfig.getNodeIndex();
		this.beanTypePropertyName = GraphDBConfig.getBeanTypePropertyName();
	}

	Node findNode(final Object id) {
		Node result = null;
		for (final Node node : nodeIndex.get(IBean.ID_PROPERTY, id)) {
			if (beanTypeId.equals(node.getProperty(beanTypePropertyName))) {
				if (result != null) {
					result = node;
				}
				else {
					throw new ServiceException("More than one node found for the id '"
						+ id
						+ "' and the type '"
						+ beanTypeId
						+ "'.");
				}
			}
		}
		return result;
	}

}
