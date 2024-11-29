package com.github.switcherapi.client.utils;

import com.github.switcherapi.client.exception.SwitcherSnapshotLoadException;
import com.github.switcherapi.client.exception.SwitcherSnapshotWriteException;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SnapshotLoader extends Utils {
	
	private static final String SNAPSHOT_FILE_FORMAT = "%s/%s.json";

	private static final Gson gson = new Gson();

	/**
	 * Load a specific snapshot file
	 * 
	 * @param snapshotFile Snapshot file exact match location
	 * @return Serialized Domain object
	 * @throws SwitcherSnapshotLoadException when JSON file has errors or the file was not found
	 */
	public static Domain loadSnapshot(final String snapshotFile) throws SwitcherSnapshotLoadException {
		try (FileReader fileReader = new FileReader(snapshotFile)) {
			final Snapshot data = gson.fromJson(fileReader, Snapshot.class);
			return data.getDomain();
		} catch (JsonSyntaxException | JsonIOException | IOException e) {
			throw new SwitcherSnapshotLoadException(snapshotFile, e);
		}
	}
	
	/**
	 * Load snapshot from the current running environment
	 * 
	 * @param snapshotLocation Snapshot folder
	 * @param environment name that is represented as [environment].json
	 * @return Serialized Domain object
	 * @throws SwitcherSnapshotLoadException when JSON file has errors
	 * @throws IOException when failing to read JSON file
	 */
	public static Domain loadSnapshot(final String snapshotLocation, final String environment)
			throws SwitcherSnapshotLoadException, IOException {
		final String path = String.format(SNAPSHOT_FILE_FORMAT, snapshotLocation, environment);

		try (FileReader fileReader = new FileReader(path)) {
			final Snapshot data = gson.fromJson(fileReader, Snapshot.class);
			return data.getDomain();
		} catch (JsonSyntaxException | JsonIOException e) {
			throw new SwitcherSnapshotLoadException(String.format(SNAPSHOT_FILE_FORMAT, snapshotLocation, environment), e);
		} catch (FileNotFoundException e) {
			return loadSnapshotFromResources(snapshotLocation, environment);
		}
	}

	/**
	 * Load snapshot from the resources folder
	 *
	 * @param snapshotLocation Snapshot folder
	 * @param environment name that is represented as [environment].json
	 * @return Serialized Domain object
	 * @throws SwitcherSnapshotLoadException when JSON file has errors
	 * @throws IOException when failing to read JSON file
	 */
	private static Domain loadSnapshotFromResources(final String snapshotLocation, final String environment)
			throws SwitcherSnapshotLoadException, IOException {
		final String path = String.format(SNAPSHOT_FILE_FORMAT, snapshotLocation, environment);

		try (InputStream is = SnapshotLoader.class.getResourceAsStream(path)) {
			if (Objects.isNull(is)) {
				throw new FileNotFoundException(path);
			}

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
				final Snapshot data = gson.fromJson(reader, Snapshot.class);
				return data.getDomain();
			} catch (JsonSyntaxException | JsonIOException e) {
				throw new SwitcherSnapshotLoadException(String.format(SNAPSHOT_FILE_FORMAT, snapshotLocation, environment), e);
			}
		}
	}
	
	/**
	 * Writes snapshot loaded from the API
	 * 
	 * @param snapshot Serialized snapshot object to be saved in a JSON file
	 * @param snapshotLocation Where the snapshot must be saved
	 * @param environment defines the name of the snapshot file
	 * @throws SwitcherSnapshotWriteException if something wrong happened while creating either the folder or file
	 */
	public static void saveSnapshot(final Snapshot snapshot, final String snapshotLocation, 
			final String environment) throws SwitcherSnapshotWriteException {

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();

		try {
			Path path = Paths.get(snapshotLocation);
			if (!path.toFile().exists())
				Files.createDirectories(path);
		} catch (Exception e) {
			throw new SwitcherSnapshotWriteException(String.format(SNAPSHOT_FILE_FORMAT, snapshotLocation, environment), e);
		}
		
		try (
				final FileWriter fileWriter = new FileWriter(String.format(SNAPSHOT_FILE_FORMAT, snapshotLocation, environment));
				final BufferedWriter bw = new BufferedWriter(fileWriter);
				final PrintWriter wr = new PrintWriter(bw)) {
			wr.write(gson.toJson(snapshot));
		} catch (Exception e) {
			throw new SwitcherSnapshotWriteException(String.format(SNAPSHOT_FILE_FORMAT, snapshotLocation, environment), e);
		}
	}

}
