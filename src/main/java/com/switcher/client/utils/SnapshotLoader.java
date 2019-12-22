package com.switcher.client.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.switcher.client.domain.criteria.Criteria;
import com.switcher.client.domain.criteria.Domain;
import com.switcher.client.exception.SwitcherSnapshotLoadException;

public class SnapshotLoader {
	
	private static final Logger logger = Logger.getLogger(SnapshotLoader.class);

	public static Domain loadSnapshot(final String snapshotLocation) throws SwitcherSnapshotLoadException {
		
		final Gson gson = new Gson();

		try {
			final Data data = gson.fromJson(new FileReader(snapshotLocation), Data.class);
			return data.getDomain();
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			logger.error(e);
			throw new SwitcherSnapshotLoadException(snapshotLocation, e);
		}
	}
	
	class Data {
		
		private Criteria data;
		
		public Domain getDomain() {
			return data.getDomain();
		}
		
	}

}
