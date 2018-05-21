package com.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.logger.MonitorLogger;

/**
 * A simple program that writes data to an Excel file with some formats
 * for cells.
 * @author www.codejava.net
 *
 */
public class ExcelWriter {
	public void writeExcel(List<Book> listBook, String excelFilePath) throws IOException {
		@SuppressWarnings("resource")
		Workbook workbook = new HSSFWorkbook();
		
		Sheet sheet = workbook.createSheet();
		createHeaderRow(sheet);
		
		int rowCount = 1;
		
		for (Book aBook : listBook) {
			Row row = sheet.createRow(rowCount++);
			writeBook(aBook, row);
		}
		
		try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
			workbook.write(outputStream);
		}		
	}
	
	
	
	private void createHeaderRow(Sheet sheet) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		Font font = sheet.getWorkbook().createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);
		cellStyle.setFont(font);
				 
		Row row = sheet.createRow(0);
		
		Cell cellTitle = row.createCell(0);
		cellTitle.setCellStyle(cellStyle);
		cellTitle.setCellValue("Title");
		
		Cell cellAuthor = row.createCell(1);
		cellAuthor.setCellStyle(cellStyle);
		cellAuthor.setCellValue("Author");
		
		Cell cellPrice = row.createCell(2);
		cellPrice.setCellStyle(cellStyle);
		cellPrice.setCellValue("Price");
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
	
	public static void main(String[] args) throws IOException {
		ExcelWriter excelWriter = new ExcelWriter();
		List<Book> listBook = excelWriter.getListBook();
		String excelFilePath = "FormattedJavaBooks.xls";
		excelWriter.writeExcel(listBook, excelFilePath);
	}

	public void writeExcel(List<String> header, Map<Integer, ArrayList<String>> valueMap,String fileName) throws IOException {
		@SuppressWarnings("resource")
		Workbook workbook = new HSSFWorkbook();
		
		Sheet sheet = workbook.createSheet();
		createHeaderRow(sheet, header);
		
		int rowCount = 1;
		
		for (Map.Entry<Integer, ArrayList<String>> entry : valueMap.entrySet()){
			Row row = sheet.createRow(rowCount++);
			writeBook(row, entry.getValue());
		}
		
		try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
			workbook.write(outputStream);
		}
	}

	private void writeBook(Row row, ArrayList<String> values) {
		int x=0;
		for (String value : values) {
			Cell cell = row.createCell(x++);
			cell.setCellType(CellType.NUMERIC);
			if(StringUtils.isNumeric(value)){
				cell.setCellValue(Long.parseLong(value));
			}else{
				cell.setCellValue(value);
			}
		}		
	}

	private void createHeaderRow(Sheet sheet, List<String> header) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		Font font = sheet.getWorkbook().createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);
		cellStyle.setFont(font);
		cellStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());
		Row row = sheet.createRow(0);
		for (int i = 0;i<header.size();i++){
			Cell cellTitle = row.createCell(i);
			cellTitle.setCellStyle(cellStyle);
			cellTitle.setCellValue(header.get(i));
		}
	}

	private void createHeaderRow(Sheet sheet, String[] rowData) {
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		Font font = sheet.getWorkbook().createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 10);
		cellStyle.setFont(font);
		cellStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());
		Row row = sheet.createRow(0); 
		for (int i = 0;i<rowData.length;i++){
			Cell cellTitle = row.createCell(i);
			cellTitle.setCellStyle(cellStyle);
			cellTitle.setCellValue(rowData[i]);
		}
	}
	
	public void writeToExcel(ArrayList<String[]> data) {
		@SuppressWarnings("resource")
		Workbook workbook = new HSSFWorkbook();
		int i=0;
		Sheet sheet = workbook.createSheet();
		for (String[] rowData : data) {
			if(i==0){
				createHeaderRow(sheet, rowData);
				i++;
			}else{
				Row row = sheet.createRow(i++);
				writeBook(row, rowData);
			}
			try (FileOutputStream outputStream = new FileOutputStream("Reports/PLSQLAnalyzer.xls")) {
				workbook.write(outputStream);
			} catch (IOException e) {
				MonitorLogger.error(ExcelWriter.class.getName(),"Error While Generating PLSQL analyzer excel ", e);
			}
		}
	}

	private void writeBook(Row row, String[] rowData) {
		int x=0;
		for (String value : rowData) {
			Cell cell = row.createCell(x++);
			cell.setCellType(CellType.NUMERIC);
			if(StringUtils.isNumeric(value)){
				cell.setCellValue(Long.parseLong(value));
			}else{
				cell.setCellValue(value);
			}
		}		
	}
}
