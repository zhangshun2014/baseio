package com.gifisan.nio.plugin.jms.server;

import java.io.OutputStream;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.Message;

public class JMSPublishServlet extends JMSServlet {

	public static final String	SERVICE_NAME	= JMSPublishServlet.class.getSimpleName();

	public void accept(Session session, ReadFuture future, JMSSessionAttachment attachment) throws Exception {

		MQContext context = getMQContext();

		if (future.hasOutputStream()) {

			OutputStream outputStream = future.getOutputStream();

			if (outputStream == null) {
				future.setOutputIOEvent(new BufferedOutputStream(future.getStreamLength()));
				return;
			}
		}

		Message message = context.parse(future);

		context.publishMessage(message);

		future.write(ByteUtil.TRUE);

		session.flush(future);
	}
}