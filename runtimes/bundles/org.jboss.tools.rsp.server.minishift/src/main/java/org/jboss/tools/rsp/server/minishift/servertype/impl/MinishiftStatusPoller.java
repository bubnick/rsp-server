/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.foundation.core.launchers.CommandTimeoutException;
import org.jboss.tools.rsp.foundation.core.launchers.ProcessUtility;
import org.jboss.tools.rsp.server.minishift.impl.Activator;
import org.jboss.tools.rsp.server.minishift.servertype.IMinishiftServerAttributes;
import org.jboss.tools.rsp.server.minishift.servertype.MinishiftPropertyUtility;
import org.jboss.tools.rsp.server.spi.model.polling.AbstractPoller;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class MinishiftStatusPoller extends AbstractPoller implements IServerStatePoller {
	
	@Override
	protected String getThreadName() {
		return "Minishift Poller: " + getServer().getName();
	}
	public String getMinishiftCommand(IServer server) {
		return server.getAttribute(IMinishiftServerAttributes.MINISHIFT_BINARY, (String) null);
	}
	public String getWorkingDirectory(IServer server) {
		return new Path(getMinishiftCommand(server)).removeLastSegments(1).toOSString();
	}
	
	private String[] callMinishiftStatus(IServer server) throws CommandTimeoutException, IOException {
		List<String> args1 = new ArrayList<>();
		args1.add("status");
		String profile = MinishiftPropertyUtility.getMinishiftProfile(server);
		args1.add("--profile");
		args1.add(profile);
		
		String[] args = (String[]) args1.toArray(new String[args1.size()]);
		String cmd = getMinishiftCommand(server);
		ProcessUtility util = new ProcessUtility();
		String[] lines = util.callMachineReadable(
				cmd, args, getWorkingDirectory(server), 
				new EnvironmentUtility(server).getEnvironment(true, true));
		return lines;
	}
	@Override
	protected SERVER_STATE onePing(IServer server) {
		String[] lines = null;
		try {
			lines = callMinishiftStatus(server);
			IStatus stat = parseOutput(lines);
			if (stat.isOK()) {
				//checkOpenShiftHealth(server, 4000); TODO 
				return SERVER_STATE.UP;
			}
			return SERVER_STATE.DOWN;
//		} catch (PollingException pe) {
//			cancel(IServerStatePoller.CANCELATION_CAUSE.FAILED);
		} catch (TimeoutException te) {
			cancel(IServerStatePoller.CANCELATION_CAUSE.TIMEOUT_REACHED);
			return SERVER_STATE.DOWN;
		} catch (IOException ioe) {
			cancel(IServerStatePoller.CANCELATION_CAUSE.FAILED);
			return SERVER_STATE.DOWN;
//		} catch(PollingException pe) {  // TODO
//			cancel(IServerStatePoller.CANCELATION_CAUSE.FAILED);
//			return SERVER_STATE.DOWN;
		}
//	return CDKCoreActivator.statusFactory().infoStatus(CDKCoreActivator.PLUGIN_ID,
//			"Response status indicates the CDK is starting.");
//
	}

	private void handleTimeoutError(CommandTimeoutException vte) throws CommandTimeoutException {
		// Try to salvage it, it could be the process never terminated but
		// it got all the output
//		List<String> inLines = vte.getInLines();
//		if (inLines != null) {
//			String[] asArr = (String[]) inLines.toArray(new String[inLines.size()]);
//			IStatus stat = parseOutput(asArr);
//			if (stat.isOK()) {
//				checkOpenShiftHealth(server, 4000);
//			} else {
//				return stat;
//			}
//		}
//		CDKCoreActivator.pluginLog().logError("Unable to successfully complete a call to vagrant status. ", vte);
		throw vte;
	}
	
	protected IStatus parseOutput(String[] lines) {
		for (int i = 0; i < lines.length; i++) {
			if( "Does Not Exist".equals(lines[i])) {
					return new Status(IStatus.ERROR, Activator.BUNDLE_ID, 
							"Minishift profile does not exist.");
			}
			if (lines[i] != null && lines[i].startsWith("Minishift:")) {
				String stat = lines[i].substring("Minishift:".length()).trim();
				if (stat.trim().startsWith("Running")){
					return Status.OK_STATUS;
				}
				if ("Stopped".equals(stat)) {
					return new Status(IStatus.ERROR, Activator.BUNDLE_ID, 
							"Minishift is stopped.");
				}
			}
		}
		return new Status(IStatus.INFO, Activator.BUNDLE_ID, 
				"Minishift status indicates the CDK is starting.");
	}
	
	/*
	 * TODO 
	 * These methods are added in preparation of a proper tycho build which
	 * can depend on, and bundle, the java rest client. Until then, 
	 * we cannot properly check the openshift health after a startup
	 */
//
//	private boolean checkOpenShiftHealth(IServer server, int timeout) throws PollingException {
//		ServiceManagerEnvironment adb = null;
//		//ServiceManagerEnvironmentLoader.type(server).getOrLoadServiceManagerEnvironment(server, true, true);
//
//		if (adb == null) {
//			return false;
//		}
//		String url = adb.getOpenShiftHost() + ":" + adb.getOpenShiftPort();
//		return checkOpenShiftHealth(url, timeout);
//	}
//
//	protected boolean checkOpenShiftHealth(String url, int timeout) throws PollingException {
//		ISSLCertificateCallback sslCallback = new LazySSLCertificateCallback();
//		IClient client = new ClientBuilder(url).sslCertificateCallback(sslCallback)
//				.withConnectTimeout(timeout, TimeUnit.MILLISECONDS).build();
//
//		Exception e = null;
//		try {
//			String v = client.getServerReadyStatus();
//			if ("ok".equals(v))
//				return true;
//		} catch (OpenShiftException ex) {
//			e = ex;
//		}
//
//		String msg = NLS.bind(
//				"The CDK VM is up and running, but OpenShift is unreachable at url {0}. "
//						+ "The VM may not have been registered successfully. Please check your console output for more information",
//				url);
//		throw new OpenShiftNotReadyPollingException(
//				new Status(IStatus.ERROR, Activator.BUNDLE_ID,
//						OpenShiftNotReadyPollingException.OPENSHIFT_UNREACHABLE_CODE,
//						msg, e));
//	}

}
