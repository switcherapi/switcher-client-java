package com.github.petruki.switcher.client.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.petruki.switcher.client.exception.SwitcherSnapshotLoadException;
import com.github.petruki.switcher.client.exception.SwitcherSnapshotWriteException;
import com.github.petruki.switcher.client.model.criteria.Domain;
import com.github.petruki.switcher.client.model.criteria.Snapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SnapshotLoader {
	
	private static final Logger logger = LogManager.getLogger(SnapshotLoader.class);
	
	private static final String SNAPSHOT_FILE_FORMAT = "%s/%s.json";
	
	private SnapshotLoader() {}

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
			final Snapshot data = gson.fromJson(new FileReader(snapshotFile), Snapshot.class);
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
	public static Domain loadSnapshot(final String snapshotLocation, final String environment) 
			throws SwitcherSnapshotLoadException, FileNotFoundException {
		
		final Gson gson = new Gson();

		try {
			final Snapshot data = gson.fromJson(new FileReader(String.format(SNAPSHOT_FILE_FORMAT, snapshotLocation, environment)), Snapshot.class);
			return data.getDomain();
		} catch (JsonSyntaxException | JsonIOException e) {
			logger.error(e);
			throw new SwitcherSnapshotLoadException(String.format(SNAPSHOT_FILE_FORMAT, snapshotLocation, environment), e);
		} catch (FileNotFoundException e) {
			throw e;
		}
	}
	
	/**
	 * Writes snapshot loaded from the API
	 * 
	 * @param snapshot
	 * @param snapshotLocation
	 * @param environment
	 * @throws SwitcherSnapshotWriteException
	 */
	public static void saveSnapshot(final Snapshot snapshot, final String snapshotLocation, 
			final String environment) throws SwitcherSnapshotWriteException {

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();

		try (
				final FileWriter fileWriter = new FileWriter(
						new File(String.format(SNAPSHOT_FILE_FORMAT, snapshotLocation, environment)));
				final BufferedWriter bw = new BufferedWriter(fileWriter);
				final PrintWriter wr = new PrintWriter(bw);
				) {
			wr.write(gson.toJson(snapshot));
		} catch (Exception e) {
			logger.error(e);
			throw new SwitcherSnapshotWriteException(String.format(SNAPSHOT_FILE_FORMAT, snapshotLocation, environment), e);
		}	

	}

}
