package uk.co.gridkey.api;

import java.util.HashMap;

import org.apache.commons.codec.binary.Hex;

public class WaveformSet {
	private String datetime;
	private long unixtime;
	private String humantime;
	private int instanceNumber;
	private int feederNumber;
	private int phaseNumber;
	private boolean isActiveBoardWaveform;
	private HashMap<String, String> voltageWaveform;
	private HashMap<String, String> currentWaveform;
	private HashMap<String, String> combinedWaveform;

	/**
	 * Public constructor for the WaveformSet class
	 * 
	 * @param datetime
	 *            the datetime in Gridtime format (yyMMddHHmmss)
	 * @param instanceNumber
	 *            the unique instance number associated with the waveform capture
	 * @param voltageWaveformBytes
	 *            the raw bytes containing the voltage waveform
	 * @param currentWaveformBytes
	 *            the raw bytes containing the current waveform
	 * @param feeder
	 *            the feeder number
	 * @param phase
	 *            the phase number
	 */
	public WaveformSet(String datetime, int instanceNumber, byte[] voltageWaveformBytes, byte[] currentWaveformBytes,
			byte[] combinedWaveformBytes, int feeder, int phase, boolean isActiveBoardWaveform) {
		// Set the HashMaps to empty maps
		voltageWaveform = new HashMap<String, String>();
		currentWaveform = new HashMap<String, String>();
		combinedWaveform = new HashMap<String, String>();

		// Indicate the type of waveform
		this.isActiveBoardWaveform = isActiveBoardWaveform;

		// Set the date time
		this.datetime = datetime;
		this.unixtime = TimeConverter.GridTimeToUnixTime(datetime);
		this.humantime = TimeConverter.GridTimeToDateTimeForCsv(datetime);

		// Set the instance number
		this.instanceNumber = instanceNumber;

		// Set the waveforms
		if (isActiveBoardWaveform) {
			this.voltageWaveform.put("raw", "");
			this.currentWaveform.put("raw", "");
			this.combinedWaveform.put("raw", Hex.encodeHexString(combinedWaveformBytes).toUpperCase());
		} else {
			this.voltageWaveform.put("raw", Hex.encodeHexString(voltageWaveformBytes).toUpperCase());
			this.currentWaveform.put("raw", Hex.encodeHexString(currentWaveformBytes).toUpperCase());
			this.combinedWaveform.put("raw", "");
		}

		// Set the feeder and phase
		this.feederNumber = feeder;
		this.phaseNumber = phase;
	}

	/**
	 * Returns the date time as unixtime
	 * 
	 * @return the date time as unixtime
	 */
	public long getDatetimeAsUnixtime() {
		// Return the timestamp as unixtime
		return unixtime;
	}

	/**
	 * Returns the date time as a human readable string
	 * 
	 * @return the date time as a human readable string
	 */
	public String getDatetimeAsHumanReadable() {
		return humantime;
	}

	/**
	 * Returns the date time as Gridtime
	 * 
	 * @return the date time as Gridtime
	 */
	public String getDatetimeAsGridtime() {
		return datetime;
	}

	/**
	 * Returns the instance number associated with the waveform captures
	 * 
	 * @return the instance number associated with the waveform captures
	 */
	public int getInstanceNumber() {
		return instanceNumber;
	}

	/**
	 * Returns the voltage waveform as a hex string
	 * 
	 * @return the voltage waveform as a hex string
	 */
	public String getRawVoltaveWaveformHexString() {
		// Convert the waveform to a Hex String and return it
		return voltageWaveform.containsKey("raw") ? voltageWaveform.get("raw") : "";
	}

	/**
	 * Returns the current waveform as a hex string
	 * 
	 * @return the current waveform as a hex string
	 */
	public String getRawCurrentWaveformHexString() {
		// Convert the waveform to a Hex String and return it
		return currentWaveform.containsKey("raw") ? currentWaveform.get("raw") : "";
	}

	/**
	 * Returns the voltage waveform as a hex string
	 * 
	 * @return the voltage waveform as a hex string
	 */
	public String getConditionedVoltaveWaveformHexString() {
		// Convert the waveform to a Hex String and return it
		return voltageWaveform.containsKey("conditioned") ? voltageWaveform.get("conditioned") : "";
	}

	/**
	 * Returns the current waveform as a hex string
	 * 
	 * @return the current waveform as a hex string
	 */
	public String getConditionedCurrentWaveformHexString() {
		// Convert the waveform to a Hex String and return it
		return currentWaveform.containsKey("conditioned") ? currentWaveform.get("conditioned") : "";
	}

	/**
	 * Returns the combined current and voltage waveform as a hex string
	 * 
	 * @return the combined current and voltage waveform as a hex string
	 */
	public String getCombinedWaveformHexString() {
		// Convert the waveform to a Hex String and return it
		return combinedWaveform.get("raw");
	}

	/**
	 * Returns the feeder number associated with the waveform set
	 * 
	 * @return the feeder number associated with the waveform set
	 */
	public int getFeederNumber() {
		return feederNumber;
	}

	/**
	 * Returns the phase number associated with the waveform set
	 * 
	 * @return the phase number associated with the waveform set
	 */
	public int getPhaseNumber() {
		return phaseNumber;
	}

	/**
	 * Returns whether the waveform is from the active DTF board rather than MK2 MCU
	 * 
	 * @return the whether the waveform is from the active DTF board rather than MK2
	 *         MCU
	 */
	public boolean getIsActiveBoardWaveform() {
		return isActiveBoardWaveform;
	}
}
