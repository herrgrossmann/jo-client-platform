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

package org.jowidgets.cap.sample1.starter.webapp.rwt;

import org.jowidgets.api.login.ILoginInterceptor;
import org.jowidgets.api.login.ILoginResultCallback;
import org.jowidgets.api.threads.IUiThreadAccess;
import org.jowidgets.api.toolkit.Toolkit;
import org.jowidgets.cap.ui.api.login.ILoginService;
import org.jowidgets.security.api.AuthenticationService;
import org.jowidgets.security.api.AuthorizationService;
import org.jowidgets.security.api.SecurityContextHolder;
import org.jowidgets.security.tools.DefaultCredentials;
import org.jowidgets.security.tools.DefaultPrincipal;

public class RwtLoginService implements ILoginService {

	@Override
	public boolean doLogin() {
		final IUiThreadAccess uiThreadAccess = Toolkit.getUiThreadAccess();
		final ILoginInterceptor loginInterceptor = new ILoginInterceptor() {
			@Override
			public void login(final ILoginResultCallback resultCallback, final String username, final String password) {

				DefaultPrincipal principal = AuthenticationService.authenticate(new DefaultCredentials(username, password));
				if (principal != null) {
					principal = AuthorizationService.authorize(principal);
					if (principal != null) {
						resultCallback.granted();
						uiThreadAccess.invokeLater(new Runnable() {
							@Override
							public void run() {
								SecurityContextHolder.setSecurityContext(new DefaultPrincipal(username));
								//CHECKSTYLE:OFF
								System.out.println("LOGGED ON AS: " + SecurityContextHolder.getSecurityContext());
								//CHECKSTYLE:ON
							}
						});
					}
					else {
						resultCallback.denied("User not authorized");
					}
				}
				else {
					resultCallback.denied("Login incorrect");
				}

			}
		};
		if (Toolkit.getLoginPane().login("Application1", loginInterceptor).isLoggedOn()) {
			return true;
		}
		else {
			return false;
		}
	}
}