package com.generallycloud.nio.container.rtp.server;

import com.generallycloud.nio.component.SocketSEListenerAdapter;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.rtp.RTPContext;

public class RTPSessionEventListener extends SocketSEListenerAdapter {
	
	@Override
	public void sessionOpened(SocketSession session) {
		
		RTPContext context = RTPContext.getInstance();
		
		RTPSessionAttachment attachment = context.getSessionAttachment(session);

		if (attachment == null) {

			attachment = new RTPSessionAttachment(context);

			session.setAttachment(context.getPluginIndex(), attachment);
		}

	}

	@Override
	public void sessionClosed(SocketSession session) {
		
		RTPContext context = RTPContext.getInstance();
		
		RTPSessionAttachment attachment = context.getSessionAttachment(session);
		
		if (attachment == null) {
			return;
		}
		
		RTPRoom room = attachment.getRtpRoom();
		
		if (room == null) {
			return;
		}

//		room.leave(session.getDatagramChannel()); //FIXME udp 
	}
	
}