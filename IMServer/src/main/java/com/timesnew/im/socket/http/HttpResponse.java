package com.timesnew.im.socket.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 响应对象
 * 每一个实例用于表示一个实际发送给客户端的响应
 * 内容
 * @author adminitartor
 *
 */
public class HttpResponse {
	/*
	 * 状态行相关信息定义
	 */
	//状态代码
	private int statusCode = 200;
	//状态描述
	private String statusReason = "OK";
	
	/*
	 * 响应头相关信息定义
	 * key:响应头的名字
	 * value:响应头对应的值
	 */
	private Map<String,String> headers = new HashMap<String,String>();
	
	
	/*
	 * 响应正文对应的实体文件
	 * 当设置了该文件,那么将来该文件的数据
	 * 就会作为当前响应的响应正文内容被发送
	 * 给客户端.
	 */
	private File entity;

	private Socket socket;
	
	//该输出流通过socket获得,用于给客户端回复内容
	private OutputStream out;
	
	public HttpResponse(Socket socket){
		try {
			this.socket = socket;
			this.out = socket.getOutputStream();
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * 将当前响应内容以HTTP响应的格式回复给客户端
	 */
	public void flush(){
		/*
		 * 回复客户端需要按照HTTP的响应格式来发送
		 * 1:发送状态行
		 * 2:发送响应头
		 * 3:发送响应正文
		 */
		sendStatusLine();
		sendHeader();
//		sendContent();
	}
	/**
	 * 发送状态行
	 */
	private void sendStatusLine(){
		try {
			//发送状态行内容
			String line = "HTTP/1.1"+" "+statusCode+" "+statusReason;
			println(line);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 发送响应头
	 */
	private void sendHeader(){
		try {
			/*
			 * 发送响应头
			 * 遍历headers这个Map将所有头发送
			 */
			for(Entry<String,String> header: headers.entrySet()){
				String name = header.getKey();
				String value = header.getValue();
				println(name+": "+value);
			}
			
			
			//单独发送CRLF
			println("");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 发送响应正文
	 */
	private void sendContent(){
		try(
			FileInputStream fis 
				= new FileInputStream(entity);	
		){			
			byte[] data = new byte[1024*10];
			int len = -1;
			while((len = fis.read(data))!=-1){
				out.write(data, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 向客户端发送一行字符串(自动以CRLF结尾)
	 * @param line
	 */
	private void println(String line){
		try {
			out.write(line.getBytes("ISO8859-1"));
			//written CR
			out.write(13);
			//written LF
			out.write(10);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public File getEntity() {
		return entity;
	}
	/**
	 * 设置响应的实体对象,设置后会自动添加
	 * 两个头信息:Content-Type与Content-Length
	 * 若文件没有后缀名,则Content-Type不会被添加
	 * @param entity
	 */
	public void setEntity(File entity) {
		/*
		 * 当设置一个响应实体文件时,自动添加针对
		 * 该文件的两个头信息:
		 * Content-Type和Content-Length
		 */
		//若文件名存在后缀,自动追加Content-Type头
		if(entity.getName().matches(".+\\.[a-zA-Z0-9]+")){
			//获取该文件的后缀名
			String ext = entity.getName().split("\\.")[1];
			headers.put("Content-Type",HttpContext.getMimeType(ext));
		}
		headers.put("Content-Length", entity.length()+"");
		this.entity = entity;
	}
	/**
	 * 设置一个消息头
	 * @param name
	 * @param value
	 */
	public void putHeader(String name,String value){
		this.headers.put(name, value);
	}
	
	public String getHeader(String name){
		return this.headers.get(name);
	}

	public int getStatusCode() {
		return statusCode;
	}
	/**
	 * 设置状态代码
	 * @param statusCode
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
		/*
		 * 外界在设置状态代码的同时自动将其对应
		 * 的状态描述设置好.
		 */
		this.statusReason = HttpContext.getStatusReason(statusCode);
	}
	
	
}






