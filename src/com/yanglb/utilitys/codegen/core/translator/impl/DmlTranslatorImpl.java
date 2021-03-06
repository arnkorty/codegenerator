/**
 * Copyright 2015 yanglb.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanglb.utilitys.codegen.core.translator.impl;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.yanglb.utilitys.codegen.core.model.DmlModel;
import com.yanglb.utilitys.codegen.core.model.TableModel;
import com.yanglb.utilitys.codegen.core.translator.BaseTranslator;
import com.yanglb.utilitys.codegen.exceptions.CodeGenException;
import com.yanglb.utilitys.codegen.support.SupportLang;
import com.yanglb.utilitys.codegen.utility.StringUtility;

public class DmlTranslatorImpl extends BaseTranslator<List<DmlModel>> {
	@Override
	protected void onBeforeTranslate() throws CodeGenException {
		super.onBeforeTranslate();
		this.writableModel.setExtension("dml");
		this.writableModel.setFilePath("dml");
		this.writableModel.setLang(SupportLang.sql);
		
		// 设置文件名（同Excel名）
		String fileName = this.model.get(0).getExcelFileName();
		File file = new File(fileName);
		fileName = file.getName();
		
		int index = fileName.lastIndexOf(".");
		if(index != -1) {
			fileName = fileName.substring(0, index);
		}
		this.writableModel.setFileName(fileName);
	}

	@Override
	protected void onTranslate() throws CodeGenException {
		super.onTranslate();		
		StringBuilder sb = this.writableModel.getData();
		
		// 添加文件头
		sb.append(this.settingMap.get("head"));
		
		// 逐个添加内容
		for(DmlModel itm : this.model) {
			sb.append(this.genDml(itm));
		}
	}

	/**
	 * 取得用于替换的Model
	 */
	@Override
	protected Object getReplaceModel() {
		if(this.model == null || this.model.size() <= 0) return null;
		
		// 只使用第一个Sheet替换
		return this.model.get(0);
	}

	private String genDml(DmlModel model) {
		StringBuilder sb = new StringBuilder();
		
		// 头部信息
		sb.append("\r\n");
		sb.append(String.format("-- %s\r\n", model.getSheetName()));
		sb.append(String.format("-- %s %s\r\n", model.getAuthor(), model.getRenewDate()));
		
		// 每一行dml语句
		String columns = "";
		String space = "";
		if(!StringUtility.isNullOrEmpty(model.getNameSpace()) && !"-".equals(model.getNameSpace())) {
			space = String.format("%s.", model.getNameSpace());
		}
		TableModel table = model.getDmlData();
		for(Map<String, String> itm : table.toList()) {
			// 如果模板为空则先生成模板
			if(StringUtility.isNullOrEmpty(columns)) {
				columns = this.genColumns(itm);
			}
			
			// 生成完整语句并添加到sb中
			sb.append(String.format("INSERT INTO %s%s(%s) VALUES(%s);\r\n", 
					space,
					model.getName(),
					columns, 
					this.genValues(itm)));
		}
		return sb.toString();
	}
	
	/**
	 * 生成dml的values部分
	 * @param data dml数据
	 * @return values
	 */
	private String genValues(Map<String, String> data) {
		String values = "";
		
		for(String col : data.values()) {
			if(!StringUtility.isNullOrEmpty(values)) {
				values += ", ";
			}
			values += String.format("'%s'", col);
		}
		return values;
	}

	/**
	 * 生成dml的column部分
	 * @param data dml数据
	 * @return 'col1', 'col2'
	 */
	private String genColumns(Map<String, String> data) {
		String columns = "";
		
		for(String col : data.keySet()) {
			if(!StringUtility.isNullOrEmpty(columns)) {
				columns += ", ";
			}
			columns += String.format("'%s'", col);
		}
		return columns;
	}
}
