/**
 * Copyright (c) 2013-2015, Jieven. All rights reserved.
 *
 * Licensed under the GPL license: http://www.gnu.org/licenses/gpl.txt
 * To use it on other terms please contact us at 1623736450@qq.com
 */
package com.oss.player;

import java.util.List;

import com.eova.template.single.SingleIntercept;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

public class PlayerIntercept extends SingleIntercept {

	@Override
	public void importBefore(Controller ctrl, List<Record> records) throws Exception {
		for (Record record : records) {
			if (record.getStr("status").equals("3")) {
				throw new Exception("数据状态异常");
			}
		}
	}

}