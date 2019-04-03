package window;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;
import throwing.Throwing;

/**
 * A class defining a server communicating with an AutoHotKey process on
 * http://localhost:PORT/actionsequence
 * 
 * @author ratha
 */
public class Server
{
	/**
	 * Launches a server to communicate with an AutoHotKey executable that sends
	 * keys to windows in the background. Both the server and the executable cease
	 * to function at the moment that the JVM exits, if {@code daemon} is set to
	 * true.
	 * 
	 * @param daemon
	 *            Whether to launch the server on a daemon thread
	 * @return Whether the server was successfully launched
	 */
	public static boolean launch(boolean daemon)
	{
		System.out.println("Launching key server on port " + PORT + "...");
		try
		{
			@SuppressWarnings("resource") // closed when the VM shuts down
			var s = new ServerSocket(PORT, 0, InetAddress.getByName("localhost"));
			Runtime.getRuntime().addShutdownHook(new Thread(Throwing.of(s::close)));
			var serverThread = new Thread(Throwing.of(() ->
			{
				while (true)
				{
					Socket a = s.accept();
					var acceptor = new Thread(() -> run(a));
					/*
					 * Although the parent may be a daemon, we want connections to be
					 * processed before exiting the JVM
					 */
					acceptor.setDaemon(false);
					acceptor.start();
				}
			}, (e) -> System.out.println("Server dead: " + e.getMessage())));
			serverThread.setDaemon(daemon);
			serverThread.start();
			System.out.println("Server up and running!");
			var process =
				new ProcessBuilder("resources/KeySender.exe", "" + PORT).start();
			System.out.println("KeySender launched!");
			Runtime.getRuntime().addShutdownHook(new Thread(() ->
			{
				process.destroy();
				System.out.println("KeySender terminated.");
			}));
			return true;
		}
		catch (Exception e)
		{
			System.err.println("Failed to launch server: " + e.getMessage());
			return false;
		}
	}
	
	static final File ROOT = new File(".");
	static final boolean verbose = false;
	static final ConcurrentLinkedQueue<String> actionSequence =
		new ConcurrentLinkedQueue<>();
	static final Charset utf8 = StandardCharsets.UTF_8;
	static final int PORT;
	static volatile ReadyState isReady = ReadyState.BUSY;
	static
	{
		try
		{
			WinUtil.properties.computeIfPresent("send-key-port", (a, b) -> WinUtil
				.readProperty(WinUtil.cleanComments().andThen(Integer::parseInt), b));
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
			System.err.println(
				"WinUtil Key Server failed to load resources: Check that config settings are correct");
		}
		PORT = (int)WinUtil.properties.getOrDefault("send-key-port", 6060);
	}
	
	public static boolean ready()
	{
		return isReady == ReadyState.READY;
	}
	
	public static void run(Socket connect)
	{
		BufferedReader in = null;
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;
		String request = null;
		try
		{
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			
			// get first line of the request from the client
			String input = in.readLine();
			if (input == null)
				return;
			if (verbose)
				System.out.println("Request: " + input);
			request = input;
			var parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase();
			request = parse.nextToken().toLowerCase();
			
			if (method.equals("POST") && request.equals("/actionsequence"))
			{
				var params = new HashMap<String, String>();
				String line = "";
				while (!(line = in.readLine()).equals(""))
				{
					String[] mapping = line.split(":", 2);
					params.put(mapping[0].trim(), mapping[1].trim());
				}
				// params.entrySet().forEach(System.out::println);
				int length = Integer.parseInt(params.get("Content-Length"));
				var buffer = new char[length];
				int actualRead = in.read(buffer, 0, length);
				var s = new String(buffer, 0, actualRead);
				String[] info = s.split("=", 2);
				if (verbose)
					System.out.println("Read: " + s);
				synchronized (isReady)
				{
					isReady = ReadyState.BUSY;
					actionSequence.offer(URLDecoder.decode(info[0], utf8) + "="
						+ URLDecoder.decode(info[1], utf8));
				}
				textResponse(out, dataOut, "Action sequence accepted");
			}
			else if (method.equals("GET") && request.equals("/actionsequence"))
			{
				String output = "";
				synchronized (isReady)
				{
					if (!actionSequence.isEmpty())
					{
						isReady = ReadyState.BUSY;
						output = actionSequence.poll();
					}
					else
						isReady = ReadyState.READY;
				}
				textResponse(out, dataOut, output);
			}
			else
			{
				if (verbose)
				{
					System.out.println("501 Not Implemented : " + method);
				}
				// we return the not supported file to the client
				fileResponse(out, dataOut, new File(ROOT, "not_implemented.html"),
					"501 Not Implemented");
			}
		}
		catch (NoSuchElementException | FileNotFoundException fnfe)
		{
			try
			{
				fileResponse(out, dataOut, new File(ROOT, "404.html"),
					"404 File Not Found");
			}
			catch (IOException ioe)
			{
				System.err
					.println("Error with file not found response: " + ioe.getMessage());
			}
			if (verbose)
			{
				if (fnfe instanceof FileNotFoundException)
					System.out.println("File " + request + " not found");
				else
					System.out.println("Error parsing: " + request);
			}
		}
		catch (IOException ioe)
		{
			System.err.println("Server error : " + ioe);
		}
		finally
		{
			try
			{
				in.close();
				out.close();
				dataOut.close();
				if (!connect.isClosed())
					connect.close(); // we close socket connection
			}
			catch (Exception e)
			{
				System.err.println("Error closing stream : " + e.getMessage());
			}
			
			if (verbose)
			{
				System.out.println("Connection closed.\n");
			}
		}
		
	}
	
	private static void fileResponse(PrintWriter out, OutputStream dataOut, File file,
		String code) throws IOException
	{
		int fileLength = (int)file.length();
		String contentType = getContentType(file.getPath());
		byte[] fileData = readFileData(file, fileLength);
		
		// we send HTTP Headers with data to client
		out.println("HTTP/1.1 " + code);//
		out.println("Server: Java HTTP Server : 1.0");
		out.println("Date: " + new Date());
		out.println("Content-type: " + contentType);
		out.println("Content-length: " + fileLength);
		out.println();
		out.flush();
		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
	}
	
	private static void textResponse(PrintWriter out, OutputStream dataOut, String output)
		throws IOException
	{
		byte[] data = output.getBytes(utf8);
		out.println("HTTP/1.1 200 OK");
		out.println("Server: Java HTTP Server : 1.0");
		out.println("Date: " + new Date());
		out.println("Content-type: " + "text/plain");
		out.println("Content-length: " + data.length);
		out.println();
		out.flush();
		
		dataOut.write(data, 0, data.length);
		dataOut.flush();
	}
	
	private static String getContentType(String fileRequested)
	{
		if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
			return "text/html";
		else
			return "text/plain";
	}
	
	private static byte[] readFileData(File file, int fileLength) throws IOException
	{
		byte[] fileData = new byte[fileLength];
		try (var fileIn = new FileInputStream(file))
		{
			fileIn.read(fileData);
		}
		return fileData;
	}
}
