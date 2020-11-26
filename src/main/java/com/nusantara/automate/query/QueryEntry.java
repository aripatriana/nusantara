package com.nusantara.automate.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import org.springframework.util.StringUtils;

import com.nusantara.automate.Statement;
import com.nusantara.automate.WebExchange;
import com.nusantara.automate.util.MapUtils;
import com.nusantara.automate.util.ReflectionUtils;

public class QueryEntry {

	public static final String ROUND_BRACKET = "()";
	public static final String SQUARE_BRACKET = "[]";
	public static final String[] BRACKETS = new String[] {ROUND_BRACKET,SQUARE_BRACKET};
	
	private String query;
	private Map<String, Statement> statements = new LinkedHashMap<String, Statement>();
	private List<String> columns = new LinkedList<String>();
	private List<String> parameters = new ArrayList<String>();
	
	public QueryEntry() {
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void addColumn(String column) {
//		int counter = 0;
//		String columnIndex = column;
//		
//		while (MapUtils.findEquals(columns, columnIndex) != null) {
//			counter++;
//			columnIndex=column +"#"+counter;
//		}
//		
		columns.add(column);
	}
	public void addStatement(String var1, String var2, String equality) {
		if (var1 != null && equality != null)
			statements.put(var1, new Statement(var1, var2, equality));
		else
			statements.put(var1, null);
	}
	
	public List<String> getColumns() {
		return columns;
	}
	
//	public List<String> getColumns() {
//		List<String> columns = new LinkedList<String>();
//		for (String column : statements.keySet()) {
//			column = com.nusantara.automate.util.StringUtils.removeLastChar(column, "#");
//			columns.add(column);
//		}
//		return columns;
//	}
	
	public Map<String, Statement> getStatements() {
		Map<String, Statement> temp = new LinkedHashMap<String, Statement>();
		MapUtils.copyValueNotNull(statements, temp);
		return temp;
	}
	
	public Map<String, Statement> getStatements(int index) {
		Map<String, Statement> temp = new LinkedHashMap<String, Statement>();
		MapUtils.copyValueNotNull(statements, temp);
		Map<String, Statement> result = new LinkedHashMap<String, Statement>();
		
		for (Entry<String, Statement> entry : temp.entrySet()) {
			String key = entry.getKey();
			Statement statement = new Statement(entry.getValue());
			
			if (statement.getArg1().contains(ROUND_BRACKET)) {
				statement.setArg1(statement.getArg1().replace(ROUND_BRACKET, "[" +  index + "]"));
			}
			if (statement.getArg2().contains(ROUND_BRACKET)) {
				statement.setArg2(statement.getArg2().replace(ROUND_BRACKET, "[" +  index + "]"));
			}
			if (key.contains(ROUND_BRACKET)) {
				key = key.replace(ROUND_BRACKET, "[" + index + "]");
			}
			result.put(key, statement);
		}
		return result;
	}
	
	public void setStatements(Map<String, Statement> statements) {
		this.statements = statements;
	}
	
	public void addParameter(String parameter) {
		parameters.add(parameter);
	}
	
	public List<String> getParameters() {
		return parameters;
	}
	
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}
	
	@SuppressWarnings("unchecked")
	public String[] getParsedQuery(WebExchange webExchange) throws Exception {
		String query = getQuery();
		String[] parsedQuery = new String[] {query};
		Map<String, List<String>> rounded = new HashMap<String, List<String>>();
		Map<String, List<String>> squared = new HashMap<String, List<String>>();
		for (String p : getParameters()) {
			if (p.contains(ROUND_BRACKET)) {
				String[] s = p.split("\\(\\)");
				if (s.length > 2)
					throw new Exception("Not valid argument");
				List<String> r = rounded.get(s[0]);
				if (r == null) r = new ArrayList<String>();
				if (s.length == 2)
					r.add(s[1].replace(".","").trim());
				rounded.put(s[0], r);
			} else if(p.contains(SQUARE_BRACKET)) {
				String[] s = p.split("\\" + SQUARE_BRACKET);
				if (s.length > 2)
					throw new Exception("Not valid argument");
				List<String> r = squared.get(s[0]);
				if (r == null) r = new ArrayList<String>();
				if (s.length == 2)
					r.add(s[1].replace(".","").trim());
				squared.put(s[0], r);
			} else {
				query = query.replace(p, StringUtils.quote(webExchange.get(p).toString()));
			}
		}
		
		
		// []
		if (!squared.isEmpty()) {
			for (Entry<String, List<String>> e : squared.entrySet()) {
				Object o = webExchange.get(e.getKey()+SQUARE_BRACKET);
				if (o != null && !ReflectionUtils.checkAssignableFrom(o.getClass(), List.class))
					throw new Exception("Argument value is not a list");
				
				if (o != null) {
					List<Object> l = (List<Object>) o;
					if (e.getValue() != null && !e.getValue().isEmpty()) {
						for (String v : e.getValue()) {
							List<Object> values = MapUtils.mapAsList((List<Map<String, Object>>) (List<?>)l, v);
							query = query.replace(e.getKey()+SQUARE_BRACKET + "." + v, MapUtils.listAsString(values, ","));
						}
					} else {
						query = query.replace(e.getKey()+SQUARE_BRACKET, MapUtils.listAsString(l, ","));
					}
				}
			}
			parsedQuery[0] = query;
		}
		
		// ()
		if (!rounded.isEmpty()) {
			int[] size = new int[rounded.size()];
			int i = 0;

			// get size
			for (String k : rounded.keySet()) {
				Object o = webExchange.get(k+SQUARE_BRACKET);
				if (o != null && !ReflectionUtils.checkAssignableFrom(o.getClass(), List.class))
					throw new Exception("Argument value is not a list");
				if (o != null) {
					List<Object> l = (List<Object>) o;
					size[i] = l.size();
				}
				i++;
			}
			
			// compare size
			if (i > 1) {
				int total = IntStream.of(size).sum();
				int average = total / i;
				if (average != size[0])
					throw new Exception("Size of argument is not match");
			}
			
			parsedQuery = new String[size[0]];
			
			for (int j=0; j<size[0]; j++) {
				String tempQuery = query;
				for (Entry<String, List<String>> e : rounded.entrySet()) {
					if (e.getValue() != null && !e.getValue().isEmpty()) {
						for (String s : e.getValue()) {
							tempQuery = tempQuery.replace(e.getKey()+ROUND_BRACKET + "." + s, StringUtils.quote(webExchange.get(e.getKey()+"[" + j + "]" + "." + s).toString()));
						}
					} else {
						MapUtils.removeEquals(getParameters(), e.getKey()+ROUND_BRACKET);
						tempQuery = tempQuery.replace(e.getKey()+ROUND_BRACKET, StringUtils.quote(webExchange.get(e.getKey()+"[" + j + "]").toString()));
					}
				}
				parsedQuery[j] = tempQuery;
			}
		}
		
		return parsedQuery;
	}
}
