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
package com.generallycloud.nio.component;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.protocol.SslReadFuture;
import com.generallycloud.nio.protocol.SslReadFutureImpl;

public class SslChannelByteBufReader extends LinkableChannelByteBufReader {

	@Override
	public void accept(SocketChannel channel, ByteBuf buffer) throws Exception {
		
		UnsafeSocketSession session = channel.getSession();

		for (;;) {

			if (!buffer.hasRemaining()) {
				return;
			}

			SslReadFuture future = channel.getSslReadFuture();

			if (future == null) {

				ByteBuf buf = allocate(session,SslReadFuture.SSL_RECORD_HEADER_LENGTH);

				future = new SslReadFutureImpl(session, buf,1024 * 64);//FIXME param

				channel.setSslReadFuture(future);
			}

			try {

				if (!future.read(session, buffer)) {

					return;
				}

			} catch (Throwable e) {

				channel.setSslReadFuture(null);

				ReleaseUtil.release(future);

				if (e instanceof IOException) {
					throw (IOException) e;
				}

				throw new IOException("exception occurred when read from channel,the nested exception is,"
						+ e.getMessage(), e);
			}

			channel.setSslReadFuture(null);

			ByteBuf produce = future.getProduce();

			if (produce == null) {
				continue;
			}

			try {

				nextAccept(channel, produce);

			} finally {

				ReleaseUtil.release(future);
			}
		}
	}

}
