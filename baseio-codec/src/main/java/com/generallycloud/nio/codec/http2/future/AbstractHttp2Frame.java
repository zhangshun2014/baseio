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
package com.generallycloud.nio.codec.http2.future;

import com.generallycloud.nio.codec.http2.Http2SocketSession;
import com.generallycloud.nio.protocol.AbstractChannelReadFuture;

public abstract class AbstractHttp2Frame extends AbstractChannelReadFuture implements SocketHttp2Frame {

	private Http2FrameHeader header;
	
	protected AbstractHttp2Frame(Http2SocketSession session,Http2FrameHeader header) {
		super(session.getContext());
		this.header = header;
	}

	@Override
	public Http2FrameHeader getHeader() {
		return header;
	}

}
