package com.nusantara.automate.reader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.nusantara.automate.Statement;
import com.nusantara.automate.exception.ScriptInvalidException;
import com.nusantara.automate.query.QueryEntry;
import com.nusantara.automate.util.DataTypeUtils;
import com.nusantara.automate.util.StringUtils;

public class QueryReader {
	
	private final static String SELECT = "select";
	private final static String FROM = "from";
	
	private SimpleFileReader fileReader;
	private String selectQuery;
	
	public QueryReader(File file) {
		fileReader = new SimpleFileReader(file);
	}
	
	public QueryReader(String selectQuery) {
		this.selectQuery = selectQuery;
	}
	
	public QueryEntry read() throws ScriptInvalidException {
		if (fileReader != null) {
			StringBuffer sb = new StringBuffer();
			while(fileReader.iterate()) {
				sb.append(fileReader.read());
			}
			fileReader.close();
			selectQuery = sb.toString();
		} 
		if (selectQuery.trim().endsWith(";"))
			throw new ScriptInvalidException("Invalid character, semicolon not allowed in a query " + selectQuery);
		QueryEntry qe = new QueryEntry();
		String[] sq = splitQuery(selectQuery);
		String query  = sq[0];
		if (sq.length > 1)
			query = parseSelect(qe, sq[0]) + sq[1];
		parseParameter(qe, query);
		qe.setQuery(query);
		return qe;
	}
	
	public void parseParameter(QueryEntry qe, String query) {
		Map<String, String> temp = new HashMap<String, String>();
		query = StringUtils.replaceById(query, QueryEntry.BRACKETS, temp);

		for (int i=0; i<query.length(); i++) {
			if (query.charAt(i) == '@') {
				String param = StringUtils.substringUntil(query.substring(i, query.length()), new char[] {')',' ','\''});
				for (Entry<String, String> id : temp.entrySet()) {
					param = param.replace(id.getKey(), id.getValue());
				}
				qe.addParameter(param.trim());
			} 
		}
	}
	
	
	public static String parseSelect(QueryEntry qe, String query) throws ScriptInvalidException {
		if (query.contains("*"))
			throw new ScriptInvalidException("Not allowed using an asterisk in a select query for " + query);
		if (!query.startsWith(SELECT))
			throw new ScriptInvalidException("Missing select statement for " + query);
		if (StringUtils.containsCharFollowingBy(query, '=', '=') == -1)
			throw new ScriptInvalidException("Missing equation of equality == for " + query);
		if (StringUtils.containsCharFollowingBy(query, '<', '>') == -1)
			throw new ScriptInvalidException("Missing equation of inequality <> for " + query);
		if (StringUtils.containsCharBackwardFollowingBy(query, '>', '<') == -1)
			throw new ScriptInvalidException("Missing equation of inequality <> for " + query);
		
		int index = StringUtils.containsCharForward(query, ' ');
		query = query.substring(index, query.length());
		String[] headers = query.split(",");
		String returnQuery = "";
		for (String header : headers) {
			String mark = StringUtils.findContains(header, Statement.MARK);
			if (mark != null) {
				String[] sh = StringUtils.parseStatement(header.trim(), mark);
				String var1 = DataTypeUtils.checkColumnPrefix(sh[0]);
				String var2 = DataTypeUtils.checkColumnPrefix(sh[1]);
				if (DataTypeUtils.checkIsColumn(var1) && DataTypeUtils.checkIsColumn(var2)) {
					// two columns defined
					returnQuery = StringUtils.concatIfNotEmpty(returnQuery, ",");
					returnQuery += var1 + " ";
					qe.addStatement(var1, var2, mark);
					qe.addColumn(var1);
				} else if (DataTypeUtils.checkIsColumn(var1)) {
					// column defined in the left side
					returnQuery = StringUtils.concatIfNotEmpty(returnQuery, ",");
					returnQuery += var1 + " ";
					qe.addStatement(var1, var2, mark);
					qe.addColumn(var1);
				} else if (DataTypeUtils.checkIsColumn(var2)) {
					// column defined in the right side
					returnQuery = StringUtils.concatIfNotEmpty(returnQuery, ",");
					returnQuery += var2 + " ";
					qe.addStatement(var2, var1, mark);
					qe.addColumn(var2);
				} else {
					// no column defined
					qe.addStatement(var1, var2, mark);
				}
			} else {
				returnQuery = StringUtils.concatIfNotEmpty(returnQuery, ",");
				qe.addStatement(header.trim(), null, null);
				qe.addColumn(header.trim());
				returnQuery += header.trim() + " ";
			}
			
		}
		return SELECT + " " + returnQuery;
	}
	
	public String[] splitQuery(String query) {
		if (!query.startsWith(SELECT)) 
			return new String[] {query};
		
		String[] strs = query.trim().split("[\n\t ]+");
		String[] result = new String[] {"", ""};
		if (strs.length > 0) {
			int count=0;
			int c = 0;
			while((!strs[count].trim().startsWith(FROM)) || c>0) {
				for (int i=0; i<strs[count].length(); i++) {
					if (strs[count].charAt(i) == '(')
						c++;
					if ((strs[count].charAt(i) == ')'))
						c--;
				}
				result[0] += strs[count] + " ";
				count++;
			}
			while (count < strs.length) {
				result[1] += strs[count] + " ";
				count++;
			} 	
		}
		return result;
	}
}
