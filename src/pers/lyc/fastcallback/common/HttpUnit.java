package pers.lyc.fastcallback.common;

import java.io.IOException;
import java.net.URLDecoder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import ch.qos.logback.classic.Logger;
import pers.lyc.fastcallback.exception.FastException;

public class HttpUnit {

	private static Logger logger = (Logger) LoggerFactory.getLogger(HttpUnit.class); // 日志记录

	/**
	 * httpPost
	 * 
	 * @param url
	 *            请求路径
	 * @param jsonParam
	 *            请求参数
	 * @return
	 */
	public static String httpPost(String url, JSONObject jsonParam) {
		return httpPost(url, jsonParam.toString(), false);
	}

	/**
	 * post请求
	 * 
	 * @param url
	 *            url地址
	 * @param jsonParam
	 *            参数
	 * @param noNeedResponse
	 *            不需要返回结果
	 * @return
	 */
	public static String httpPost(String url, String jsonParam, boolean noNeedResponse) {
		// post请求返回结果
		HttpClient httpClient = HttpClients.createDefault();
		String jsonResult = null;
		HttpPost method = new HttpPost(url);
		try {
			if (null != jsonParam) {
				RequestConfig config = RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(2000).build();
				method.setConfig(config);
				// 解决中文乱码问题
				StringEntity entity = new StringEntity(jsonParam, "utf-8");
				entity.setContentEncoding("UTF-8");
				entity.setContentType("application/json");
				method.setEntity(entity);
			}
			HttpResponse result = httpClient.execute(method);
			url = URLDecoder.decode(url, "UTF-8");
			/** 请求发送成功，并得到响应 **/
			if (result.getStatusLine().getStatusCode() == 200) {
				String str = "";
				try {
					/** 读取服务器返回过来的json字符串数据 **/
					str = EntityUtils.toString(result.getEntity());
					if (noNeedResponse) {
						return null;
					}
					/** 把json字符串转换成json对象 **/
					jsonResult = str;
				} catch (Exception e) {
					System.out.println("post请求提交失败:" + url);
					throw new FastException("读取请求返回数据出错");
				}
			}
		} catch (IOException e) {
			System.out.println("post请求提交失败:" + url);
			throw new FastException("请求地址没有响应");
		}
		return jsonResult;
	}

	/**
	 * 发送get请求
	 * 
	 * @param url
	 *            路径
	 * @return
	 */
	public static JSONObject httpGet(String url) {
		// get请求返回结果
		JSONObject jsonResult = null;
		try {
			HttpClient client = HttpClients.createDefault();
			// 发送get请求
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);

			/** 请求发送成功，并得到响应 **/
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				/** 读取服务器返回过来的json字符串数据 **/
				String strResult = EntityUtils.toString(response.getEntity());
				/** 把json字符串转换成json对象 **/
				jsonResult = JSONObject.parseObject(strResult);
				url = URLDecoder.decode(url, "UTF-8");
			} else {
				logger.error("get请求提交失败:" + url);
			}
		} catch (IOException e) {
			logger.error("get请求提交失败:" + url, e);
		}
		return jsonResult;
	}
}
