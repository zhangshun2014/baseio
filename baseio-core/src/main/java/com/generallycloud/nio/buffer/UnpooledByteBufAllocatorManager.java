/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.nio.buffer;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.ChannelContext;
import com.generallycloud.nio.configuration.ServerConfiguration;

/**
 * @author wangkai
 *
 */
public class UnpooledByteBufAllocatorManager extends AbstractLifeCycle
		implements ByteBufAllocatorManager {

	private ChannelContext			context	= null;

	private UnpooledByteBufAllocator	unpooledByteBufAllocator;

	public UnpooledByteBufAllocatorManager(ChannelContext context) {
		this.context = context;
	}

	@Override
	public ByteBufAllocator getNextBufAllocator() {
		return unpooledByteBufAllocator;
	}

	@Override
	protected void doStart() throws Exception {

		ServerConfiguration c = context.getServerConfiguration();

		boolean isDirect = c.isSERVER_ENABLE_MEMORY_POOL_DIRECT();

		unpooledByteBufAllocator = new UnpooledByteBufAllocator(isDirect);

		LifeCycleUtil.start(unpooledByteBufAllocator);
	}

	@Override
	protected void doStop() throws Exception {
		LifeCycleUtil.stop(unpooledByteBufAllocator);
	}

}
