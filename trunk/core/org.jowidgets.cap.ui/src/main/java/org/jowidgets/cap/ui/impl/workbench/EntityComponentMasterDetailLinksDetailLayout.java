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

package org.jowidgets.cap.ui.impl.workbench;

import java.util.Map;
import java.util.Map.Entry;

import org.jowidgets.cap.common.api.bean.IBeanDtoDescriptor;
import org.jowidgets.cap.common.api.entity.IEntityClass;
import org.jowidgets.cap.common.api.entity.IEntityLinkDescriptor;
import org.jowidgets.cap.common.api.service.IEntityService;
import org.jowidgets.service.api.ServiceProvider;
import org.jowidgets.workbench.api.ILayout;
import org.jowidgets.workbench.toolkit.api.IFolderLayoutBuilder;
import org.jowidgets.workbench.toolkit.api.ILayoutBuilder;
import org.jowidgets.workbench.toolkit.api.ISplitLayoutBuilder;
import org.jowidgets.workbench.tools.FolderLayoutBuilder;
import org.jowidgets.workbench.tools.LayoutBuilder;
import org.jowidgets.workbench.tools.SplitLayoutBuilder;

public class EntityComponentMasterDetailLinksDetailLayout {

	public static final String DEFAULT_LAYOUT_ID = "DEFAULT_LAYOUT_ID";
	public static final String MASTER_FOLDER_ID = "MASTER_FOLDER_ID";
	public static final String DETAIL_FOLDER_ID = "DETAIL_FOLDER_ID";
	public static final String LINKED_MASTER_FOLDER_ID = "LINKED_MASTER_FOLDER_ID";
	public static final String LINKED_DETAIL_FOLDER_ID = "LINKED_DETAIL_FOLDER_ID";

	private static final String LINKS_STRING = Messages.getString("EntityComponentDefaultLayout.links");

	private final ILayout layout;

	public EntityComponentMasterDetailLinksDetailLayout(
		final IEntityClass entityClass,
		final Map<String, IEntityLinkDescriptor> links) {
		final ILayoutBuilder builder = new LayoutBuilder();
		builder.setId(DEFAULT_LAYOUT_ID).setLayoutContainer(createMainSplit(entityClass, links));
		this.layout = builder.build();
	}

	public ILayout getLayout() {
		return layout;
	}

	private ISplitLayoutBuilder createMainSplit(final IEntityClass entityClass, final Map<String, IEntityLinkDescriptor> links) {
		final ISplitLayoutBuilder result = new SplitLayoutBuilder();
		result.setVertical().setWeight(0.5).setResizeFirst();
		result.setFirstContainer(createMasterFolder(entityClass));
		result.setSecondContainer(createLinkedMasterDetailSplit(entityClass, links));
		return result;
	}

	private IFolderLayoutBuilder createMasterFolder(final IEntityClass entityClass) {
		final IFolderLayoutBuilder result = new FolderLayoutBuilder(MASTER_FOLDER_ID);
		result.setViewsCloseable(false);
		result.addView(EntityComponent.ROOT_TABLE_VIEW_ID, entityClass.getLabel(), entityClass.getDescription()); //$NON-NLS-1$
		return result;
	}

	private ISplitLayoutBuilder createLinkedMasterDetailSplit(
		final IEntityClass entityClass,
		final Map<String, IEntityLinkDescriptor> links) {
		final ISplitLayoutBuilder result = new SplitLayoutBuilder();
		result.setHorizontal().setWeight(0.3).setResizeBoth();
		result.setFirstContainer(createLinkedMasterFolder(entityClass, links));
		result.setSecondContainer(createLinkedDetailFolder(entityClass, links));
		return result;
	}

	private IFolderLayoutBuilder createLinkedMasterFolder(
		final IEntityClass entityClass,
		final Map<String, IEntityLinkDescriptor> links) {
		final IFolderLayoutBuilder result = new FolderLayoutBuilder(LINKED_MASTER_FOLDER_ID);
		result.setViewsCloseable(false);
		result.addView(EntityRelationTreeView.ID, entityClass.getLabel() + " " + LINKS_STRING, entityClass.getDescription());
		return result;
	}

	private IFolderLayoutBuilder createLinkedDetailFolder(
		final IEntityClass entityClass,
		final Map<String, IEntityLinkDescriptor> links) {
		final IFolderLayoutBuilder result = new FolderLayoutBuilder(LINKED_DETAIL_FOLDER_ID);
		result.setViewsCloseable(false);
		result.addView(EntityMultiDetailView.ID, EntityMultiDetailView.DEFAULT_LABEL);
		for (final Entry<String, IEntityLinkDescriptor> linkEntry : links.entrySet()) {
			final IEntityService entityService = ServiceProvider.getService(IEntityService.ID);
			if (entityService != null) {
				final IBeanDtoDescriptor descriptor = entityService.getDescriptor(linkEntry.getValue().getLinkedEntityId());
				result.addView(linkEntry.getKey(), descriptor.getLabelPlural(), descriptor.getDescription());
			}

		}
		return result;
	}

}
