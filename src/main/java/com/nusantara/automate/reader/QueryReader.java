package com.nusantara.automate.reader;

import java.io.File;

import com.nusantara.automate.Statement;
import com.nusantara.automate.exception.ScriptInvalidException;
import com.nusantara.automate.query.QueryEntry;
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
		boolean marked = false;
		int start = 0;
		int bracket = 0;
		for (int i=0; i<query.length(); i++) {
			if (query.charAt(i) == '@') {
				marked=true;
				start=i;
			} else if (marked && query.charAt(i) == '(') {
				bracket++;
			} else if (marked && bracket > 0 && query.charAt(i) == ')') {
				bracket--;
			} else if (marked && bracket == 0 && query.charAt(i) == ')') {
				qe.addParameter(query.substring(start, i));
				marked=false;
			} else if (marked && bracket == 0 && (query.charAt(i) == ' ')) {
				qe.addParameter(query.substring(start, i));
				marked=false;
			} else if (marked && (i == query.length()-1)) {
				qe.addParameter(query.substring(start, query.length()));
				marked=false;
			}
		}
	}
	
	public static void main(String[] args) {
		QueryReader r = new QueryReader("select instructionId<>@elementId from gen_tx_instruction where instructionId=@web.element columnId=@session.data");
		try {
			QueryEntry qe = r.read();
			System.out.println(qe);
		} catch (ScriptInvalidException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String parseSelect(QueryEntry qe, String query) throws ScriptInvalidException {
		query = query.replace(SELECT, "");
		String[] headers = query.split(",");
		String returnQuery = "";
		for (String header : headers) {
			boolean marked = false;
			int index = 0;
			for (int j=0; j<Statement.MARK.length; j++) {
				if (header.contains(Statement.MARK[j])) {
					marked = true;
					index=j;
				}
			}
			
			if (!returnQuery.isEmpty() && !returnQuery.isBlank()) 
				returnQuery += ",";
			
			if (marked) {
				String[] sh = StringUtils.parseStatement(header.trim(), Statement.MARK[index]);
				String var1 = checkColumnPrefix(sh[0]);
				String var2 = checkColumnPrefix(sh[1]);
				String columnQuery = "";
				if (!var1.startsWith("@") && !var1.startsWith("'"))
					columnQuery = var1;
				if (!var2.startsWith("@") && !var2.startsWith("'"))
					columnQuery = var2;				
				qe.addStatement(var1, var2, Statement.MARK[index]);				
				returnQuery += columnQuery + " ";
			} else {
				qe.addStatement(header.trim(), null, null);
				returnQuery += header.trim() + " ";
			}
			
		}
		return SELECT + " " + returnQuery;
	}
	
	private String checkColumnPrefix(String var) {
		if (var.startsWith("@") || var.startsWith("'"))
			return var;
		
		String[] c = var.split(".");
		if (c.length> 1) {
			var = c[1];
		}
		return var;
	}
	
	public String[] splitQuery(String query) {
		if (!query.startsWith(SELECT)) 
			return new String[] {query};
		
		String[] strs = query.trim().split("[\n\t ]+");
		String[] result = new String[] {"", ""};
		if (strs.length > 0) {
			int count=0;
			while(!strs[count].trim().equals(FROM)) {
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
