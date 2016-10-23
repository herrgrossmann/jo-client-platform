/*
 * Copyright (c) 2014, grossmann
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * * Neither the name of jo-widgets.org nor the
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
package org.jowidgets.cap.tools.starter.client.osgi;

import org.jowidgets.cap.tools.starter.client.remoting.MessagingInitializer;
import org.jowidgets.util.Assert;
import org.jowidgets.workbench.api.IWorkbenchFactory;
import org.jowidgets.workbench.api.IWorkbenchRunner;
import org.jowidgets.workbench.impl.WorkbenchRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public class CapClientOsgiWorkbenchRunner implements BundleActivator {

	private static final IWorkbenchRunner WORKBENCH_RUNNER = new WorkbenchRunner();

	private final MessagingInitializer messagingInitializer;
	private final IWorkbenchFactory workbench;

	/**
	 * Creates a new instance
	 * 
	 * @param workbench The workbench to run
	 */
	public CapClientOsgiWorkbenchRunner(final IWorkbenchFactory workbench) {
		this(workbench, "http://localhost:8080/");
	}

	@Deprecated
	/**
	 * Creates a new CapClientOsgiWorkbenchRunner runner.
	 * 
	 * @param workbench The workbench to run
	 * @param includePath The include path
	 * 
	 * @deprecated Do no longer use the ClassLoadingActivator, instead use the
	 *             org.jowidgets.classloading.weaving.plugin and autostart this before any
	 *             "Shared-Classloader-Contribution" consuming bundle has been started, e.g.
	 *             start level 2.
	 */
	public CapClientOsgiWorkbenchRunner(final IWorkbenchFactory workbench, final String[] includePath) {
		this(workbench, "http://localhost:8080/", includePath);
	}

	@Deprecated
	/**
	 * Creates a new CapClientOsgiWorkbenchRunner runner.
	 * 
	 * @param workbench The workbench to run
	 * @param defaultServerHost The default server host
	 * @param includePath The include path
	 * 
	 * @deprecated Do no longer use the ClassLoadingActivator, instead use the
	 *             org.jowidgets.classloading.weaving.plugin and autostart this before any
	 *             "Shared-Classloader-Contribution" consuming bundle has been started, e.g.
	 *             start level 2.
	 */
	public CapClientOsgiWorkbenchRunner(
		final IWorkbenchFactory workbench,
		final String defaultServerHost,
		final String[] includePath) {
		this(workbench, defaultServerHost);
	}

	/**
	 * Creates a new CapClientOsgiWorkbenchRunner runner.
	 * 
	 * @param workbench The workbench to run
	 * @param defaultServerHost The default server host
	 */
	public CapClientOsgiWorkbenchRunner(final IWorkbenchFactory workbench, final String defaultServerHost) {
		Assert.paramNotNull(workbench, "workbench");
		this.messagingInitializer = new MessagingInitializer(defaultServerHost);
		this.workbench = workbench;
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		final Bundle bundle = context.getBundle();
		context.addBundleListener(new BundleListener() {
			@Override
			public void bundleChanged(final BundleEvent event) {
				if (event.getBundle().equals(bundle) && BundleEvent.STARTED == event.getType()) {
					bundleStartet();
				}
			}
		});
	}

	@Override
	public void stop(final BundleContext context) throws Exception {}

	private void bundleStartet() {
		messagingInitializer.initializeMessaging();
		WORKBENCH_RUNNER.run(workbench);
		System.exit(0);
	}

}
