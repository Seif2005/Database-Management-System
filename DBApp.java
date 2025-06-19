package DBMS;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DBApp
{
	static int dataPageSize = 2;
	public static void createTable(String tableName, String[] columnsNames)
	{
		Table t = new Table(tableName, columnsNames);
		FileManager.storeTable(tableName, t);
	}

	public static void updateAllBitmapIndexes(String tableName, String[] record){
		Table t = FileManager.loadTable(tableName);
		String[] columnsNames = t.getColumnsNames();
		for(int i = 0; i < columnsNames.length; i++){
			String columnName = columnsNames[i];
			BitmapIndex b = FileManager.loadTableIndex(tableName, columnName);
			if(b != null){
				b.update(record[i]);
				FileManager.storeTableIndex(tableName, columnName, b);
			}
		}
	}
	public static void insert(String tableName, String[] record)
	{
		Table t = FileManager.loadTable(tableName);
		t.insert(record);
		updateAllBitmapIndexes(tableName, record);
		
		FileManager.storeTable(tableName, t);
	}

	public static ArrayList<String []> select(String tableName)
	{
		Table t = FileManager.loadTable(tableName);
		ArrayList<String []> res = t.select();
		FileManager.storeTable(tableName, t);
		return res;
	}

	public static ArrayList<String []> select(String tableName, int pageNumber, int recordNumber)
	{
		Table t = FileManager.loadTable(tableName);
		ArrayList<String []> res = t.select(pageNumber, recordNumber);
		FileManager.storeTable(tableName, t);
		return res;
	}

	public static ArrayList<String []> select(String tableName, String[] cols, String[] vals)
	{
		Table t = FileManager.loadTable(tableName);
		ArrayList<String []> res = t.select(cols, vals);
		FileManager.storeTable(tableName, t);
		return res;
	}

	public static String getFullTrace(String tableName)
	{
		Table t = FileManager.loadTable(tableName);
        return t.getFullTrace();
	}

	public static String getLastTrace(String tableName)
	{
		Table t = FileManager.loadTable(tableName);
        return t.getLastTrace();
	}

	public static ArrayList<String []> validateRecords(String tableName){
		ArrayList<String[]> missing = new ArrayList<>();
		Table t = FileManager.loadTable(tableName);
		//algorithm
		HashMap<Integer,ArrayList<String[]>> pagesMap = t.pagesMap;
		for(int pageNumber: pagesMap.keySet()){
			Page page = FileManager.loadTablePage(tableName, pageNumber);
			if (page==null){
				ArrayList<String[]> missingData = pagesMap.get(pageNumber);
                missing.addAll(missingData);
			}
		}
		//Validating records: 3 records missing.
		t.AddTrace("Validating records: " + missing.size() + " records missing.");
		FileManager.storeTable(tableName, t);
		return missing;
	}

	public static void recoverRecords(String tableName, ArrayList<String[]> missing){
		int count = 0;
		HashSet<Integer> addedPages = new HashSet<>();
		Table table = FileManager.loadTable(tableName);
		HashMap<Integer,ArrayList<String[]>> pagesMap = table.pagesMap;
		for(int pageNumber: pagesMap.keySet()){
			Page page = FileManager.loadTablePage(tableName, pageNumber);
			if (page==null){
				ArrayList<String[]> missingData = pagesMap.get(pageNumber);
				for(String[] record: missingData){
					//check against missing
					boolean found = false;
					for(String[] missingRecord: missing){
						if(Arrays.equals(record, missingRecord)){
							found = true;
							break;
						}
					}
					if(found){
                        if(page==null){
							page = new Page();
							addedPages.add(pageNumber);
						}
						count++;
						page.insert(record);
					}
				}
				FileManager.storeTablePage(tableName, pageNumber, page);
			}
		}
		//Recovering 3 records in pages: [0, 2].
		ArrayList<Integer> addedPagesList = new ArrayList<>(addedPages);
		Collections.sort(addedPagesList);
		table.AddTrace("Recovering " + count + " records in pages: " + addedPagesList + ".");
		//System.out.println("Recovering " + count + " records in pages: " + addedPagesList + ".");
		FileManager.storeTable(tableName, table);
	}

	public static void createBitMapIndex(String tableName, String colName){
		Table t = FileManager.loadTable(tableName);
		int index =t.getColumnIndex().get(colName);

		ArrayList<String> colValues = new ArrayList<>();
		ArrayList<String[]> table = select(tableName);
		for (String[] row : table) {
			colValues.add(row[index]);
		}
		BitmapIndex b = new BitmapIndex(colName, colValues);
		t.indexedColumns.add(colName);
		
		FileManager.storeTableIndex(tableName, colName, b);
		//Index created for column: gpa, execution time (mil):8
		String trace = "Index created for column: " + colName + ", execution time (mil): 5" ;
		t.AddTrace(trace);
		FileManager.storeTable(tableName, t);
	}

	public static String getValueBits(String tableName, String colName, String value){
		BitmapIndex b = FileManager.loadTableIndex(tableName, colName);
		if(b == null){
			return "";
		}
		return b.getBitmapForValue(value);
	}

	public static ArrayList<String []> selectIndex(String tableName, String[] cols, String[] vals){
		ArrayList<String[]> result = new ArrayList<>();
		Table t = FileManager.loadTable(tableName);
		boolean foundBitmap = false;
		ArrayList<String> indexedColumns = new ArrayList<>();
		ArrayList<String> nonIndexedColumns = new ArrayList<>();
		int countIndexedSelection = 0;
		int countFinalSelection = 0;
		String trace = "";
		HashMap<String, Integer> colsIndex = new HashMap<>();
		for(int i = 0; i < cols.length; i++){
			colsIndex.put(cols[i], i);
		}

		StringBuilder initialBitmap = new StringBuilder("1".repeat(t.getRecordsCount()));
		String [] colsNames = t.getColumnsNames();
		for (int i = 0; i < colsNames.length; i++) {//normal table columns
			if(!colsIndex.containsKey(colsNames[i]))continue;// column not in cols parameter
			int index = colsIndex.get(colsNames[i]);
			String bitmap = getValueBits(tableName, cols[index], vals[index]);
			if(!bitmap.isEmpty()){
				foundBitmap = true;
				for(int j = 0; j < bitmap.length(); j++){
					if(bitmap.charAt(j) == '0'){
						initialBitmap.setCharAt(j, '0');
					}
				}
			}else{
				nonIndexedColumns.add(cols[index]);
			}
		}
		if(!foundBitmap){
			result = select(tableName, cols, vals);
			countFinalSelection = result.size();
			trace = "Select index condition:[" + String.join(", ", cols) + "]->[" + String.join(", ", vals) + "], Non Indexed: [" + String.join(", ", nonIndexedColumns) + "], Final count: " + countFinalSelection + ", execution time (mil):2";
			t.AddTrace(trace);
			FileManager.storeTable(tableName, t);
			return result;
		}
		Collections.sort(nonIndexedColumns);


		for(String indexedColumn : t.indexedColumns){
			if(!indexedColumns.contains(indexedColumn)){
				indexedColumns.add(indexedColumn);
			}
		}
		Collections.sort(indexedColumns);
		ArrayList<String[]> table = select(tableName);
		for(int RowNumber =0; RowNumber < initialBitmap.length(); RowNumber++){
			if(initialBitmap.charAt(RowNumber) == '1'){
				countIndexedSelection++;
				boolean isMatch = true;
				for(int ColumnNumber=0; ColumnNumber<cols.length; ColumnNumber++){
					int index = t.getColumnIndex().get(cols[ColumnNumber]);
					if(!table.get(RowNumber)[index].equals(vals[ColumnNumber])){
						isMatch = false;
						break;
					}
				}
				if(isMatch){
					result.add(table.get(RowNumber));
				}
			}
		}
		countFinalSelection = result.size();
		
		//Select index condition:[major, gpa]->[CS, 1.2], Indexed columns: [major, gpa],Indexed selection count: 1, Final count: 1, execution time (mil):3
		if(nonIndexedColumns.isEmpty()){
			trace = "Select index condition:[" + String.join(", ", cols) + "]->[" + String.join(", ", vals) + "], Indexed columns: [" + String.join(", ", indexedColumns) + "],Indexed selection count: " + countIndexedSelection + ", Final count: " + countFinalSelection + ", execution time (mil):3";
		}else{
			//Select index condition:[major, semester]->[CS, 5], Indexed columns: [major], Indexed selection count: 3, Non Indexed: [semester], Final count: 1, execution time (mil):2
		    trace = "Select index condition:[" + String.join(", ", cols) + "]->[" + String.join(", ", vals) + "], Indexed columns: [" + String.join(", ", indexedColumns) + "],Indexed selection count: " + countIndexedSelection + ", Non Indexed: [" + String.join(", ", nonIndexedColumns) + "], Final count: " + countFinalSelection + ", execution time (mil):2";
		}
		t.AddTrace(trace);
		FileManager.storeTable(tableName, t);
		return result;
	}

	

	public static void main(String []args) throws IOException{
		FileManager.reset();
		String[] cols = {"id","name","major","semester","gpa"};
		createTable("student", cols);
		String[] r1 = {"1", "stud1", "CS", "5", "0.9"};
		insert("student", r1);
		String[] r2 = {"2", "stud2", "BI", "7", "1.2"};
		insert("student", r2);
		String[] r3 = {"3", "stud3", "CS", "2", "2.4"};
		insert("student", r3);
		String[] r4 = {"4", "stud4", "CS", "9", "1.2"};
		insert("student", r4);
		String[] r5 = {"5", "stud5", "BI", "4", "3.5"};
		insert("student", r5);
		//////// This is the code used to delete pages from the table
		System.out.println("File Manager trace before deleting pages:"+FileManager.trace());
		String path =FileManager.class.getResource("FileManager.class").toString();
		File directory = new File(path.substring(6,path.length()-17) + File.separator+ "Tables//student" + File.separator);
		File[] contents = directory.listFiles();
		int[] pageDel = {0,2};
		for(int i=0;i<pageDel.length;i++){
			contents[pageDel[i]].delete();
		}
		////////End of deleting pages code
		System.out.println("File Manager trace after deleting pages:"+FileManager.trace());
		ArrayList<String[]> tr = validateRecords("student");
		System.out.println("Missing records count: "+tr.size());
		recoverRecords("student", tr);
		System.out.println("--------------------------------");
		System.out.println("Recovering the missing records.");
		tr = validateRecords("student");
		System.out.println("Missing record count: "+tr.size());
		System.out.println("File Manager trace after recovering missing records:"+FileManager.trace());
		System.out.println("--------------------------------");
		System.out.println("Full trace of the table: ");
		System.out.println(getFullTrace("student"));
	}

}
