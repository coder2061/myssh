package com.github.tools.poi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Excel操作工具类
 * 
 * 要求：POI_3.8+，Excel_2003+
 * 
 * @author jiangyf
 * @date 2017年9月18日 下午6:26:23
 */
public class ExcelUtil<T> implements Serializable {
	private static final long serialVersionUID = -5963601668245436768L;

	/**
	 * 内存中保留 n 条数据，以免内存溢出，其余写入硬盘
	 */
	private static final int ROW_ACCESS_WINDOW_SIZE = 1000;
	/**
	 * 每个工作表存放最大记录数
	 */
	public static final int SHEET_MAX_ROWS = 50000;
	/**
	 * 默认工作表名
	 */
	public static final String DEFAULT_SHEET_NAME = "Sheet";
	/**
	 * 单元格默认列宽度
	 */
	private static final int DEFAULT_COLUMN_WIDTH = 15;
	/**
	 * 声明数据类
	 */
	private Class<T> clazz;

	public ExcelUtil(Class<T> clazz) {
		this.clazz = clazz;
	}

	/**
	 * 创建工作簿(支持同类型数据分工作簿展示)
	 *
	 * @param dataList
	 *            导出数据
	 * @param sheetName
	 *            工作表名（传入null，表示使用默认工作表名）
	 * @return
	 */
	public Workbook createWorkbook(List<T> dataList, String fileName, String sheetName) throws Exception {
		// 根据是03之前还是03之后的版本创建不同的工作簿
		Workbook workbook = null;
		if (fileName.endsWith(".xls")) {
			workbook = new HSSFWorkbook();
		} else if (fileName.endsWith(".xlsx")) {
			workbook = new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE);
		} else {
			throw new Exception("文件名或文件格式错误");
		}
		// 工作表名
		sheetName = (sheetName == null || sheetName.trim().length() == 0) ? DEFAULT_SHEET_NAME : sheetName;
		// 总记录数
		int totalRows = dataList != null && dataList.size() >= 0 ? dataList.size() : 0;
		// 工作表页数
		double sheetNum = getSheetNum(totalRows);
		// 获取数据所有属性
		List<Field> fields = getObjectFields();
		// 初始化工作表
		for (int sheetNo = 0; sheetNo < sheetNum; sheetNo++) {
			// 创建工作簿
			Sheet sheet = workbook.createSheet();
			// 设置工作表名
			workbook.setSheetName(sheetNo, sheetName + (sheetNo + 1));
			// 设置表格默认列宽度
			sheet.setDefaultColumnWidth(DEFAULT_COLUMN_WIDTH);
			// 标题行样式
			// CellStyle titleStyle = generateTitleStyle(workbook);
			// 数据行样式
			// CellStyle dataStyle = generateDataStyle(workbook);
			// 写入表头到Excel
			writeHeaderToExcel(sheet, sheetNo, fields);
			// 写入数据到Excel
			writeDataToExcel(sheet, sheetNo, dataList, totalRows, fields);
		}
		return workbook;
	}

	/**
	 * 写入数据到Excel
	 * 
	 * @param sheet
	 *            工作表
	 * @param sheetNo
	 *            第几个工作表
	 * @param dataList
	 *            数据集
	 * @param totalRows
	 *            总记录数
	 * @param fields
	 *            数据属性集
	 * @throws Exception
	 */
	private void writeDataToExcel(Sheet sheet, int sheetNo, List<T> dataList, int totalRows,
			List<Field> fields) throws Exception {
		// 创建内容列
		int startNo = sheetNo * SHEET_MAX_ROWS;
		int endNo = Math.min(startNo + SHEET_MAX_ROWS, totalRows);
		for (int j = startNo; j < endNo; j++) {
			Row row = sheet.createRow(j + 1 - startNo);
			// 得到导出对象.
			T obj = (T) dataList.get(j);
			for (int k = 0; k < fields.size(); k++) {
				// 获得field
				Field field = fields.get(k);
				// 设置实体类私有属性可访问
				field.setAccessible(true);
				ExcelColumn attr = field.getAnnotation(ExcelColumn.class);
				int col = k;
				// 根据指定的顺序获得列号
				if (StringUtils.isNotBlank(attr.column())) {
					col = getExcelCol(attr.column());
				}
				// 创建单元格
				Cell cell = row.createCell(col);
				// 如果数据存在就填入,不存在填入空格
				Class<?> classType = (Class<?>) field.getType();
				String value = null;
				if (field.get(obj) != null && classType.isAssignableFrom(Date.class)) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					value = sdf.format(sdf.parse(String.valueOf(field.get(obj))));
				}
				// 设置数据行样式
				// cell.setCellStyle(dataStyle);
				cell.setCellValue(
						field.get(obj) == null ? "" : value == null ? String.valueOf(field.get(obj)) : value);
			}
		}
	}

	/**
	 * 写入表头到Excel
	 * 
	 * @param sheet
	 *            工作表
	 * @param sheetNo
	 *            第几个工作表
	 * @param fields
	 *            数据属性集
	 */
	private void writeHeaderToExcel(Sheet sheet, int sheetNo, List<Field> fields) {
		// 创建工作表第一行，存放表头
		Row row = sheet.createRow(0);
		for (int cellNum = 0; cellNum < fields.size(); cellNum++) {
			// 获取属性
			Field field = fields.get(cellNum);
			// 获取注解信息
			ExcelColumn attr = field.getAnnotation(ExcelColumn.class);
			// 根据指定的顺序获得列号
			int col = cellNum;
			if (StringUtils.isNotBlank(attr.column())) {
				col = getExcelCol(attr.column());
			}
			// 创建列
			Cell cell = row.createCell(col);
			// 设置列宽
			sheet.setColumnWidth(sheetNo,
					(int) ((attr.name().getBytes().length <= 4 ? 6 : attr.name().getBytes().length) * 1.5
							* 256));
			// 设置列中写入内容为String类型
			cell.setCellType(Cell.CELL_TYPE_STRING);
			// 写入列名
			cell.setCellValue(attr.name());
			// 如果设置了提示信息则鼠标放上去提示
			// if (StringUtils.isNotBlank(attr.prompt())) {
			// setHSSFPrompt(sheet, "", attr.prompt(), 1, 100, col,
			// col);
			// }
			// 如果设置了combo属性则本列只能选择不能输入
			// if (attr.combo().length > 0) {
			// setHSSFValidation(sheet, attr.combo(), 1, 100, col, col);
			// }
		}
	}

	/**
	 * 生成并设置标题行样式
	 * 
	 * @param workbook
	 * @return
	 */
	@SuppressWarnings("unused")
	private CellStyle generateTitleStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setFillBackgroundColor(HSSFColor.SKY_BLUE.index); // 填充的背景颜色
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);// 填充图案
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);// 设置边框样式
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);// 顶边框
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中
		style.setFont(generateTitleFont(workbook));// 字体样式
		return style;
	}

	/**
	 * 生成并设置数据行样式
	 * 
	 * @param workbook
	 * @return
	 */
	@SuppressWarnings("unused")
	private CellStyle generateDataStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		style.setFont(generateDataFont(workbook));
		return style;
	}

	/**
	 * 生成并设置标题行字体
	 * 
	 * @param workbook
	 * @return
	 */
	private Font generateTitleFont(Workbook workbook) {
		Font font = workbook.createFont();
		font.setColor(HSSFColor.VIOLET.index);// 字体颜色
		font.setFontHeightInPoints((short) 12);// 字号
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 加粗
		return font;
	}

	/**
	 * 生成并设置数据行字体
	 * 
	 * @param workbook
	 * @return
	 */
	private Font generateDataFont(Workbook workbook) {
		Font font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
		return font;
	}

	/**
	 * 将Excel中A,B,C,D,E列映射成0,1,2,3
	 *
	 * @param col
	 *            大写字母列名
	 * @return int 数字列名
	 */
	public int getExcelCol(String col) {
		col = col.toUpperCase();
		// 从-1开始计算，字母从1开始运算。这种总数下来算数正好相同。
		int count = -1;
		char[] cs = col.toCharArray();
		for (int i = 0; i < cs.length; i++) {
			count += (cs[i] - 64) * Math.pow(26, cs.length - 1 - i);
		}
		return count;
	}

	/**
	 * 获取数据类的所有属性
	 * 
	 * @return
	 */
	private List<Field> getObjectFields() {
		// 得到所有定义字段
		Field[] allFields = clazz.getDeclaredFields();
		List<Field> fields = new ArrayList<Field>();
		// 得到所有field并存放到一个list中
		for (Field field : allFields) {
			if (field.isAnnotationPresent(ExcelColumn.class)) {
				fields.add(field);
			}
		}
		return fields;
	}

	/**
	 * 获取工作表数量
	 * 
	 * @param totalRows
	 *            总记录数
	 * @return
	 */
	private int getSheetNum(int totalRows) {
		if (totalRows == 0) {
			return 1;
		}
		int count = totalRows / SHEET_MAX_ROWS;
		int remain = totalRows % SHEET_MAX_ROWS;
		if (remain > 0) {
			return ++count;
		}
		return count;
	}

	/**
	 * 写入Excel到HTTP响应
	 * 
	 * @param dataList
	 *            数据集
	 * @param fileName
	 *            文件名
	 * @param sheetName
	 *            工作表名（传入null，表示使用默认工作表名）
	 * @param response
	 *            响应对象
	 * @throws Exception
	 */
	public void writeToHttpResponse(List<T> dataList, String fileName, String sheetName,
			HttpServletResponse response) throws Exception {
		Workbook workbook = createWorkbook(dataList, fileName, sheetName);
		writeToHttpResponse(workbook, fileName, response);
	}

	/**
	 * 写入Excel到HTTP响应
	 * 
	 * @param workbook
	 *            工作簿
	 * @param fileName
	 *            文件名
	 * @param response
	 *            响应对象
	 * @throws Exception
	 */
	public void writeToHttpResponse(Workbook workbook, String fileName, HttpServletResponse response)
			throws Exception {
		// 以流形式下载文件
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		// 把创建好的workbook以字节流的形式写进缓冲中
		workbook.write(os);
		// 读取os字节数组
		InputStream is = new ByteArrayInputStream(os.toByteArray());
		// 设置response弹出下载框
		// 清空response
		response.reset();
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		response.setHeader("Content-Disposition",
				"attachment;fileName=" + new String(fileName.getBytes(), "iso-8859-1"));
		OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
		BufferedInputStream bis = new BufferedInputStream(is);
		// 获取缓冲数据
		byte[] buffer = new byte[bis.available()];
		int bytesRead;
		while (-1 != (bytesRead = bis.read(buffer, 0, buffer.length))) {
			toClient.write(buffer, 0, bytesRead);
		}
		toClient.flush();
		bis.close();
		toClient.close();
	}

	/**
	 * 写入Excel到指定文件地址
	 * 
	 * @param dataList
	 *            数据集
	 * @param fileName
	 *            文件名
	 * @param sheetName
	 *            工作表名
	 * @param path
	 *            文件地址
	 */
	public void writeToFilePath(List<T> dataList, String fileName, String sheetName, String path) {
		File file = new File(path + fileName);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			createWorkbook(dataList, fileName, sheetName).write(fos);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		List<Dog> list = new ArrayList<>();
		list.add(new Dog(1, "a", "1"));
		list.add(new Dog(2, "b", "2"));
		list.add(new Dog(3, "c", "3"));
		list.add(new Dog(4, "d", "4"));
		list.add(new Dog(5, "e", "5"));
		new ExcelUtil<>(Dog.class).writeToFilePath(list, "a.xls", "dog", "D:\\");
	}

}
