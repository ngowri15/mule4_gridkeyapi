package uk.co.gridkey.api;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class GetWaveformFiles {
	
	public Object onCall(Map<String, String> uriAttributes) throws Exception {

		Map<Object, Object> msg = new HashMap<Object, Object>();
		
		String dno = uriAttributes.get("dno");
		String mcu = uriAttributes.get("mcu");
		String date = uriAttributes.get("date");

		// Create an empty map to store the result in
		Map<String, byte[]> resultingWaveforms = new HashMap<>();
		LinkedHashSet<String> filetimeHashSet = new LinkedHashSet<String>();
		List<WaveformSet> listWaveformSet = new ArrayList<WaveformSet>();

		// Get the files in the specified dno/mcu directory
		File dir = null;
		if (System.getProperty("os.name").startsWith("Windows")) {
			// Assume Windows
			dir = new File("D:/Temp/waveform/" + dno + "/" + mcu);
		} else {
			// Assume Linux
			dir = new File("/data/ftp/waveform/" + dno + "/" + mcu);
		}

		// Apply the FileFilter to only find files with the specified legacy file
		// extension i.e. *.bin
		FileFilter fileFilter = new WildcardFileFilter(date + "*.bin", IOCase.INSENSITIVE);
		File[] files = dir.listFiles(fileFilter);

		// Ensure we have some files
		if (files != null) {
			// loop through each file which meets the filter
			for (File file : files) {
				// We'll need the datetime from the filename, we can get this by doing a split
				// on _
				// Add this to our linked hash set so we can see all of the unique date times
				filetimeHashSet.add(file.getName().split("_")[0]);

				// Read the bytes and convert them to a hex string representation
				byte[] fileBytes = Files.readAllBytes(file.toPath());

				// Put the waveform into the resultingWaveforms map with the filename
				resultingWaveforms.put(file.getName(), fileBytes);
			}

			// Loop through each individual datetime
			for (String datetime : filetimeHashSet) {
				Map<String, byte[]> tempMap = new HashMap<>();

				// Iterate over the hashmap and find any entries with our timestamp - This code
				// serves no purpose, remove?
				for (Map.Entry<String, byte[]> entry : resultingWaveforms.entrySet()) {
					if (entry.getKey().startsWith(datetime)) {
						// Add the entry to our temporary map
						tempMap.put(entry.getKey(), entry.getValue());
					}
				}

				// Get an iterator for our tempmap
				Iterator<Entry<String, byte[]>> tempmapIterator = tempMap.entrySet().iterator();

				// Continue to do this until our map is empty
				while (tempmapIterator.hasNext()) {
					// Get the entry
					Map.Entry<String, byte[]> mapElement = tempmapIterator.next();

					// Check if this is a voltage waveform
					if (mapElement.getKey().contains("_V")) {
						// The waveform filenames
						String voltageWaveformFilename = mapElement.getKey();
						String currentWaveformFilename = voltageWaveformFilename.replace("_V", "_I");

						// Check to see if we have a corresponding current waveform
						if (tempMap.containsKey(currentWaveformFilename)) {

							// The rest of the info needed to generate a waveform set
							int instanceNumber = Integer.parseInt(
									voltageWaveformFilename.substring(voltageWaveformFilename.indexOf("_V") + 2,
											voltageWaveformFilename.indexOf("_V") + 3));
							int feederNumber = Integer.parseInt(
									voltageWaveformFilename.substring(voltageWaveformFilename.indexOf("_F") + 2,
											voltageWaveformFilename.indexOf("_F") + 3));
							int phaseNumber = Integer.parseInt(
									voltageWaveformFilename.substring(voltageWaveformFilename.indexOf("_L") + 2,
											voltageWaveformFilename.indexOf("_L") + 3));
							byte[] voltageWaveform = mapElement.getValue();
							byte[] currentWaveform = tempMap.get(currentWaveformFilename);

							// Create a new waveform set and add it to our waveform set list - there is
							// no combined waveform for the legacy MK2 version
							listWaveformSet.add(new WaveformSet(datetime, instanceNumber, voltageWaveform,
									currentWaveform, null, feederNumber, phaseNumber, false));
						}
					}

				}
			}
		}

		// New format files with the active board have a different file extension, these
		// can now be
		// processed and added to the same overall waveform list. These files contain
		// waveforms
		// that are combined voltage and current in a single file.
		// Ensure we have some files
		fileFilter = new WildcardFileFilter(date + "*.awc", IOCase.INSENSITIVE);
		files = dir.listFiles(fileFilter);

		resultingWaveforms = new HashMap<>();
		filetimeHashSet = new LinkedHashSet<String>();

		if (files != null) {
			// loop through each file which meets the filter
			for (File file : files) {
				// We'll need the datetime from the filename, we can get this by doing a split
				// on '.'
				// Add this to our linked hash set so we can see all of the unique date times
				filetimeHashSet.add(file.getName().split("\\.")[0]);

				// Read the bytes and convert them to a hex string representation
				byte[] fileBytes = Files.readAllBytes(file.toPath());

				// Put the waveform into the resultingWaveforms map with the filename
				resultingWaveforms.put(file.getName(), fileBytes);
			}

			// Loop through each individual datetime
			for (String datetime : filetimeHashSet) {
				Map<String, byte[]> tempMap = new HashMap<>();

				// Iterate over the hashmap and find any entries with our timestamp - This code
				// serves no purpose, remove?
				for (Map.Entry<String, byte[]> entry : resultingWaveforms.entrySet()) {
					if (entry.getKey().startsWith(datetime)) {
						// Add the entry to our temporary map
						tempMap.put(entry.getKey(), entry.getValue());
					}
				}

				// Get an iterator for our tempmap
				Iterator<Entry<String, byte[]>> tempmapIterator = tempMap.entrySet().iterator();

				// Continue to do this until our map is empty
				while (tempmapIterator.hasNext()) {

					// Get the entry
					Map.Entry<String, byte[]> mapElement = tempmapIterator.next();

					// The waveform filenames
					String combinedWaveformFilename = mapElement.getKey();
					byte[] combinedWaveform = tempMap.get(combinedWaveformFilename);

					// The rest of the info needed to generate a waveform set
					int instanceNumber = 1; // Fixed as only once instance possible

					// Extract the feeder number from the waveform file content, taking into account
					// the offset along with the 66 byte KLV header present in the file as per the
					// ICD
					// document GK23000076 MCU318 DTF Module ICD v1.10
					int feederNumber = combinedWaveform[276] & 0xFF;

					// Extract the phase number from the waveform file content, taking into account
					// the offset along with the 66 byte KLV header present in the file as per the
					// ICD
					// document GK23000076 MCU318 DTF Module ICD v1.10
					int phaseNumber = combinedWaveform[277] & 0xFF;

					// Create a new waveform set and add it to our waveform set list
					listWaveformSet.add(new WaveformSet(datetime, instanceNumber, null, null, combinedWaveform,
							feederNumber, phaseNumber, true));
				}
			}
		}

		msg.put("listWaveformSet", listWaveformSet);

		return msg;
	}
}