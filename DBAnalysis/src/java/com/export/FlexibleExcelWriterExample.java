package com.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * A flexible program that writes data to an Excel file in either
 * XLSX or XLS format, depending on the extension of the file. 
 * @author www.codejava.net
 *
 */
public class FlexibleExcelWriterExample {

	public void writeExcel(List<Book> listBook, String excelFilePath) throws IOException {
		Workbook workbook = getWorkbook(excelFilePath);
		Sheet sheet = workbook.createSheet();
		
		int rowCount = 0;
		
		for (Book aBook : listBook) {
			Row row = sheet.createRow(rowCount++);
			writeBook(aBook, row);
		}
		
		try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
			workbook.write(outputStream);
		}		
	}
	
	private void writeBook(Book aBook, Row row) {
		Cell cell = row.createCell(0);
		cell.setCellValue(aBook.getTitle());

		cell = row.createCell(1);
		cell.setCellValue(aBook.getAuthor());
		
		cell = row.createCell(2);
		cell.setCellValue(aBook.getPrice());
	}
	
	private List<Book> getListBook() {
		Book book1 = new Book("Head First Java", "Kathy Serria", 79);
		Book book2 = new Book("Effective Java", "Joshua Bloch", 36);
		Book book3 = new Book("Clean Code", "Robert Martin", 42);
		Book book4 = new Book("Thinking in Java", "Bruce Eckel", 35);
		
		List<Book> listBook = Arrays.asList(book1, book2, book3, book4);
		
		return listBook;
	}
	
	private Workbook getWorkbook(String excelFilePath) 
			throws IOException {
		Workbook workbook = null;
		
		if (excelFilePath.endsWith("xlsx")) {
			workbook = new XSSFWorkbook();
		} else if (excelFilePath.endsWith("xls")) {
			workbook = new HSSFWorkbook();
		} else {
			throw new IllegalArgumentException("The specified file is not Excel file");
		}
		
		return workbook;
	}
	
	public static void main(String[] args) throws IOException {
		FlexibleExcelWriterExample excelWriter = new FlexibleExcelWriterExample();
		List<Book> listBook = excelWriter.getListBook();
		String excelFilePath = "JavaBooks1.xls";
		excelWriter.writeExcel(listBook, excelFilePath);
		
		/*excelFilePath = "JavaBooks2.xlsx";
		excelWriter.writeExcel(listBook, excelFilePath);*/
	}

}
