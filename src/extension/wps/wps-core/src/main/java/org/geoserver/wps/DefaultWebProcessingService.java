/* (c) 2014 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.wps;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.opengis.wps10.DescribeProcessType;
import net.opengis.wps10.ExecuteResponseType;
import net.opengis.wps10.ExecuteType;
import net.opengis.wps10.GetCapabilitiesType;
import net.opengis.wps10.ProcessDescriptionsType;
import net.opengis.wps10.WPSCapabilitiesType;

import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.platform.resource.Resource;
import org.geoserver.wps.executor.ProcessStatusTracker;
import org.geoserver.wps.executor.WPSExecutionManager;
import org.geoserver.wps.resource.WPSResourceManager;
import org.geotools.util.logging.Logging;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Default Web Processing Service class
 * 
 * @author Lucas Reed, Refractions Research Inc
 */
public class DefaultWebProcessingService implements WebProcessingService, ApplicationContextAware {
    private static final Logger LOGGER = Logging.getLogger(DefaultWebProcessingService.class);

    protected WPSInfo wps;

    protected GeoServerInfo gs;

    protected ApplicationContext context;

    protected WPSExecutionManager executionManager;

    protected WPSResourceManager resources;

    private ProcessStatusTracker tracker;

    public DefaultWebProcessingService(GeoServer gs, WPSExecutionManager executionManager,
            WPSResourceManager resources, ProcessStatusTracker tracker) {
        this.wps = gs.getService(WPSInfo.class);
        this.gs = gs.getGlobal();
        this.executionManager = executionManager;
        this.resources = resources;
        this.tracker = tracker;
    }

    /**
     * @see WebMapService#getServiceInfo()
     */
    public WPSInfo getServiceInfo() {
        return wps;
    }

    /**
     * @see org.geoserver.wps.WebProcessingService#getCapabilities
     */
    public WPSCapabilitiesType getCapabilities(GetCapabilitiesType request) throws WPSException {
        return new GetCapabilities(this.wps, context).run(request);
    }

    /**
     * @see org.geoserver.wps.WebProcessingService#describeProcess
     */
    public ProcessDescriptionsType describeProcess(DescribeProcessType request) throws WPSException {
        return new DescribeProcess(this.wps, context).run(request);
    }

    /**
     * @see org.geoserver.wps.WebProcessingService#execute
     */
    public ExecuteResponseType execute(ExecuteType request) throws WPSException {
        return new Execute(executionManager, context).run(request);
    }
    
    /**
     * @see org.geoserver.wps.WebProcessingService#getSchema
     */
    public void getSchema(HttpServletRequest request, HttpServletResponse response)
            throws WPSException {
        new GetSchema(this.wps).run(request, response);
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext
     */
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public Object getExecutionStatus(GetExecutionStatusType request) throws WPSException {
        return new GetStatus(tracker, resources, context).run(request);
    }
    
    @Override
    public Resource getExecutionResult(GetExecutionResultType request) throws WPSException {
        return new GetResult(resources).run(request);
    }

    public ExecuteResponseType dismiss(DismissType request) throws WPSException {
        return new Dismiss(executionManager, tracker, resources, context).run(request);
    }
}
