package business;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class ExcelReader {
	
	public void readExcelFile(String file) throws IOException{
		FileInputStream input = new FileInputStream(file);
        POIFSFileSystem fs = new POIFSFileSystem( input );
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);

        // Iterate over each row in the sheet
        Iterator rows = sheet.rowIterator(); 
        while( rows.hasNext() ) {           
            HSSFRow row = (HSSFRow) rows.next();
            
            

            // Iterate over each cell in the row and print out the cell's content
            Iterator cells = row.cellIterator();
            while( cells.hasNext() ) {
                HSSFCell cell = (HSSFCell) cells.next();
                
                switch ( cell.getCellType() ) {
                    case HSSFCell.CELL_TYPE_NUMERIC:
                       
                        break;
                    case HSSFCell.CELL_TYPE_STRING: 
                        
                        break;
                    default:
                        
                        break;
                }
            }
		
	}
	
	
	}
}
