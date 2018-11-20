package com.timesnew.im.socket.http;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP协议相关内容定义
 * @author adminitartor
 *
 */
public class HttpContext {
	/**
	 * 介质类型映射
	 * key:资源后缀名
	 * value:Content-Type中对应的值
	 */
	private static Map<String,String> MIME_MAPPING = new HashMap<>();
	/**
	 * 状态代码与描述映射
	 * key:状态代码
	 * value:对应的状态描述
	 */
	private static Map<Integer,String> STATUS_CODE_REASON_MAPPING = new HashMap<>();
	
	
	static{
		//初始化
		initMimeMapping();
		initStatusCodeReasonMapping();
	}
	/**
	 * 初始化状态代码与描述的映射
	 */
	private static void initStatusCodeReasonMapping(){
		STATUS_CODE_REASON_MAPPING.put(200, "OK");
		STATUS_CODE_REASON_MAPPING.put(302, "Moved Temporarily");
		STATUS_CODE_REASON_MAPPING.put(404, "Not Found");
		STATUS_CODE_REASON_MAPPING.put(500, "Internal Server Error");
	}
	/**
	 * 初始化介质类型映射
	 * 
	 */
	private static void initMimeMapping(){
		
		/*
		 * 使用DOM4J读取并解析conf/web.xml文件.
		 */
		try {
			SAXReader reader = new SAXReader();
			Document doc 
				= reader.read(new File("conf/web.xml"));
			Element root = doc.getRootElement();
			List<Element> mimeList 
				= root.elements("mime-mapping");
			for(Element mimeEle : mimeList){
				String key = mimeEle.elementText("extension");
				String value = mimeEle.elementText("mime-type");
				MIME_MAPPING.put(key, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 根据给定的资源后缀名获取对应的Content-Type值
	 * @param name
	 * @return
	 */
	public static String getMimeType(String name){
		return MIME_MAPPING.get(name);
	}
	/**
	 * 根据给定的状态代码获取对应的状态描述
	 * @param code
	 * @return
	 */
	public static String getStatusReason(int code){
		return STATUS_CODE_REASON_MAPPING.get(code);
	}
	
}







