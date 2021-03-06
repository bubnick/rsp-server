/******************************************************************************* 
 * Copyright (c) 2012-2019 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.rsp.server.wildfly.servertype.capabilities;

import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.IDefaultLaunchArguments;
import org.jboss.tools.rsp.server.wildfly.servertype.launch.JBossEAP60LaunchArgs;

public class JBossEAP60ExtendedProperties extends JBossAS710ExtendedProperties {

	public JBossEAP60ExtendedProperties(IServer obj) {
		super(obj);
	}

	@Override
	public String getRuntimeTypeVersionString() {
		return "6.0"; //$NON-NLS-1$
	}

	@Override
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		return new JBossEAP60LaunchArgs(server);
	}

	@Override
	public boolean requiresJDK() {
		return true;
	}
	
	@Override
	public boolean allowExplodedDeploymentsInEars() {
		return allowExplodedDeploymentsInWarLibs();
	}

	@Override
	public boolean allowExplodedDeploymentsInWarLibs() {
		String version = getServerBeanLoader().getFullServerVersion();
		if (version == null)
			return false;
		else if (version.startsWith("6.0.0")) //$NON-NLS-1$
			return false; // 6.0.0 contains AS 7.1.2 which is bugged, 6.0.1 contains AS 7.1.3 which is fixed
		else
			return true;
	}

	
	@Override
	public String getMinimumJavaVersionString() {
		return "1.6.";
	}
	@Override
	public String getMaximumJavaVersionString() {
		return "1.8.";
	}
	
}
