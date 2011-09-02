/*
 * Copyright (c) 2011, M. Grossmann, M. Woelker, H. Westphal
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

package org.jowidgets.cap.sample1.starter.standalone.rcp;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.sample1.ui.workbench.SampleWorkbench;
import org.jowidgets.impl.toolkit.DefaultToolkitProvider;
import org.jowidgets.spi.impl.swt.SwtWidgetsServiceProvider;
import org.jowidgets.workbench.impl.rcp.WorkbenchRunner;

public class Sample1StarterStandaloneRcp implements IApplication {

	@Override
	public Object start(final IApplicationContext arg0) throws Exception {
		if (!Toolkit.isInitialized()) {
			Toolkit.initialize(new DefaultToolkitProvider(new SwtWidgetsServiceProvider(Display.getDefault())));
		}
		new WorkbenchRunner().run(new SampleWorkbench());
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}
