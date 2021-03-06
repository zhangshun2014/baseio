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
package com.generallycloud.nio.container;

import java.io.File;
import java.io.FileInputStream;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.FileUtil;
import com.generallycloud.nio.component.SocketSession;

public class FileSendUtil {
	
	public void sendFile(SocketSession session,String serviceName,File file,int cacheSize) throws Exception {

		FileInputStream inputStream = new FileInputStream(file);
		
		int available = inputStream.available();
		
		int time = (available + cacheSize) / cacheSize - 1;
		
		byte [] cache = new byte[cacheSize];
		
		JSONObject json = new JSONObject();
		json.put(FileReceiveUtil.FILE_NAME, file.getName());
		json.put(FileReceiveUtil.IS_END, false);
		
		String jsonString = json.toJSONString();
		
		for (int i = 0; i < time; i++) {
			
			FileUtil.readFromtInputStream(inputStream, cache);
			
			ProtobaseReadFuture f = new ProtobaseReadFutureImpl(session.getContext(),serviceName);
			
			f.write(jsonString);
			
			f.writeBinary(cache);
			
			session.flush(f);
		}
		
		int r = FileUtil.readFromtInputStream(inputStream, cache);
		
		json.put(FileReceiveUtil.IS_END, true);
		
		ProtobaseReadFuture f = new ProtobaseReadFutureImpl(session.getContext(),serviceName);
		
		f.write(json.toJSONString());
		
		f.writeBinary(cache,0,r);
		
		session.flush(f);
		
		CloseUtil.close(inputStream);
	}
}
