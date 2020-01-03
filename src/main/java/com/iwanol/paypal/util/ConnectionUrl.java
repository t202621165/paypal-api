package com.iwanol.paypal.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionUrl {
	private Logger logger =  LoggerFactory.getLogger(this.getClass());
	private static ConnectionUrl connectionUrl;
	public static ConnectionUrl getConnectionUrl(){
		if(connectionUrl == null){
			connectionUrl = new ConnectionUrl();
			return connectionUrl;
		}else{
			return connectionUrl;
		}
	}
	
	public <v> String getDataFromURL(String strURL,Map<String,v> param,String charset) throws Exception {
		URL url = new URL(strURL);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);	
		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), charset);
		final StringBuilder sb;
		if (param != null) {
			sb = new StringBuilder(param.size() << 4);// 4次方
			final Set<String> keys = param.keySet();
			for (final String key : keys) {
				final Object value = param.get(key);
				sb.append(key); // 不能包含特殊字符
				sb.append('=');
				sb.append(value);
				sb.append('&');
			}
			// 将最后的 '&' 去掉
			sb.deleteCharAt(sb.length() - 1);
		}else{
			sb = new StringBuilder(100 << 4);
		}
		writer.write(sb.toString());
		writer.flush();
		writer.close();
		
		InputStreamReader reder = new InputStreamReader(conn.getInputStream(), charset);
		BufferedReader breader = new BufferedReader(reder);
		String content = null;
		String result = "";
		while ((content = breader.readLine()) != null) {
			result += content;
		}		
		return result;
	}
	
	/**  
     * 发起http请求并获取结果  
     *   
     * @param requestUrl 请求地址  
     * @param requestMethod 请求方式（GET、POST）  
     * @param outputStr 提交的数据  
     * @return String 响应数据  
     */    
	public String httpRequest(String requestUrl, String requestMethod, String outputStr, String charset){  
        StringBuffer buffer = new StringBuffer();    
        try {
            URL url = new URL(requestUrl);  
            if ("https".equals(url.getProtocol())){
            	SslUtils.ignoreSsl();
            }
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();       
            httpUrlConn.setDoOutput(true);    
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);    
            // 设置请求方式（GET/POST）    
            httpUrlConn.setRequestMethod(requestMethod);    
    
            httpUrlConn.connect();    
    
            // 当有数据需要提交时
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码    
                outputStream.write(outputStr.getBytes(charset));    
                outputStream.close();    
            }   
    
            // 将返回的输入流转换成字符串    
            InputStream inputStream = httpUrlConn.getInputStream();    
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,charset);    
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);    
    
            String str = null;    
            while ((str = bufferedReader.readLine()) != null) {    
                buffer.append(str);    
            }    
            bufferedReader.close();    
            inputStreamReader.close();    
            // 释放资源    
            inputStream.close();    
            inputStream = null;    
            httpUrlConn.disconnect();    
              
        } catch (Exception e) {    
           //e.printStackTrace();
        	logger.info("下发请求失败:"+e.getMessage());
        }    
        return buffer.toString();    
    }
	
	public String httpRequest(String reqUrl,String json) { 
        StringBuffer sb = new StringBuffer(""); 
        try { 
        	//创建连接 
            URL url = new URL(reqUrl); 
            HttpURLConnection connection = (HttpURLConnection) url 
                    .openConnection(); 
            connection.setDoOutput(true); 
            connection.setDoInput(true); 
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Charsert", "UTF-8"); //设置请求编码
            connection.setUseCaches(false); 
            connection.setInstanceFollowRedirects(true); 
            connection.setRequestProperty("Content-Type","application/json");
            connection.setConnectTimeout(60000);//连接超时时间一分钟
            connection.setReadTimeout(60000);//响应超时时间一分钟
            connection.connect(); 

            //POST请求
            DataOutputStream out = new DataOutputStream( 
                    connection.getOutputStream()); 
            out.writeBytes(json);
            out.flush(); 
            out.close(); 

            //读取响应 
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));//设置编码,否则中文乱码

            String lines; 
            while ((lines = reader.readLine()) != null) { 
                sb.append(lines); 
            } 
            reader.close(); 
         // 断开连接 
            connection.disconnect(); 
        } catch (MalformedURLException e) { 
            logger.info(e.getMessage());
            return "failed";
        } catch (UnsupportedEncodingException e) { 
        	logger.info(e.getMessage());
            return "failed";
        } catch (IOException e) { 
        	logger.info(e.getMessage()); 
            return "failed";
        }
		return sb.toString(); 
    }
	
}
