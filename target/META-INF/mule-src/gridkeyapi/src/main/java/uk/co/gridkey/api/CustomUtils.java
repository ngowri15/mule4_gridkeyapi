package uk.co.gridkey.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CustomUtils {

	/**
	 * Read Excel File into Java List Objects
	 * 
	 * @param filePath
	 * @return
	 */
	public List<Map<String, String>> readExcelFile(String filePath) {
		try {
			FileInputStream excelFile = new FileInputStream(new File(filePath));
			Workbook workbook = new XSSFWorkbook(excelFile);

			Sheet sheet = workbook.getSheet("Sheet1");
			Iterator<?> rows = sheet.iterator();

			List<Map<String, String>> listSummaryData = new ArrayList<Map<String, String>>();
			int rowNumber = 0;
			int feedercnt = 0, totalFeederCnt = 5, tmpTotalEvents = 0;
			String tmp = "", substationName = "", feeder1num = "", feeder2num = "", feeder3num = "", feeder4num = "",
					feeder5num = "";
			while (rows.hasNext()) {
				Row currentRow = (Row) rows.next();

				// skip header
				if (rowNumber == 0) {
					rowNumber++;
					continue;
				}

				int cellIndex = 0;

				Iterator<?> cellsInRow = currentRow.iterator();

				while (cellsInRow.hasNext()) {
					Cell currentCell = (Cell) cellsInRow.next();

					if (cellIndex == 0) { // Substation name
						tmp = currentCell.getStringCellValue();
						if (!"".contentEquals(tmp))
							substationName = tmp;
						feedercnt++;
					} else if (cellIndex == 2) { // number of events per feeder
						tmpTotalEvents = tmpTotalEvents + (int) currentCell.getNumericCellValue();
						if (feedercnt == 1)
							feeder1num = Integer.toString((int) currentCell.getNumericCellValue());
						if (feedercnt == 2)
							feeder2num = Integer.toString((int) currentCell.getNumericCellValue());
						if (feedercnt == 3)
							feeder3num = Integer.toString((int) currentCell.getNumericCellValue());
						if (feedercnt == 4)
							feeder4num = Integer.toString((int) currentCell.getNumericCellValue());
						if (feedercnt == 5)
							feeder5num = Integer.toString((int) currentCell.getNumericCellValue());
					}
					cellIndex++;

				}
				if (feedercnt == totalFeederCnt) {
					Map<String, String> tempMap = new HashMap<String, String>();
					tempMap.put("substation-name", substationName);
					tempMap.put("total-events", Integer.toString(tmpTotalEvents));
					tempMap.put("feeder1", feeder1num);
					tempMap.put("feeder2", feeder2num);
					tempMap.put("feeder3", feeder3num);
					tempMap.put("feeder4", feeder4num);
					tempMap.put("feeder5", feeder5num);
					listSummaryData.add(tempMap);
					feedercnt = 0;
					tmpTotalEvents = 0;
				}

			}

			excelFile.close();

			// Close WorkBook
			// workbook.close();

			return listSummaryData;
		} catch (IOException e) {
			throw new RuntimeException("FAIL! -> message = " + e.getMessage());
		}
	}

	public Map<String, String> filterSummaryBySubstation(String substationName, List<Map<String, String>> summaryData) {

		// For each item in the received data map, extract the row key and insert the
		// data into the "data" array nodes
		for (Map<String, String> mcusummary : summaryData) {
			// Loop through each pair in the Row Key
			for (Map.Entry<String, String> mapEntry : mcusummary.entrySet()) {
				if (mapEntry.getKey().equals("substation-name")) {
					if (mapEntry.getValue().equals(substationName)) {
						return mcusummary;
					}
				}
			}
		}
		return null;

	}

	public Map<String, String> filterMCUDetailWithSubstationName(List<Map<String, String>> mcuDetails) {

		Map<String, String> tempMap = new HashMap<String, String>();

		// For each item in the received data map, extract the row key and insert the
		// data into the "data" array nodes
		for (Map<String, String> mcuDtls : mcuDetails) {
			tempMap.put(mcuDtls.get("description"), mcuDtls.get("unit-sn"));
		}
		return tempMap;
	}

}
