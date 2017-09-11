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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import ch.qos.logback.classic.Logger;
import pers.lyc.fastcallback.exception.FastException;

public class HttpUnit {

	private static Logger logger = (Logger) LoggerFactory.getLogger(HttpUnit.class); // ��־��¼

	/**
	 * httpPost
	 * 
	 * @param url
	 *            ����·��
	 * @param jsonParam
	 *            �������
	 * @return
	 */
	public static String httpPost(String url, JSONObject jsonParam) {
		return httpPost(url, jsonParam.toString(), false);
	}

	/**
	 * post����
	 * 
	 * @param url
	 *            url��ַ
	 * @param jsonParam
	 *            ����
	 * @param noNeedResponse
	 *            ����Ҫ���ؽ��
	 * @return
	 */
	public static String httpPost(String url, String jsonParam, boolean noNeedResponse) {
		// post���󷵻ؽ��
		HttpClient httpClient = HttpClients.createDefault();
		String jsonResult = null;
		HttpPost method = new HttpPost(url);
		try {
			if (null != jsonParam) {
				RequestConfig config = RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(2000).build();
				method.setConfig(config);
				// ���������������
				StringEntity entity = new StringEntity(jsonParam, "utf-8");
				entity.setContentEncoding("UTF-8");
				entity.setContentType("application/json");
				method.setEntity(entity);
			}
			HttpResponse result = httpClient.execute(method);
			url = URLDecoder.decode(url, "UTF-8");
			/** �����ͳɹ������õ���Ӧ **/
			if (result.getStatusLine().getStatusCode() == 200) {
				String str = "";
				try {
					/** ��ȡ���������ع�����json�ַ������� **/
					str = EntityUtils.toString(result.getEntity());
					if (noNeedResponse) {
						return null;
					}
					/** ��json�ַ���ת����json���� **/
					jsonResult = str;
				} catch (Exception e) {
					System.out.println("post�����ύʧ��:" + url);
					throw new FastException("��ȡ���󷵻����ݳ���");
				}
			}
		} catch (IOException e) {
			System.out.println("post�����ύʧ��:" + url);
			throw new FastException("�����ַû����Ӧ");
		}
		return jsonResult;
	}

	/**
	 * ����get����
	 * 
	 * @param url
	 *            ·��
	 * @return
	 */
	public static JSONObject httpGet(String url) {
		// get���󷵻ؽ��
		JSONObject jsonResult = null;
		try {
			HttpClient client = HttpClients.createDefault();
			// ����get����
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);

			/** �����ͳɹ������õ���Ӧ **/
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				/** ��ȡ���������ع�����json�ַ������� **/
				String strResult = EntityUtils.toString(response.getEntity());
				/** ��json�ַ���ת����json���� **/
				jsonResult = JSONObject.parseObject(strResult);
				url = URLDecoder.decode(url, "UTF-8");
			} else {
				logger.error("get�����ύʧ��:" + url);
			}
		} catch (IOException e) {
			logger.error("get�����ύʧ��:" + url, e);
		}
		return jsonResult;
	}
}
