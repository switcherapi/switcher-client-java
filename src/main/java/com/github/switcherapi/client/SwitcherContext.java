package com.github.switcherapi.client;

import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.exception.SwitchersValidationException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.SwitcherRequest;

/**
 * <b>Switcher Context</b>
 * <p>
 * 
 * This class will load Switcher Properties internally, making it ready to use.
 * By inheriting this class, all Switchers can be placed in one single place.
 * 
 * <pre>
 * public class SwitcherFeatures extends SwitcherContext {
 * 	&#064;SwitcherKey
 * 	public static final String MY_FEATURE = "MY_FEATURE";
 * }
 * </pre>
 * 
 * @see SwitcherKey
 * @author Roger Floriano (petruki)
 */
public abstract class SwitcherContext extends SwitcherContextBase {
	
	static {
		loadProperties();
	}
	
	/**
	 * Load properties from the resource's folder, look up for resources/switcherapi.properties file.
	 * After loading the properties, it will validate the arguments and load the Switchers in memory.
	 */
	public static void loadProperties() {
		SwitcherContextBase.contextBase = null;
		SwitcherContextBase.loadProperties("switcherapi");
		SwitcherContextBase.initializeClient();
	}
	
	/**
	 * {@link SwitcherContextBase#initializeClient()}
	 */
	public static void initializeClient() {
		SwitcherContextBase.initializeClient();
	}
	
	/**
	 * {@link SwitcherContextBase#getSwitcher(String, boolean)}
	 *
	 * @param key name of the key created
	 * @param keepEntries when true it will return a cached Switcher with all parameters used before
	 *
	 * @return a ready to use Switcher
	 * @throws SwitcherKeyNotFoundException in case the key was not properly loaded
	 */
	public static SwitcherRequest getSwitcher(String key, boolean keepEntries) {
		return SwitcherContextBase.getSwitcher(key, keepEntries);
	}
	
	/**
	 * {@link #getSwitcher(String, boolean)}
	 * 
	 * @param key name
	 * @return a ready to use Switcher
	 */
	public static SwitcherRequest getSwitcher(String key) {
		return SwitcherContextBase.getSwitcher(key);
	}
	
	/**
	 * {@link SwitcherContextBase#validateSnapshot()}
	 *
	 * @return true if snapshot was updated
	 */
	public static boolean validateSnapshot() {
		return SwitcherContextBase.validateSnapshot();
	}
	
	/**
	 * {@link SwitcherContextBase#checkSwitchers()}
	 *
	 * @throws SwitchersValidationException when one or more Switcher Key is not found
	 */
	public static void checkSwitchers() {
		SwitcherContextBase.checkSwitchers();
	}

	/**
	 * Retrieve local snapshot version
	 *
	 * @return snapshot version
	 */
	public static long getSnapshotVersion() {
		return SwitcherContextBase.instance.getSnapshotVersion();
	}
	
	/**
	 * {@link SwitcherContextBase#contextStr(ContextKey)}
	 *
	 * @param contextKey to be retrieved
	 * @return Value configured for the context parameter
	 */
	public static String contextStr(ContextKey contextKey) {
		return SwitcherContextBase.contextStr(contextKey);
	}
	
	/**
	 * {@link SwitcherContextBase#contextBol(ContextKey)}
	 *
	 * @param contextKey to be retrieved
	 * @return Value configured for the context parameter
	 */
	public static boolean contextBol(ContextKey contextKey) {
		return SwitcherContextBase.contextBol(contextKey);
	}
	
	/**
	 * {@link SwitcherContextBase#configure(ContextBuilder)}
	 *
	 * @param builder specification to be applied
	 */
	public static void configure(ContextBuilder builder) {
		SwitcherContextBase.configure(builder);
	}

}
