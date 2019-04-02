package window;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class defining a client that sends key sequences to a server that
 * communicates with an AutoHotKey process, which executes those key sequences
 */
public class Client
{
	public static void postSequence(String title, String actionSequence) throws Exception
	{
		URL url = new URL("http://localhost:" + Server.PORT + "/actionSequence");
		Map<String, Object> params = new LinkedHashMap<>();
		params.put(title, actionSequence);
		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String, Object> param : params.entrySet())
		{
			if (postData.length() != 0)
				postData.append('&');
			postData.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()),
				StandardCharsets.UTF_8));
		}
		byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);
		
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		conn.setDoOutput(true);
		conn.getOutputStream().write(postDataBytes);
		conn.getOutputStream().flush();
		conn.getInputStream().close();
	}
}
