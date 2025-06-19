package DBMS;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.Serializable;

public class BitmapIndex implements Serializable{

    private String columnName;
    private HashMap<String, StringBuilder> index; 
    public int numberOfElements;

    public BitmapIndex(String columnName, ArrayList<String> columnData) {
        this.columnName = columnName;
        this.index = new HashMap<>();
        this.numberOfElements = columnData.size();
        

        // Build bitmap index
        for (int i = 0; i < this.numberOfElements; i++) {
            String value = columnData.get(i);
            if (!index.containsKey(value)) {
                StringBuilder sb = new StringBuilder("0".repeat(this.numberOfElements));
                index.put(value, sb);
            }
        }    
            
        for (int i = 0; i < this.numberOfElements; i++) {
            String value = columnData.get(i);
            index.get(value).setCharAt(i, '1');
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public HashMap<String, StringBuilder> getIndex() {
        return index;
    }

    public String getBitmapForValue(String value) {
        return index.getOrDefault(value, new StringBuilder("0".repeat(this.numberOfElements))).toString();
    }

    public void update(String value){
        if(!index.containsKey(value)){
            index.put(value, new StringBuilder("0".repeat(this.numberOfElements)));
        }
        for(String key : index.keySet()){
            if(key.equals(value)){
                index.get(key).append("1");
            }else{
                index.get(key).append("0");
            }
        }
        this.numberOfElements++;
    }

}
