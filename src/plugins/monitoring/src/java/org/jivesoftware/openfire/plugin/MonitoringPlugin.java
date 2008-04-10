/**
 * $Revision: 3034 $
 * $Date: 2005-11-04 21:02:33 -0300 (Fri, 04 Nov 2005) $
 *
 * Copyright (C) 2008 Jive Software. All rights reserved.
 *
 * This software is published under the terms of the GNU Public License (GPL),
 * a copy of which is included in this distribution, or a commercial license
 * agreement with Jive.
 */

package org.jivesoftware.openfire.plugin;

import org.jivesoftware.openfire.archive.*;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.reporting.graph.GraphEngine;
import org.jivesoftware.openfire.reporting.stats.*;
import org.jivesoftware.openfire.reporting.util.TaskEngine;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.JiveProperties;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

import java.io.File;

/**
 * Openfire Monitoring plugin.
 *
 * @author Matt Tucker
 * @src.include false
 */
public class MonitoringPlugin implements Plugin {

    private MutablePicoContainer picoContainer;

    private boolean shuttingDown = false;

    public MonitoringPlugin() {

        // Enable AWT headless mode so that stats will work in headless environments.
        System.setProperty("java.awt.headless", "true");

        picoContainer = new DefaultPicoContainer();
        picoContainer.registerComponentInstance(TaskEngine.getInstance());
        picoContainer.registerComponentInstance(JiveProperties.getInstance());

        // Stats and Graphing classes
        picoContainer.registerComponentImplementation(StatsEngine.class);
        picoContainer.registerComponentImplementation(GraphEngine.class);
        picoContainer.registerComponentImplementation(StatisticsModule.class);
        picoContainer.registerComponentImplementation(StatsViewer.class,
            getStatsViewerImplementation());

        // Archive classes
        picoContainer.registerComponentImplementation(ConversationManager.class);
        picoContainer.registerComponentImplementation(ArchiveInterceptor.class);
        picoContainer.registerComponentImplementation(GroupConversationInterceptor.class);
        picoContainer.registerComponentImplementation(ArchiveSearcher.class);
        picoContainer.registerComponentImplementation(ArchiveIndexer.class);
    }

    private Class<? extends StatsViewer> getStatsViewerImplementation() {
        if (JiveGlobals.getBooleanProperty("stats.mock.viewer", false)) {
            return MockStatsViewer.class;
        }
        else {
            return DefaultStatsViewer.class;
        }
    }

    /**
     * Returns the instance of a module registered with the Monitoring plugin.
     *
     * @param clazz the module class.
     * @return the instance of the module.
     */
    public Object getModule(Class clazz) {
        return picoContainer.getComponentInstanceOfType(clazz);
    }

    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        System.out.println("Starting Monitoring Plugin");

        shuttingDown = false;

        // Make sure that the enteprise folder exists under the home directory
        File dir = new File(JiveGlobals.getHomeDirectory() +
            File.separator + "monitoring");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        picoContainer.start();
    }

    public void destroyPlugin() {
        shuttingDown = true;

        if (picoContainer != null) {
            picoContainer.stop();
            picoContainer.dispose();
            picoContainer = null;
        }
    }

    public boolean isShuttingDown() {
        return shuttingDown;
    }
}