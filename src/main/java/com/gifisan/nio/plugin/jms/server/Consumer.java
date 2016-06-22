package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.BytedMessage;
import com.gifisan.nio.plugin.jms.Message;

public class Consumer {

	private String				queueName		= null;
	private JMSSessionAttachment	attachment	= null;
	private ConsumerQueue		consumerQueue	= null;
	private Session			session		= null;
	private ReadFuture		future		= null;
	private Message 			message 		= null;

	public Consumer(ConsumerQueue consumerQueue, JMSSessionAttachment attachment, Session session,
			ReadFuture future, String queueName) {
		this.consumerQueue = consumerQueue;
		this.queueName = queueName;
		this.attachment = attachment;
		this.session = session;
		this.future = future;
	}

	public String getQueueName() {
		return queueName;
	}

	public ConsumerQueue getConsumerQueue() {
		return consumerQueue;
	}

	//FIXME push 失败时对message进行回收,并移除Consumer
	public void push(Message message) {
		
		this.message = message;

		TransactionSection section = attachment.getTransactionSection();

		if (section != null) {
			section.offerMessage(message);
		}

		int msgType = message.getMsgType();

		String content = message.toString();

		Session session = this.session;
		
		ReadFuture future = ReadFutureFactory.create(this.future);
		
		future.attach(this);
		
		future.write(content);

		if (msgType == Message.TYPE_TEXT || msgType == Message.TYPE_MAP) {
			
			session.flush(future);

		} else if(msgType == Message.TYPE_TEXT_BYTE || msgType == Message.TYPE_MAP_BYTE) {
			
			BytedMessage byteMessage = (BytedMessage) message;

			byte[] bytes = byteMessage.getByteArray();

			future.setInputIOEvent(new ByteArrayInputStream(bytes));

			session.flush(future);
		}
	}
	
//	public void refresh(){
//		
//		this.future = ReadFutureFactory.create(future);
//	}
	
	public Message getMessage() {
		return message;
	}

	public Consumer clone(){
		return new Consumer(consumerQueue, attachment, session, ReadFutureFactory.create(future), queueName);
	}
}