package imuanalyzer.data;

import java.util.Date;

/**
 * Contains all data which es necessary to describe a dataset of MARG data
 * 
 * @author Christopher-Eyk Hrabia
 * 
 */
public class DatasetMetadata {

	protected long id = 0;

	protected String name;
	protected String description;

	protected Date start = null;
	protected Date end = null;

	private static final String DEFAULT_DATASET_NAME = "Default";

	public static DatasetMetadata getDefaultMarker() {
		return new DatasetMetadata(DEFAULT_DATASET_NAME, "", 1, new Date(0),
				new Date(0));
	}

	public DatasetMetadata() {
		this(DEFAULT_DATASET_NAME, null);
	}

	public DatasetMetadata(String name, String description) {
		this(name, description, 0, new Date(new java.util.Date().getTime()),
				new Date(new java.util.Date().getTime()));
	}

	public DatasetMetadata(String name, String description, long id,
			Date start, Date end) {
		this.name = name;
		this.description = description;
		this.id = id;
		this.start = start;
		this.end = end;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return name + description;
	}

	public boolean isUsed() {
		return !start.equals(end);
	}

}
