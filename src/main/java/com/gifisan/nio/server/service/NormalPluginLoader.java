package com.gifisan.nio.server.service;

import java.util.List;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.DynamicClassLoader;
import com.gifisan.nio.component.PluginContext;
import com.gifisan.nio.server.configuration.PluginsConfiguration;

public class NormalPluginLoader extends AbstractLifeCycle implements PluginLoader {

	private ApplicationContext		context		= null;
	private DynamicClassLoader	classLoader	= null;
	private Logger				logger		= LoggerFactory.getLogger(NormalPluginLoader.class);
	private PluginContext[]		pluginContexts	= new PluginContext[4];
	private PluginsConfiguration	configuration	= null;

	public NormalPluginLoader(ApplicationContext context, DynamicClassLoader classLoader) {
		this.configuration = context.getConfiguration().getPluginsConfiguration();
		this.context = context;
		this.classLoader = classLoader;
	}

	protected void doStart() throws Exception {

		loadPlugins(context, this.classLoader, this.configuration);

		this.initializePlugins(pluginContexts);

		this.configPluginFilterAndServlet((ApplicationContext) context);
	}

	protected void doStop() throws Exception {

		for (PluginContext plugin : pluginContexts) {

			if (plugin == null) {
				continue;
			}

			try {

				plugin.destroy(context, plugin.getConfig());

				logger.info("  [NIOServer] 卸载完成 [ {} ]", plugin);

			} catch (Throwable e) {

				logger.error(e.getMessage(), e);
			}
		}
	}

	public PluginContext[] getPluginContexts() {
		return pluginContexts;
	}

	private void initializePlugins(PluginContext[] plugins) throws Exception {

		for (PluginContext plugin : plugins) {

			if (plugin == null) {
				continue;
			}

			plugin.initialize(context, plugin.getConfig());

			logger.info("  [NIOServer] 加载完成 [ {} ]", plugin);
		}
	}

	private void loadPlugins(ApplicationContext context, DynamicClassLoader classLoader, PluginsConfiguration configuration)
			throws Exception {

		List<Configuration> plugins = configuration.getPlugins();

		if (plugins.size() > 4) {
			throw new IllegalArgumentException("plugin size max to 4");
		}

		for (int i = 0; i < plugins.size(); i++) {

			Configuration config = plugins.get(i);

			String className = config.getParameter("class", "empty");

			Class<?> clazz = classLoader.forName(className);

			PluginContext plugin = (PluginContext) clazz.newInstance();

			pluginContexts[i] = plugin;

			plugin.setConfig(config);

		}
	}

	public void prepare(ApplicationContext context, Configuration config) throws Exception {

		logger.info("  [NIOServer] 尝试加载新的Servlet配置......");

		loadPlugins(context, classLoader, this.configuration);

		logger.info("  [NIOServer] 尝试启动新的Servlet配置......");

		this.prepare(context,pluginContexts);
		
		this.softStart();
		
	}

	private void prepare(ApplicationContext context,PluginContext[] plugins) throws Exception {

		for (PluginContext plugin : plugins) {

			if (plugin == null) {
				continue;
			}

			plugin.prepare(context, plugin.getConfig());

			logger.info("  [NIOServer] 新的Servlet [ {} ] Prepare完成", plugin);

		}
		
		this.configPluginFilterAndServlet((ApplicationContext) context);
	}

	public void unload(ApplicationContext context, Configuration config) throws Exception {

		for (PluginContext plugin : pluginContexts) {

			if (plugin == null) {
				continue;
			}

			try {

				plugin.unload(context, plugin.getConfig());

				logger.info("  [NIOServer] 旧的Servlet [ {} ] Unload完成", plugin);

			} catch (Throwable e) {

				logger.info("  [NIOServer] 旧的Servlet [ {} ] Unload失败", plugin);

				logger.error(e.getMessage(), e);

			}

		}
	}
	
	private void configPluginFilterAndServlet(ApplicationContext serverContext){
		
		for (PluginContext context : pluginContexts) {

			if (context == null) {
				continue;
			}

			context.configFilter(serverContext.getPluginFilters());
			context.configServlet(serverContext.getPluginServlets());
		}
	}

}