/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.services.cdi.impl.store;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;

import org.jbpm.kie.services.impl.store.DeploymentSynchronizer;

@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.WRITE)
@TransactionManagement(TransactionManagementType.BEAN)
@AccessTimeout(value=1, unit=TimeUnit.MINUTES)
public class DeploymentSynchronizerCDInvoker {
	
	private Timer timer;
	@Resource
    private TimerService timerService;
	@Inject
	private DeploymentSynchronizer deploymentSynchronizer;
	
	@PostConstruct
	public void configure() {
		if (DeploymentSynchronizer.DEPLOY_SYNC_ENABLED) {
			ScheduleExpression schedule = new ScheduleExpression();
			
			schedule.hour("*");
			schedule.minute("*");
			schedule.second("*/" + DeploymentSynchronizer.DEPLOY_SYNC_INTERVAL);
			timer = timerService.createCalendarTimer(schedule, new TimerConfig(null, false));
		
		}
	}
	
	@PreDestroy
	public void shutdown() {
		if (timer != null) {
			timer.cancel();
		}
	}

	@Timeout
	public void synchronize() {
		deploymentSynchronizer.synchronize();
	}

	
}
