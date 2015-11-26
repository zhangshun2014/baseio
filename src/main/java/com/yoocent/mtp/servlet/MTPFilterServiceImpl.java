package com.yoocent.mtp.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yoocent.mtp.AbstractLifeCycle;
import com.yoocent.mtp.LifeCycle;
import com.yoocent.mtp.common.FileUtil;
import com.yoocent.mtp.component.FilterConfig;
import com.yoocent.mtp.component.ServletService;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;
import com.yoocent.mtp.server.context.ServletContext;

public final class MTPFilterServiceImpl extends AbstractLifeCycle implements MTPFilterService , LifeCycle{
	
	private ServletContext context = null;
	
	private ServletService service = null;
	
	private List<WrapperMTPFilter> filters = new ArrayList<WrapperMTPFilter>();

	public MTPFilterServiceImpl(ServletContext context, ServletService service) {
		this.context = context;
		this.service = service;
	}

	public boolean doFilter(Request request, Response response)throws Exception {
		for(WrapperMTPFilter filter : filters){
			boolean _break = filter.doFilter(this, request, response);
			if (_break) {
				return true;
			}
		}
		return false;
	}

	public void accept(Request request, Response response) throws Exception {
		this.service.acceptServlet(request, response);
		
	}
	
	private void loadFilters (){
		try {
			String str = FileUtil.readContentByCls("filters.config", "UTF-8");
			JSONArray jArray = JSONArray.parseArray(str);
			for (int i = 0; i < jArray.size(); i++) {
				JSONObject jObj = jArray.getJSONObject(i);
				String clazz = jObj.getString("class");
				Map<String,Object> config = toMap(jObj);
				FilterConfig filterConfig = new FilterConfig();
				filterConfig.setConfig(config);
				try {
					MTPFilter filter =(MTPFilter)Class.forName(clazz).newInstance();
					this.filters.add(new WrapperMTPFilterImpl(context, filter, filterConfig));
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static Map<String, Object> toMap(JSONObject jsonObject) {
		Map<String, Object> result = new HashMap<String, Object>();
		Set enteys = jsonObject.entrySet();
		Iterator iterator = enteys.iterator();
		while (iterator.hasNext()) {
				Entry e = (Entry) iterator.next();
				String key = (String) e.getKey();
				Object value = e.getValue();
				result.put(key, value);
		}
		return result;
	}
	protected void doStart() throws Exception {
		this.loadFilters();
		//start all filter
		for (int i = 0; i < filters.size(); i++) {
			WrapperMTPFilter filter = filters.get(i);
			try {
				filter.start();
			} catch (Exception e) {
				e.printStackTrace();
				filters.remove(i);
				i--;
			}
		}
		
	}

	protected void doStop() throws Exception {
		for(WrapperMTPFilter filter : filters){
			try {
				filter.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}