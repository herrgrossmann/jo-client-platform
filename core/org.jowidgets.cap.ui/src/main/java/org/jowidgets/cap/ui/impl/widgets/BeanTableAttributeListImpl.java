/*
 * Copyright (c) 2011, nimoll
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jowidgets.api.color.Colors;
import org.jowidgets.api.command.IAction;
import org.jowidgets.api.command.ICommandExecutor;
import org.jowidgets.api.command.IExecutionContext;
import org.jowidgets.api.image.IconsSmall;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.api.widgets.ICheckBox;
import org.jowidgets.api.widgets.IComboBox;
import org.jowidgets.api.widgets.IContainer;
import org.jowidgets.api.widgets.IControl;
import org.jowidgets.api.widgets.IScrollComposite;
import org.jowidgets.api.widgets.IToolBar;
import org.jowidgets.api.widgets.IToolBarButton;
import org.jowidgets.api.widgets.blueprint.ITextLabelBluePrint;
import org.jowidgets.api.widgets.blueprint.factory.IBluePrintFactory;
import org.jowidgets.cap.common.api.sort.SortOrder;
import org.jowidgets.cap.ui.api.attribute.DisplayFormat;
import org.jowidgets.cap.ui.api.attribute.IAttribute;
import org.jowidgets.cap.ui.api.attribute.IAttributeConfig;
import org.jowidgets.cap.ui.api.attribute.IAttributeGroup;
import org.jowidgets.cap.ui.api.attribute.IControlPanelProvider;
import org.jowidgets.cap.ui.api.table.IBeanTableModel;
import org.jowidgets.common.color.ColorValue;
import org.jowidgets.common.color.IColorConstant;
import org.jowidgets.common.types.AlignmentHorizontal;
import org.jowidgets.common.types.Dimension;
import org.jowidgets.common.types.Markup;
import org.jowidgets.common.types.Rectangle;
import org.jowidgets.common.widgets.controler.IInputListener;
import org.jowidgets.common.widgets.layout.ILayouter;
import org.jowidgets.common.widgets.layout.MigLayoutDescriptor;
import org.jowidgets.tools.command.ActionBuilder;
import org.jowidgets.tools.widgets.wrapper.ContainerWrapper;
import org.jowidgets.util.NullCompatibleEquivalence;

@SuppressWarnings("unused")
final class BeanTableAttributeListImpl extends ContainerWrapper {

	private static final IColorConstant ATTRIBUTE_GROUP_BACKGROUND = new ColorValue(130, 177, 236);

	private static final IColorConstant[] NO_COLORS = new IColorConstant[0];
	private static final IColorConstant[] ALTERNATING_GROUPS = new IColorConstant[] {
			new ColorValue(130, 177, 236), new ColorValue(115, 162, 255)};

	private static final IColorConstant[] ALTERNATING_ATTRIBUTES = new IColorConstant[] {
			Colors.DEFAULT_TABLE_EVEN_BACKGROUND_COLOR, Colors.WHITE};

	private static final ITextLabelBluePrint LABEL_HEADER = Toolkit.getBluePrintFactory().textLabel().setColor(Colors.WHITE).setMarkup(
			Markup.STRONG).alignCenter();
	private static final ITextLabelBluePrint LABEL_GROUPS = Toolkit.getBluePrintFactory().textLabel().setMarkup(Markup.STRONG).setColor(
			Colors.WHITE);
	private static final ITextLabelBluePrint LABEL_ATTRIBUTES = Toolkit.getBluePrintFactory().textLabel();
	private static final ITextLabelBluePrint LABEL_ALL = Toolkit.getBluePrintFactory().textLabel().setMarkup(Markup.STRONG);

	private final Map<String, AttributeGroupHeader> groupHeaders;
	private final Map<String, AttributeGroupContainer> groupContainers;
	private final Map<String, AttributeComposite> attributeComposites;
	private final AttributeLayoutManager attributeLayoutManager;
	private final IScrollComposite attributeScroller;

	private final ListLayout attributeScrollerLayout;

	private final AllAttributesComposite allAttributesComposite;

	private boolean isProgrammaticUpdate;

	BeanTableAttributeListImpl(final IContainer container, final IBeanTableModel<?> model) {
		super(container);
		this.attributeLayoutManager = new AttributeLayoutManager(10, 10, 4);
		this.groupHeaders = new HashMap<String, AttributeGroupHeader>();
		this.groupContainers = new HashMap<String, AttributeGroupContainer>();
		this.attributeComposites = new HashMap<String, AttributeComposite>();
		this.setLayout(new MigLayoutDescriptor("[grow]", "[]0[]0[grow, 0:600:]"));

		final IBluePrintFactory bpF = Toolkit.getBluePrintFactory();

		new AttributeHeaderComposite(add(bpF.composite(), "grow, wrap"));
		allAttributesComposite = new AllAttributesComposite(
			add(bpF.composite(), "grow, wrap"),
			attributeLayoutManager,
			model,
			new AllAttributeInformation(model));

		attributeScroller = this.add(bpF.scrollComposite().setHorizontalBar(false), "grow, w 0::, h 0::");
		attributeScrollerLayout = new ListLayout(attributeScroller, ALTERNATING_GROUPS);
		attributeScroller.setLayout(attributeScrollerLayout);

		AttributeGroupHeader lastHeader = null;
		for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
			final IAttribute<?> attribute = model.getAttribute(columnIndex);

			final String attributeGroupId;
			if (attribute.getGroup() != null) {
				attributeGroupId = attribute.getGroup().getId();
			}
			else {
				attributeGroupId = null;
			}

			AttributeGroupContainer groupContainer = groupContainers.get(attributeGroupId);
			if (groupContainer == null) {
				final AttributeGroupInformation information = new AttributeGroupInformation(columnIndex, model);
				final AttributeGroupHeader header = new AttributeGroupHeader(
					attributeScroller.add(bpF.composite()),
					attributeLayoutManager,
					model,
					information);
				groupHeaders.put(attributeGroupId, header);
				groupContainer = new AttributeGroupContainer(attributeScroller.add(bpF.composite()));
				groupContainers.put(attributeGroupId, groupContainer);

				if (lastHeader != null) {
					lastHeader.setEnabled(true);
				}
				else {
					header.setEnabled(false);
				}
				lastHeader = header;
			}

			// add item
			final AttributeComposite attributeComposite = new AttributeComposite(
				groupContainer.add(bpF.composite()),
				attributeLayoutManager,
				model,
				attribute);
			attributeComposites.put(attribute.getPropertyName(), attributeComposite);
		}

		attributeLayoutManager.modes[2] = FillMode.CENTER; // center visibility checkbox
		attributeLayoutManager.gaps[5] = 40; // add central gap
		attributeLayoutManager.gaps[7] = 7; // reduce gap between in sort order
		attributeLayoutManager.gaps[9] = 7; // reduce gap between in sort order

		if (groupHeaders.size() <= 1) {
			// hide toolbar column if no groups exist
			attributeLayoutManager.modes[0] = FillMode.HIDDEN;
			attributeLayoutManager.gaps[1] = 0; // no gap after hidden column
		}

		attributeLayoutManager.calculateLayout();
	}

	void updateValues(final Map<String, IAttributeConfig> currentConfig) {
		for (final Entry<String, AttributeComposite> entry : attributeComposites.entrySet()) {
			final AttributeComposite composite = entry.getValue();
			composite.updateValues(currentConfig);
		}
		programmaticUpdate();
	}

	void programmaticUpdate() {
		if (isProgrammaticUpdate) {
			return;
		}

		isProgrammaticUpdate = true;
		for (final Entry<String, AttributeGroupHeader> entry : groupHeaders.entrySet()) {
			final AttributeGroupHeader composite = entry.getValue();
			composite.updateValues();
		}

		allAttributesComposite.updateValues();
		isProgrammaticUpdate = false;
	}

	private final class AttributeGroupContainer extends ContainerWrapper {

		private AttributeGroupContainer(final IContainer container) {
			super(container);
			setLayout(new ListLayout(container, ALTERNATING_ATTRIBUTES));
		}

	}

	private final class AttributeHeaderComposite extends ContainerWrapper {

		public AttributeHeaderComposite(final IContainer container) {
			super(container);
			setBackgroundColor(ATTRIBUTE_GROUP_BACKGROUND);
			setLayout(new AttributeLayout(container, attributeLayoutManager));

			final IBluePrintFactory bpF = Toolkit.getBluePrintFactory();
			add(LABEL_HEADER.setText(""));
			add(LABEL_HEADER.setText("Name"));
			add(LABEL_HEADER.setText("Visible"));
			add(LABEL_HEADER.setText("Header format"));
			add(LABEL_HEADER.setText("Content format"));
			add(LABEL_HEADER.setText("Alignment"));
			add(LABEL_HEADER.setText("Current sorting"), 2);
			add(LABEL_HEADER.setText("Default sorting"), 2);
		}
	}

	private abstract class AbstractAttributeComposite<TYPE> extends ContainerWrapper {
		private final TYPE information;
		private final ICheckBox visible;
		private final IComboBox<String> headerFormat;
		private final IComboBox<String> contentFormat;
		private final IComboBox<String> columnAlignment;
		private final IComboBox<String> currentSorting;
		private final IComboBox<String> currentSortingIndex;
		private final IComboBox<String> defaultSorting;
		private final IComboBox<String> defaultSortingIndex;

		private AbstractAttributeComposite(
			final IContainer container,
			final AttributeLayoutManager attributeLayoutManager,
			final IBeanTableModel<?> model,
			final TYPE information,
			final ITextLabelBluePrint textLabel) {
			super(container);
			this.information = information;
			final IBluePrintFactory bpF = Toolkit.getBluePrintFactory();
			setLayout(new AttributeLayout(container, attributeLayoutManager));

			if (hasCollapseButton()) {
				final IToolBar toolBar = container.add(bpF.toolBar());
				toolBar.addAction(createCollapseAction());
			}
			else {
				container.add(bpF.textLabel());
			}

			add(textLabel.setText(getLabelText()));

			visible = add(bpF.checkBox());
			visible.addInputListener(createVisibleChangedListener());

			final List<String> headerFormats = getHeaderFormats();
			if (headerFormats.size() > 1) {
				headerFormat = createComboBox(bpF, headerFormats, 300);
				headerFormat.addInputListener(createHeaderChangedListener());
			}
			else {
				headerFormat = null;
				addNotAvailableLabel(textLabel);
			}

			final List<String> contentFormats = getContentFormats();
			if (contentFormats.size() > 1) {
				contentFormat = createComboBox(bpF, contentFormats, 300);
				contentFormat.addInputListener(createContentChangedListener());
			}
			else {
				contentFormat = null;
				addNotAvailableLabel(textLabel);
			}

			columnAlignment = createComboBox(bpF, getAlignments(), 200);
			columnAlignment.addInputListener(createAlignmentChangedListener());

			if (isSortable()) {
				currentSorting = createComboBox(bpF, getSortOrders(), 250);
				currentSortingIndex = add(bpF.comboBoxSelection("1"));

				defaultSorting = createComboBox(bpF, getSortOrders(), 250);
				defaultSortingIndex = add(bpF.comboBoxSelection("1"));
				// TODO NM add listeners...
			}
			else {
				currentSorting = null;
				currentSortingIndex = null;
				defaultSorting = null;
				defaultSortingIndex = null;
				for (int i = 0; i < 4; i++) {
					addNotAvailableLabel(textLabel);
				}
			}
		}

		protected TYPE getData() {
			return information;
		}

		protected abstract IInputListener createVisibleChangedListener();

		protected abstract IInputListener createHeaderChangedListener();

		protected abstract IInputListener createContentChangedListener();

		protected abstract IInputListener createAlignmentChangedListener();

		protected abstract IAction createCollapseAction();

		protected abstract boolean hasCollapseButton();

		protected abstract String getLabelText();

		protected abstract List<String> getHeaderFormats();

		protected abstract List<String> getContentFormats();

		protected abstract boolean isSortable();

		private void addNotAvailableLabel(final ITextLabelBluePrint textLabel) {
			final AlignmentHorizontal alignment = textLabel.getAlignment();
			add(textLabel.alignCenter().setText("-"));
			textLabel.setAlignment(alignment);
		}

		protected IComboBox<String> createComboBox(
			final IBluePrintFactory bpF,
			final List<String> elements,
			final Integer maxWidth) {
			final IComboBox<String> result = add(bpF.comboBoxSelection());
			result.setElements(elements);
			if (maxWidth != null) {
				result.setMaxSize(new Dimension(maxWidth.intValue(), result.getPreferredSize().getHeight()));
			}
			return result;
		}

		protected List<String> getAlignments() {
			final List<String> result = new LinkedList<String>();
			for (final AlignmentHorizontal alignment : AlignmentHorizontal.values()) {
				result.add(alignment.getLabel());
			}
			return result;
		}

		protected List<String> getSortOrders() {
			final List<String> result = new LinkedList<String>();
			for (final SortOrder sortOrder : SortOrder.values()) {
				result.add(sortOrder.getLabel());
			}
			return result;
		}

		public ICheckBox getVisible() {
			return visible;
		}

		public IComboBox<String> getHeaderFormat() {
			return headerFormat;
		}

		public IComboBox<String> getContentFormat() {
			return contentFormat;
		}

		public IComboBox<String> getColumnAlignment() {
			return columnAlignment;
		}

		public IComboBox<String> getCurrentSorting() {
			return currentSorting;
		}

		public IComboBox<String> getCurrentSortingIndex() {
			return currentSortingIndex;
		}

		public IComboBox<String> getDefaultSorting() {
			return defaultSorting;
		}

		public IComboBox<String> getDefaultSortingIndex() {
			return defaultSortingIndex;
		}

		public void updateValues(final String[] propertyNames) {
			int visibleCount = 0;
			int invisibleCount = 0;
			final Set<String> headerFormats = new HashSet<String>();
			final Set<String> contentFormats = new HashSet<String>();
			final Set<String> columnAlignments = new HashSet<String>();
			for (final String propertyName : propertyNames) {
				final AttributeComposite attributeComposite = attributeComposites.get(propertyName);
				if (attributeComposite.getVisible().getValue()) {
					visibleCount++;
				}
				else {
					invisibleCount++;
				}

				if (attributeComposite.getHeaderFormat() != null) {
					headerFormats.add(attributeComposite.getHeaderFormat().getValue());
				}
				if (attributeComposite.getContentFormat() != null) {
					contentFormats.add(attributeComposite.getContentFormat().getValue());
				}

				columnAlignments.add(attributeComposite.getColumnAlignment().getValue());
			}

			if (headerFormats.size() == 1) {
				final String format = (String) headerFormats.toArray()[0];
				setValue(getHeaderFormat(), format);
			}
			else {
				setValue(getHeaderFormat(), null);
			}

			if (contentFormats.size() == 1) {
				final String format = (String) contentFormats.toArray()[0];
				setValue(getContentFormat(), format);
			}
			else {
				setValue(getContentFormat(), null);
			}

			if (columnAlignments.size() == 1) {
				final String format = (String) columnAlignments.toArray()[0];
				setValue(getColumnAlignment(), format);
			}
			else {
				setValue(getColumnAlignment(), null);
			}

			getVisible().setValue(invisibleCount == 0);
		}

	}

	private final class AttributeComposite extends AbstractAttributeComposite<IAttribute<?>> {

		private final String propertyName;

		private AttributeComposite(
			final IContainer container,
			final AttributeLayoutManager attributeLayoutManager,
			final IBeanTableModel<?> model,
			final IAttribute<?> attribute) {
			super(container, attributeLayoutManager, model, attribute, LABEL_ATTRIBUTES);
			propertyName = attribute.getPropertyName();
		}

		private IAttribute<?> getAttribute() {
			return getData();
		}

		@Override
		protected String getLabelText() {
			return getAttribute().getLabel();
		}

		@Override
		protected List<String> getHeaderFormats() {
			final List<String> result = new LinkedList<String>();
			if (hasShortAndLongLabel(getAttribute())) {
				result.add(DisplayFormat.SHORT.getName());
				result.add(DisplayFormat.LONG.getName());
			}
			return result;
		}

		@Override
		protected List<String> getContentFormats() {
			final List<String> result = new LinkedList<String>();
			for (final IControlPanelProvider<?> provider : getAttribute().getControlPanels()) {
				result.add(provider.getDisplayFormatName());
			}
			return result;
		}

		@Override
		protected boolean isSortable() {
			return getAttribute().isSortable();
		}

		@Override
		protected boolean hasCollapseButton() {
			return false;
		}

		@Override
		protected IAction createCollapseAction() {
			return null;
		}

		public void updateValues(final Map<String, IAttributeConfig> currentConfig) {
			final IAttributeConfig attributeConfig = currentConfig.get(propertyName);
			if (attributeConfig == null) {
				// TODO NM handle this
				return;
			}

			if (getHeaderFormat() != null) {
				String format = getHeaderFormatConfig(attributeConfig);
				if ((format != null)
					&& (!getHeaderFormat().getElements().contains(format))
					&& (format.equals(DisplayFormat.DEFAULT.getName()))) {
					format = DisplayFormat.SHORT.getName();

				}
				setValue(getHeaderFormat(), format);
			}

			if (getContentFormat() != null) {
				final String format = getContentFormatConfig(attributeConfig);
				setValue(getContentFormat(), format);
			}

			getVisible().setValue(attributeConfig.isVisible());

			setValue(getColumnAlignment(), attributeConfig.getTableAlignment().getLabel());
		}

		protected String getHeaderFormatConfig(final IAttributeConfig attributeConfig) {
			// TODO NM check if there are several configurations for this attribute available
			// if (!hasShortAndLongLabel(attribute)) {
			//	continue;
			// }

			final DisplayFormat format = attributeConfig.getLabelDisplayFormat();
			if (format == null) {
				return null;
			}
			else {
				if (format.equals(DisplayFormat.DEFAULT)) {
					return DisplayFormat.SHORT.getName();
				}
				return format.getName();
			}
		}

		protected String getContentFormatConfig(final IAttributeConfig attributeConfig) {
			// TODO NM check if there are several configurations for this attribute available
			//if (!hasContentFormatSelection(attribute)) {
			//	continue;
			//}

			final String id = attributeConfig.getDisplayFormatId();
			if (id == null) {
				return null;
			}
			return getDisplayFormatNameById(id);
		}

		protected IInputListener createAttributeChangedListener() {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					programmaticUpdate();
				}
			};
		}

		@Override
		protected IInputListener createHeaderChangedListener() {
			return createAttributeChangedListener();
		}

		@Override
		protected IInputListener createContentChangedListener() {
			return createAttributeChangedListener();
		}

		@Override
		protected IInputListener createAlignmentChangedListener() {
			return createAttributeChangedListener();
		}

		@Override
		protected IInputListener createVisibleChangedListener() {
			return createAttributeChangedListener();
		}
	}

	private final class AttributeGroupHeader extends AbstractAttributeComposite<AttributeGroupInformation> {

		private final AttributeGroupInformation information;

		private AttributeGroupHeader(
			final IContainer container,
			final AttributeLayoutManager attributeLayoutManager,
			final IBeanTableModel<?> model,
			final AttributeGroupInformation information) {
			super(container, attributeLayoutManager, model, information, LABEL_GROUPS);
			setBackgroundColor(ATTRIBUTE_GROUP_BACKGROUND);
			this.information = information;
		}

		@Override
		protected String getLabelText() {
			return getData().getId();
		}

		@Override
		protected List<String> getHeaderFormats() {
			return getData().headerFormats;
		}

		@Override
		protected List<String> getContentFormats() {
			return getData().contentFormats;
		}

		@Override
		protected boolean isSortable() {
			return getData().sortable;
		}

		@Override
		protected boolean hasCollapseButton() {
			return true;
		}

		@Override
		protected IAction createCollapseAction() {
			final ActionBuilder actionBuilder = new ActionBuilder();
			actionBuilder.setIcon(IconsSmall.TABLE_SORT_DESC);
			actionBuilder.setCommand(new ICommandExecutor() {

				@Override
				public void execute(final IExecutionContext executionContext) throws Exception {
					final IToolBarButton button = (IToolBarButton) executionContext.getSource();
					getData().setCollapsed(!getData().isCollapsed());

					if (getData().isCollapsed()) {
						button.setIcon(IconsSmall.TABLE_SORT_ASC);
					}
					else {
						button.setIcon(IconsSmall.TABLE_SORT_DESC);
					}

				}

			});
			return actionBuilder.build();
		}

		public void updateValues() {
			updateValues(information.propertyNames);
		}

		@Override
		protected IInputListener createVisibleChangedListener() {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					if (isProgrammaticUpdate) {
						return;
					}
					isProgrammaticUpdate = true;
					final boolean visible = getVisible().getValue();
					for (final String property : getData().propertyNames) {
						attributeComposites.get(property).getVisible().setValue(visible);
					}
					isProgrammaticUpdate = false;
					programmaticUpdate();
				}
			};
		}

		@Override
		protected IInputListener createHeaderChangedListener() {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					if (isProgrammaticUpdate) {
						return;
					}
					isProgrammaticUpdate = true;
					final String value = getHeaderFormat().getValue();
					for (final String property : getData().propertyNames) {
						final IComboBox<String> header = attributeComposites.get(property).getHeaderFormat();
						setValue(header, value);
					}
					isProgrammaticUpdate = false;
					programmaticUpdate();
				}
			};
		}

		@Override
		protected IInputListener createContentChangedListener() {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					if (isProgrammaticUpdate) {
						return;
					}
					isProgrammaticUpdate = true;
					final String value = getContentFormat().getValue();
					for (final String property : getData().propertyNames) {
						final IComboBox<String> content = attributeComposites.get(property).getContentFormat();
						setValue(content, value);
					}
					isProgrammaticUpdate = false;
					programmaticUpdate();
				}
			};
		}

		@Override
		protected IInputListener createAlignmentChangedListener() {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					if (isProgrammaticUpdate) {
						return;
					}

					isProgrammaticUpdate = true;
					final String value = getColumnAlignment().getValue();
					for (final String property : getData().propertyNames) {
						final IComboBox<String> alignment = attributeComposites.get(property).getColumnAlignment();
						setValue(alignment, value);
					}
					isProgrammaticUpdate = false;
					programmaticUpdate();
				}
			};
		}

	}

	private final class AllAttributesComposite extends AbstractAttributeComposite<AllAttributeInformation> {

		private AllAttributesComposite(
			final IContainer container,
			final AttributeLayoutManager attributeLayoutManager,
			final IBeanTableModel<?> model,
			final AllAttributeInformation information) {
			super(container, attributeLayoutManager, model, information, LABEL_ALL);
			setBackgroundColor(Colors.WHITE);
		}

		@Override
		protected String getLabelText() {
			// TODO i18n
			return "All";
		}

		@Override
		protected List<String> getHeaderFormats() {
			return getData().headerFormats;
		}

		@Override
		protected List<String> getContentFormats() {
			return getData().contentFormats;
		}

		@Override
		protected boolean isSortable() {
			return getData().sortable;
		}

		@Override
		protected boolean hasCollapseButton() {
			return false;
		}

		@Override
		protected IAction createCollapseAction() {
			return null;
		}

		public void updateValues() {
			updateValues(getData().propertyNames);
		}

		@Override
		protected IInputListener createVisibleChangedListener() {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					if (!isProgrammaticUpdate) {
						isProgrammaticUpdate = true;
						final boolean visible = getVisible().getValue();
						for (final String property : getData().propertyNames) {
							attributeComposites.get(property).getVisible().setValue(visible);
						}
						isProgrammaticUpdate = false;
						programmaticUpdate();
					}
				}
			};
		}

		@Override
		protected IInputListener createHeaderChangedListener() {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					if (isProgrammaticUpdate) {
						return;
					}

					isProgrammaticUpdate = true;
					final String value = getHeaderFormat().getValue();
					for (final String property : getData().propertyNames) {
						final IComboBox<String> header = attributeComposites.get(property).getHeaderFormat();
						setValue(header, value);
					}
					isProgrammaticUpdate = false;
					programmaticUpdate();
				}
			};
		}

		@Override
		protected IInputListener createContentChangedListener() {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					if (isProgrammaticUpdate) {
						return;
					}

					isProgrammaticUpdate = true;
					final String value = getContentFormat().getValue();
					for (final String property : getData().propertyNames) {
						final IComboBox<String> content = attributeComposites.get(property).getContentFormat();
						setValue(content, value);
					}
					isProgrammaticUpdate = false;
					programmaticUpdate();
				}
			};
		}

		@Override
		protected IInputListener createAlignmentChangedListener() {
			return new IInputListener() {

				@Override
				public void inputChanged() {
					if (isProgrammaticUpdate) {
						return;
					}
					isProgrammaticUpdate = true;
					final String value = getColumnAlignment().getValue();
					for (final String property : getData().propertyNames) {
						final IComboBox<String> alignment = attributeComposites.get(property).getColumnAlignment();
						setValue(alignment, value);
					}
					isProgrammaticUpdate = false;
					programmaticUpdate();
				}
			};
		}

	}

	private enum FillMode {
		FILL,
		CENTER,
		HIDDEN
	}

	private final class AttributeLayoutManager {
		private final int columnCount;
		private final List<AttributeLayout> layouters;
		private final int[] widths;
		private final int[] gaps;
		private final FillMode[] modes;
		private final int verticalGap;

		private Dimension preferredSize;

		private int layoutHashCode;

		private AttributeLayoutManager(final int columnCount, final int defaultGap, final int verticalGap) {
			this.columnCount = columnCount;
			this.layouters = new LinkedList<AttributeLayout>();
			this.verticalGap = verticalGap;

			this.widths = new int[columnCount];
			this.gaps = new int[columnCount + 1];
			for (int i = 0; i < gaps.length; i++) {
				gaps[i] = defaultGap;
			}

			this.modes = new FillMode[columnCount];
			for (int i = 0; i < modes.length; i++) {
				modes[i] = FillMode.FILL;
			}

			this.layoutHashCode = 0;
		}

		public void setGroupVisible(final String id, final boolean visible) {
			groupContainers.get(id).setEnabled(visible);
			invalidate();
		}

		private void invalidate() {
			attributeScrollerLayout.invalidate();
			attributeScrollerLayout.layout();
			attributeScroller.layoutBegin();
			attributeScroller.layoutEnd();
		}

		private void calculateLayout() {
			for (int i = 0; i < widths.length; i++) {
				widths[i] = 0;
			}

			int minHeight = 0;
			for (final AttributeLayout layouter : layouters) {
				for (int i = 0; i < columnCount; i++) {
					if (modes[i] == FillMode.HIDDEN) {
						continue;
					}

					final Dimension size = layouter.getChildSize(i);
					widths[i] = Math.max(widths[i], size.getWidth());
					minHeight = Math.max(minHeight, size.getHeight());
				}
			}

			int minWidth = 0;
			for (final int width : widths) {
				minWidth = minWidth + width;
			}
			for (final int gap : gaps) {
				minWidth = minWidth + gap;
			}
			preferredSize = new Dimension(minWidth, minHeight + 2 * verticalGap);

			layoutHashCode = 17;
			for (int i = 0; i < widths.length; i++) {
				layoutHashCode = 31 * layoutHashCode + widths[i];
			}
			for (int i = 0; i < gaps.length; i++) {
				layoutHashCode = 31 * layoutHashCode + gaps[i];
			}
		}

		public Dimension getPreferredSize() {
			if (preferredSize == null) {
				calculateLayout();
			}
			return preferredSize;
		}
	}

	private static final class AttributeLayout implements ILayouter {
		private final IContainer container;
		private int layoutHashCode;
		private final AttributeLayoutManager attributeLayoutManager;

		private final HashMap<IControl, Dimension> preferredSizes;

		private AttributeLayout(final IContainer container, final AttributeLayoutManager attributeLayoutManager) {
			this.container = container;
			this.preferredSizes = new HashMap<IControl, Dimension>();
			this.layoutHashCode = 0;
			this.attributeLayoutManager = attributeLayoutManager;
			attributeLayoutManager.layouters.add(this);
		}

		@Override
		public void layout() {
			if (layoutHashCode == attributeLayoutManager.layoutHashCode) {
				return;
			}
			layoutHashCode = attributeLayoutManager.layoutHashCode;

			final Rectangle clientArea = container.getClientArea();
			int x = attributeLayoutManager.gaps[0];
			int index = 0;
			for (final IControl control : container.getChildren()) {
				final int span = getSpan(control);
				final int width = getSpanWidth(index, span);

				final Dimension size = getPreferredSize(control);
				final int y = clientArea.getY() + (clientArea.getHeight() - size.getHeight()) / 2;

				final int controlWidth;
				if (attributeLayoutManager.modes[index] == FillMode.FILL) {
					controlWidth = width;
				}
				else if (attributeLayoutManager.modes[index] == FillMode.CENTER) {
					controlWidth = size.getWidth();
				}
				else if (attributeLayoutManager.modes[index] == FillMode.HIDDEN) {
					controlWidth = 0;
				}
				else {
					throw new IllegalStateException("Unkown fill mode");
				}
				control.setSize(controlWidth, size.getHeight());
				control.setPosition(x + (width - controlWidth) / 2, y);

				index = index + span;
				x = x + width + attributeLayoutManager.gaps[index];
			}
		}

		@Override
		public Dimension getMinSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getPreferredSize() {
			return attributeLayoutManager.getPreferredSize();
		}

		@Override
		public Dimension getMaxSize() {
			return getPreferredSize();
		}

		@Override
		public void invalidate() {}

		public Dimension getChildSize(final int index) {
			int currentIndex = 0;
			for (final IControl control : container.getChildren()) {
				if (index == currentIndex) {
					return getPreferredSize(control);
				}

				currentIndex = currentIndex + getSpan(control);
			}
			return new Dimension(0, 0);
		}

		private int getSpan(final IControl control) {
			if ((control.getLayoutConstraints() == null) || (!(control.getLayoutConstraints() instanceof Integer))) {
				return 1;
			}

			return (Integer) control.getLayoutConstraints();
		}

		private int getSpanWidth(final int index, final int span) {
			int result = attributeLayoutManager.widths[index];
			for (int i = 1; i < span; i++) {
				result = result + attributeLayoutManager.gaps[index + i] + attributeLayoutManager.widths[index + i];
			}
			return result;
		}

		private Dimension getPreferredSize(final IControl control) {
			if (!preferredSizes.containsKey(control)) {
				final Dimension size = control.getPreferredSize();
				if (size.getHeight() > 0) {
					preferredSizes.put(control, size);
				}
				return size;
			}
			return preferredSizes.get(control);
		}

	}

	private static final class ListLayout implements ILayouter {

		private final IContainer container;
		private final HashMap<IControl, Dimension> preferredSizes;
		private Dimension preferredSize;
		private final IColorConstant[] colors;

		private ListLayout(final IContainer container, final IColorConstant[] colors) {
			this.container = container;
			this.colors = colors;
			this.preferredSizes = new HashMap<IControl, Dimension>();
		}

		@Override
		public void layout() {
			final Rectangle clientArea = container.getClientArea();
			final int x = clientArea.getX();
			int y = clientArea.getY();
			final int width = clientArea.getWidth();

			int groupIndex = 0;

			for (final IControl control : container.getChildren()) {
				final boolean controlVisible = control.isVisible();
				if (!control.isEnabled()) {
					if (controlVisible) {
						control.setVisible(false);
					}
					continue;
				}

				if (!controlVisible) {
					control.setVisible(true);
				}

				final Dimension size = getPreferredSize(control);

				control.setPosition(x, y);
				control.setSize(width, size.getHeight());

				if (colors.length > 0) {
					control.setBackgroundColor(getAttributeColor(groupIndex));
					groupIndex++;
				}

				y = y + size.getHeight();

			}
		}

		private IColorConstant getAttributeColor(final int index) {
			return colors[index % colors.length];
		}

		@Override
		public Dimension getMinSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getPreferredSize() {
			if (preferredSize == null) {
				calculateSizes();
			}
			return preferredSize;
		}

		@Override
		public Dimension getMaxSize() {
			return getPreferredSize();
		}

		@Override
		public void invalidate() {
			preferredSizes.clear();
			preferredSize = null;
		}

		private void calculateSizes() {
			//final Rectangle clientArea = container.getClientArea();
			int preferredWidth = 0;
			int preferredHeight = 0;
			for (final IControl control : container.getChildren()) {
				final boolean controlVisible = control.isVisible();
				if (!control.isEnabled()) {
					if (controlVisible) {
						control.setVisible(false);
					}
					continue;
				}

				if (!controlVisible) {
					control.setVisible(true);
				}

				final Dimension size = getPreferredSize(control);
				preferredWidth = Math.max(preferredWidth, size.getWidth());
				preferredHeight = preferredHeight + size.getHeight();
			}
			preferredSize = container.computeDecoratedSize(new Dimension(preferredWidth, preferredHeight));
		}

		private Dimension getPreferredSize(final IControl control) {
			if (!preferredSizes.containsKey(control)) {
				final Dimension size = control.getPreferredSize();
				if (size.getHeight() > 0) {
					preferredSizes.put(control, size);
				}
				return size;
			}
			return preferredSizes.get(control);
		}

	}

	private enum Visibility {
		NONE,
		PARTIAL,
		ALL
	}

	private final class AllAttributeInformation {
		private final boolean sortable;
		private final String[] propertyNames;
		private final List<String> headerFormats;
		private final List<String> contentFormats;

		private AllAttributeInformation(final IBeanTableModel<?> model) {
			this.propertyNames = new String[model.getColumnCount()];
			this.headerFormats = new LinkedList<String>();
			this.contentFormats = new LinkedList<String>();

			int sortableCount = 0;
			for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
				final IAttribute<?> attribute = model.getAttribute(columnIndex);
				propertyNames[columnIndex] = attribute.getPropertyName();
				if (attribute.isSortable()) {
					sortableCount++;
				}

				for (final IControlPanelProvider<?> provider : attribute.getControlPanels()) {
					if (contentFormats.contains(provider.getDisplayFormatName())) {
						continue;
					}

					contentFormats.add(provider.getDisplayFormatName());
				}
			}

			// TODO NM
			headerFormats.add(DisplayFormat.SHORT.getName());
			headerFormats.add(DisplayFormat.LONG.getName());

			sortable = sortableCount > 0;
		}
	}

	private final class AttributeGroupInformation {
		private final IAttributeGroup group;
		private final int startIndex;
		private final int endIndex;
		private final boolean sortable;
		private final String[] propertyNames;
		private final List<String> headerFormats;
		private final List<String> contentFormats;
		private boolean collapsed;

		private AttributeGroupInformation(final int startIndex, final IBeanTableModel<?> model) {
			this.group = model.getAttribute(startIndex).getGroup();
			this.startIndex = startIndex;
			this.endIndex = getGroupEnd(model, startIndex);
			this.propertyNames = new String[endIndex - startIndex + 1];
			this.headerFormats = new LinkedList<String>();
			this.contentFormats = new LinkedList<String>();

			int sortableCount = 0;
			int propIndex = 0;
			boolean hasLongLabel = false;
			for (int index = startIndex; index <= endIndex; index++) {
				final IAttribute<?> attribute = model.getAttribute(index);
				propertyNames[propIndex] = attribute.getPropertyName();

				if (attribute.isSortable()) {
					sortableCount++;
				}

				if ((!hasLongLabel) && (hasShortAndLongLabel(attribute))) {
					hasLongLabel = true;
				}

				final List<?> controlPanels = attribute.getControlPanels();
				for (final Object provider : controlPanels) {
					final IControlPanelProvider<?> controlPanelProvider = (IControlPanelProvider<?>) provider;

					if (!contentFormats.contains(controlPanelProvider.getDisplayFormatName())) {
						contentFormats.add(controlPanelProvider.getDisplayFormatName());
					}
				}

				propIndex++;
			}

			if (hasLongLabel) {
				headerFormats.add(DisplayFormat.SHORT.getName());
				headerFormats.add(DisplayFormat.LONG.getName());
			}

			sortable = (sortableCount > 0);
		}

		private String getId() {
			if (group == null) {
				return null;
			}
			return group.getId();
		}

		private boolean isCollapsed() {
			return collapsed;
		}

		private void setCollapsed(final boolean collapsed) {
			this.collapsed = collapsed;
			attributeLayoutManager.setGroupVisible(getId(), !collapsed);
		}

	}

	private static int getGroupEnd(final IBeanTableModel<?> model, final int index) {
		final IAttributeGroup group = model.getAttribute(index).getGroup();
		int currentIndex = index + 1;
		while (currentIndex < model.getColumnCount()) {
			final IAttributeGroup currentGroup = model.getAttribute(currentIndex).getGroup();
			if ((group != null) && (currentGroup != null)) {
				if ((group.getId() != null) && (currentGroup.getId() != null)) {
					if (!group.getId().equals(currentGroup.getId())) {
						break;
					}
				}
			}

			currentIndex++;
		}
		return currentIndex - 1;
	}

	private static boolean hasShortAndLongLabel(final IAttribute<?> attribute) {
		return ((attribute.getLabelLong() != null) && (!attribute.getLabelLong().equals(attribute.getLabel())));
	}

	private static boolean hasContentFormatSelection(final IAttribute<?> attribute) {
		return attribute.getControlPanels().size() > 1;
	}

	private static String getDisplayFormatNameById(final String id) {
		for (final DisplayFormat format : DisplayFormat.values()) {
			if (format.getId().equals(id)) {
				return format.getName();
			}
		}
		return null;
	}

	private static void setValue(final IComboBox<String> comboBox, final String value) {
		if (comboBox == null) {
			return;
		}
		if ((value != null) && (!comboBox.getElements().contains(value))) {
			return;
		}
		if (NullCompatibleEquivalence.equals(value, comboBox.getValue())) {
			return;
		}
		comboBox.setValue(value);
	}
}
