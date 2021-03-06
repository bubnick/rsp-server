/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.tools.rsp.api.RSPClient;
import org.jboss.tools.rsp.api.dao.DiscoveryPath;
import org.jboss.tools.rsp.api.dao.JobHandle;
import org.jboss.tools.rsp.api.dao.JobProgress;
import org.jboss.tools.rsp.api.dao.JobRemoved;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerProcess;
import org.jboss.tools.rsp.api.dao.ServerProcessOutput;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.VMDescription;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallChangedListener;
import org.jboss.tools.rsp.eclipse.jdt.launching.PropertyChangeEvent;
import org.jboss.tools.rsp.server.ServerManagementServerImpl;
import org.jboss.tools.rsp.server.spi.discovery.IDiscoveryPathListener;
import org.jboss.tools.rsp.server.spi.jobs.IJob;
import org.jboss.tools.rsp.server.spi.jobs.IJobListener;
import org.jboss.tools.rsp.server.spi.model.IServerModel;
import org.jboss.tools.rsp.server.spi.model.IServerModelListener;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public class RemoteEventManager implements IDiscoveryPathListener, 
	IVMInstallChangedListener, IServerModelListener, IJobListener {
	
	private ServerManagementServerImpl server;
	public RemoteEventManager(ServerManagementServerImpl serverManagementServerImpl) {
		this.server = serverManagementServerImpl; 
		serverManagementServerImpl.getModel().getDiscoveryPathModel().addListener(this);
		//serverManagementServerImpl.getModel().getVMInstallModel().addListener(this);
		serverManagementServerImpl.getModel().getServerModel().addServerModelListener(this);
		serverManagementServerImpl.getModel().getJobManager().addJobListener(this);
	}
	
	@Override
	public void discoveryPathAdded(DiscoveryPath path) {
		List<RSPClient> l = server.getClients();
		for( RSPClient c : l) {
			c.discoveryPathAdded(path);
		}
	}
	@Override
	public void discoveryPathRemoved(DiscoveryPath path) {
		List<RSPClient> l = server.getClients();
		for( RSPClient c : l) {
			c.discoveryPathRemoved(path);
		}
	}

	public void serverAdded(ServerHandle server2) {
		List<RSPClient> l = server.getClients();
		for( RSPClient c : l) {
			c.serverAdded(server2);
		}
	}
	
	public void serverRemoved(ServerHandle server2) {
		List<RSPClient> l = server.getClients();
		for( RSPClient c : l) {
			c.serverRemoved(server2);
		}
	}
	
	public void serverAttributesChanged(ServerHandle server) {
		// TODO 
	}
	
	public void serverStateChanged(ServerHandle server, ServerState state) {
		List<RSPClient> l = this.server.getClients();
		if( this.server.getModel().getServerModel().getServer(server.getId()) != null ) {
			for( RSPClient c : l) {
				c.serverStateChanged(state);
			}
		}
	}
	
	/*
	 * Initialize a new client with all server states
	 */
	public void initClientWithServerStates(RSPClient client) {
		IServerModel model = server.getModel().getServerModel();
		List<IServer> all = new ArrayList<>(model.getServers().values());
		ServerState state = null;
		for( Iterator<IServer> it = all.iterator(); it.hasNext(); ) {
			state = it.next().getDelegate().getServerState();
			client.serverStateChanged(state);
		}
	}
	
	public void serverProcessCreated(ServerHandle server, String processId) {
		List<RSPClient> l = this.server.getClients();
		for( RSPClient c : l) {
			c.serverProcessCreated(new ServerProcess(server, processId));
		}
	}
	
	public void serverProcessTerminated(ServerHandle server, String processId) {
		List<RSPClient> l = this.server.getClients();
		for( RSPClient c : l) {
			c.serverProcessTerminated(new ServerProcess(server, processId));
		}
	}
	
	public void serverProcessOutputAppended(ServerHandle server, String processId, int streamType, String text) {
		List<RSPClient> l = this.server.getClients();
		for( RSPClient c : l) {
			c.serverProcessOutputAppended(new ServerProcessOutput(
					server, processId, streamType, text));
		}
	}
	
	
	
	// To be ignored
	@Override
	public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void vmChanged(PropertyChangeEvent event) {
		// TODO Auto-generated method stub
		
	}


	private VMDescription getDescription(IVMInstall vmi) {
		String vers = vmi.getJavaVersion();
		return new VMDescription(vmi.getId(), vmi.getInstallLocation().getAbsolutePath(), vers);
	}

	
	@Override
	public void vmAdded(IVMInstall vm) {
//		List<RSPClient> l = server.getClients();
//		for( RSPClient c : l) {
//			c.vmAdded(getDescription(vm));
//		}
	}
	@Override
	public void vmRemoved(IVMInstall vm) {
//		List<RSPClient> l = server.getClients();
//		for( RSPClient c : l) {
//			c.vmRemoved(getDescription(vm));
//		}
	}
	@Override
	public void jobAdded(IJob job) {
		JobHandle jh = new JobHandle(job.getName(), job.getId());
		List<RSPClient> l = server.getClients();
		for( RSPClient c : l) {
			c.jobAdded(jh);
		}
	}
	@Override
	public void jobRemoved(IJob job, IStatus status) {
		JobHandle jh = new JobHandle(job.getName(), job.getId());
		JobRemoved rem = new JobRemoved(jh,  StatusConverter.convert(status));
		List<RSPClient> l = server.getClients();
		for( RSPClient c : l) {
			c.jobRemoved(rem);
		}
	}
	@Override
	public void progressChanged(IJob job, double work) {
		JobProgress progress = new JobProgress(new JobHandle(job.getName(), job.getId()), work); 
		List<RSPClient> l = server.getClients();
		for( RSPClient c : l) {
			c.jobChanged(progress);
		}
	}
}
