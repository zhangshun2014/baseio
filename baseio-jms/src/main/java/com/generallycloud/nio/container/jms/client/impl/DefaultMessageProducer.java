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
package com.generallycloud.nio.container.jms.client.impl;

import java.io.IOException;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.jms.BytedMessage;
import com.generallycloud.nio.container.jms.MQException;
import com.generallycloud.nio.container.jms.Message;
import com.generallycloud.nio.container.jms.client.MessageProducer;
import com.generallycloud.nio.container.jms.server.MQProducerServlet;
import com.generallycloud.nio.container.jms.server.MQPublishServlet;

public class DefaultMessageProducer implements MessageProducer {

	private FixedSession session = null;
	
	public DefaultMessageProducer(FixedSession session) {
		this.session = session;
	}

	@Override
	public boolean offer(Message message) throws MQException {
		return offer(message, MQProducerServlet.SERVICE_NAME);
	}
	
	private boolean offer(Message message,String serviceName) throws MQException {
		
		String param = message.toString();

		ProtobaseReadFuture future = null;

		int msgType = message.getMsgType();

		try {

			if (msgType == Message.TYPE_TEXT || msgType == Message.TYPE_MAP) {

				future = session.request(serviceName, param);

			} else if (msgType == Message.TYPE_TEXT_BYTE || msgType == Message.TYPE_MAP_BYTE) {
				
				BytedMessage _message = (BytedMessage) message;
				
				future = session.request(serviceName, param, _message.getByteArray());
				
			} else {
				
				throw new MQException("msgType:" + msgType);
			}
		} catch (IOException e) {
			
			throw new MQException(e.getMessage(), e);
		}
		
		String result = future.getReadText();

		if (result.length() == 1) {
			return "T".equals(result);
		}
		throw new MQException(result);

	}

	@Override
	public boolean publish(Message message) throws MQException {

		return offer(message, MQPublishServlet.SERVICE_NAME);
	}

}
