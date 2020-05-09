package com.github.petruki.switcher.client.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.petruki.switcher.client.domain.criteria.Criteria;
import com.github.petruki.switcher.client.domain.criteria.Domain;
import com.github.petruki.switcher.client.exception.SwitcherSnapshotLoadException;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SnapshotLoader {
	
	private static final Logger logger = LogManager.getLogger(SnapshotLoader.class);

	/**
	 * Load a specific snapshot file
	 * 
	 * @param snapshotFile location and file name must be provided
	 * @return
	 * @throws SwitcherSnapshotLoadException
	 */
	public static Domain loadSnapshot(final String snapshotFile) throws SwitcherSnapshotLoadException {
		
		final Gson gson = new Gson();

		try {
			final Data data = gson.fromJson(new FileReader(snapshotFile), Data.class);
			return data.getDomain();
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			logger.error(e);
			throw new SwitcherSnapshotLoadException(snapshotFile, e);
		}
	}
	
	/**
	 * Load snapshot from the current running environment
	 * 
	 * @param snapshotLocation
	 * @param environment
	 * @return
	 * @throws SwitcherSnapshotLoadException
	 */
	public static Domain loadSnapshot(final String snapshotLocation, final String environment) throws SwitcherSnapshotLoadException {
		
		final Gson gson = new Gson();

		try {
			final Data data = gson.fromJson(new FileReader(String.format("%s/%s.json", snapshotLocation, environment)), Data.class);
			return data.getDomain();
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			logger.error(e);
			throw new SwitcherSnapshotLoadException(String.format("%s/%s.json", snapshotLocation, environment), e);
		}
	}
	
	class Data {
		
		private Criteria data;
		
		public Domain getDomain() {
			return data.getDomain();
		}
		
	}

}
