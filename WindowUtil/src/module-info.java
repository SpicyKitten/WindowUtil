module avi.utils.window
{
	exports file;
	exports window;
	
	requires java.base;
	requires avi.utils.throwing;
	requires transitive java.desktop;
	requires transitive com.sun.jna.platform;
	requires transitive com.sun.jna;
}