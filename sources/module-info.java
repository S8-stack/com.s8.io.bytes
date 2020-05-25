/**
 * 
 */
/**
 * @author pc
 *
 */
module com.s8.io.bytes {
	
	exports com.s8.io.bytes;
	
	
	// others
	exports com.s8.io.bytes.utilities.index;
	exports com.s8.io.bytes.utilities.chain;
	exports com.s8.io.bytes.utilities.reactive;
	exports com.s8.io.bytes.utilities.log;
	exports com.s8.io.bytes.utilities.boot;
	exports com.s8.io.bytes.utilities.units;
	exports com.s8.io.bytes.utilities.others;
	
	
	/*
	 * dependencies: XML
	 */
	requires transitive com.s8.lang.xml;
	requires transitive com.s8.api;

}