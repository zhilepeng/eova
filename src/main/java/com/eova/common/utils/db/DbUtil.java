package com.eova.common.utils.db;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eova.common.utils.xx;
import com.eova.template.common.config.TemplateConfig;
import com.jfinal.plugin.activerecord.Record;

public class DbUtil {

	/**
	 * 转换Oracle数据类型
	 * 
	 * @param typeName DB数据类型
	 * @return
	 */
	private static String convertDataType(String typeName) {
		if (typeName.contains("INT") || typeName.contains("BIT")) {
			return "NUMBER";
		} else if (typeName.indexOf("TIME") != -1) {
			return TemplateConfig.DATATYPE_TIME;
		} else {
			return "VARCHAR2";
		}
	}

	public static void createOracleSql() {
		
		StringBuilder sbs = new StringBuilder();
		StringBuilder sbDrop = new StringBuilder();
		
		String ds = xx.DS_EOVA;
		List<String> tables = DsUtil.getTableNamesByConfigName(xx.DS_EOVA, DsUtil.TABLE);
		// List<String> tables = new ArrayList<String>();
		// tables.add("eova_item");
		for (String table : tables) {

			String drop = "drop table " + table + ";\n";
			sbDrop.append(drop);

			JSONArray list = DsUtil.getColumnInfoByConfigName(ds, table);

			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			StringBuilder sb3 = new StringBuilder();

			sb.append("create table " + table);
			sb.append("(\n");

			for (int i = 0; i < list.size(); i++) {
	            JSONObject o = list.getJSONObject(i);
	            
	            Record re = new Record();
				re.set("en", o.getString("COLUMN_NAME"));
				re.set("cn", o.getString("REMARKS"));
	            re.set("indexNum", o.getIntValue("ORDINAL_POSITION"));
				re.set("isNotNull", "YES".equalsIgnoreCase(o.getString("IS_NULLABLE")) ? true : false);

				// 是否自增
				boolean isAuto = "YES".equalsIgnoreCase(o.getString("IS_AUTOINCREMENT")) ? true : false;
				re.set("isAuto", isAuto);
				// 字段类型
				String typeName = o.getString("TYPE_NAME");
				re.set("dataType", convertDataType(typeName));
				// 字段长度
				int size = o.getIntValue("COLUMN_SIZE");
				if(size == 0){
					size = 1;
				}
				// 默认值
				String def = o.getString("COLUMN_DEF");
				re.set("valueExp", def);

				// create table
				sb.append("    " + re.getStr("en") + " " + re.getStr("dataType") + "(" + size + ")");
				if (re.getBoolean("isNotNull")) {
					sb.append(" NOT NULL");
				}
				sb.append(",\n");

				// create remarks
				String remarks = o.getString("REMARKS");
				if (!xx.isEmpty(remarks)) {
					String str = "comment on column %s.%s is '%s';\n";
					sb2.append(String.format(str, table, re.getStr("en"), remarks));
				}

				// add default
				{
					if (!xx.isEmpty(def)) {
						String str = "alter table %s modify %s default %s;\n";
						sb3.append(String.format(str, table, re.getStr("en"), xx.format(def)));
					}

				}

	        }
			sb.delete(sb.length() - 2, sb.length() - 1);
			sb.append(");\n");
			
			// 导入元字段
			// importMetaField(code, list);

			// 导入视图默认第一列为主键
			String pkName = DsUtil.getPkName(ds, table);
			if (!xx.isEmpty(pkName)) {
				String str = "\nalter table %s add constraint pk_%s primary key(%s);\n";
				sb2.insert(0, String.format(str, table, table, pkName));
			}

			// 导入元对象
			// importMetaObject(ds, type, table, name, code, pkName);

			sbs.append(sb);
			sbs.append(sb2);
			sbs.append(sb3);
			sbs.append("\n");
		}

		System.out.println(sbDrop.toString());
		System.out.println(sbs.toString());
	}
}