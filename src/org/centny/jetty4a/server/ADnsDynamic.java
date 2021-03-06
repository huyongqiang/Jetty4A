package org.centny.jetty4a.server;

import java.io.File;

import org.centny.cny4a.util.Util;
import org.centny.jetty4a.server.api.DnsDynamic;
import org.centny.jetty4a.server.api.ServerListener;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Base64;
import dalvik.system.DexClassLoader;

/**
 * the implemented DnsDynamic class for android.
 * 
 * @author Centny.
 * 
 */
public class ADnsDynamic extends DnsDynamic {

	/**
	 * shared instance.
	 */
	private static ADnsDynamic sharedInstance_;

	/**
	 * get shared instance.
	 * 
	 * @return the instance.
	 */
	public static ADnsDynamic sharedInstance() {
		if (sharedInstance_ == null) {
			sharedInstance_ = new ADnsDynamic();
		}
		return sharedInstance_;
	}

	//
	private NetworkChangedReceiver receiver;
	private ContextWrapper ctx;
	private boolean only4Wifi;

	/**
	 * the default constructor.
	 */
	public ADnsDynamic() {
		this.only4Wifi = true;
	}

	//
	@Override
	protected String createBase64(String tar) {
		return Base64.encodeToString(tar.getBytes(), Base64.DEFAULT);
	}

	@Override
	protected ClassLoader externalClassLoader(File jar, ClassLoader parent) {
		try {
			String wdir = System.getProperty(ServerListener.J4A_WDIR);
			DexClassLoader dcl = new DexClassLoader(jar.getAbsolutePath(),
					wdir, null, parent);
			return dcl;
		} catch (Exception e) {
			return parent;
		}
	}

	@Override
	protected void preUpdate() {
		if (this.ctx != null) {
			this.setMyip(Util.localIpAddress(this.ctx, this.only4Wifi));
		} else {
			this.setMyip(null);
		}
	}

	public void startNetworkListener(ContextWrapper ctx) {
		this.receiver = new NetworkChangedReceiver(this);
		this.ctx = ctx;
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		this.ctx.registerReceiver(this.receiver, filter);
	}

	public void stopNetworkListener() {
		if (this.receiver == null) {
			return;
		}
		this.ctx.unregisterReceiver(this.receiver);
		this.receiver = null;
		this.ctx = null;
	}

	private void onNetworkChanged() {
		this.updateDnsDynamic();
	}

	/**
	 * the network status change listener.
	 * 
	 * @author Centny.
	 * 
	 */
	private static class NetworkChangedReceiver extends BroadcastReceiver {
		/**
		 * the dns.
		 */
		private ADnsDynamic dns;

		/**
		 * the default constructor.
		 * 
		 * @param dns
		 *            the dns dynamic.
		 */
		public NetworkChangedReceiver(ADnsDynamic dns) {
			super();
			this.dns = dns;
		}

		@SuppressLint("InlinedApi")
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent
						.getAction())) {
					return;
				}
				this.dns.onNetworkChanged();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the only4Wifi
	 */
	public boolean isOnly4Wifi() {
		return only4Wifi;
	}

	/**
	 * @param only4Wifi
	 *            the only4Wifi to set
	 */
	public void setOnly4Wifi(boolean only4Wifi) {
		this.only4Wifi = only4Wifi;
	}

}
