package com.github.tools.poi;

/**
 * 测试对象
 * 
 * @author jiangyf
 * @date 2017年9月18日 下午8:43:14
 */
public class Dog {
	@ExcelColumn(name = "ID", isExport = true)
	private Integer id;
	@ExcelColumn(name = "名称", isExport = true)
	private String name;
	@ExcelColumn(name = "备注")
	private String remark;

	public Dog(Integer id, String name, String remark) {
		this.id = id;
		this.name = name;
		this.remark = remark;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
