package com.ofss.fc.junit.commonservice.test.questionnaire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Lexer {
	
	public static List<Token> lex(String expression){
		List<Token> tokens = new ArrayList<Token>();
		String tokenPatterns = Arrays.stream(TokenType.values())
								     .map(t -> new StringBuilder("(?<").append(t.name()).append(">").append(t.getPattern()).append(")"))
								     .collect(Collectors.joining("|"));
		Pattern pattern = Pattern.compile(tokenPatterns);
		Matcher matcher = pattern.matcher(expression);
		while(matcher.find()){
			if(matcher.group(TokenType.WHITESPACE.name())!=null){
//				System.out.println("Whitespace matched" + matcher.group(TokenType.WHITESPACE.name()));
				continue;
			}
			else if(matcher.group(TokenType.FACT.name())!=null){
				tokens.add(new Token(TokenType.FACT, matcher.group(TokenType.FACT.name())));
			}
			else if(matcher.group(TokenType.DOUBLE.name())!=null){
				tokens.add(new Token(TokenType.DOUBLE, matcher.group(TokenType.DOUBLE.name())));
			}
			else if(matcher.group(TokenType.INTEGER.name())!=null){
				tokens.add(new Token(TokenType.INTEGER, matcher.group(TokenType.INTEGER.name())));
			}
			else if(matcher.group(TokenType.FACT.name())!=null){
				tokens.add(new Token(TokenType.FACT, matcher.group(TokenType.FACT.name())));
			}
			else if(matcher.group(TokenType.STRING.name())!=null){
				tokens.add(new Token(TokenType.STRING, matcher.group(TokenType.STRING.name())));
			}
			else if(matcher.group(TokenType.DATE.name())!=null){
				tokens.add(new Token(TokenType.DATE, matcher.group(TokenType.DATE.name())));
			}
			else if(matcher.group(TokenType.MONEY.name())!=null){
				tokens.add(new Token(TokenType.MONEY, matcher.group(TokenType.MONEY.name())));
			}
			else if(matcher.group(TokenType.BOOLEAN.name())!=null){
				tokens.add(new Token(TokenType.BOOLEAN, matcher.group(TokenType.BOOLEAN.name())));
			}
			else if(matcher.group(TokenType.LPAREN.name())!=null){
				tokens.add(new Token(TokenType.LPAREN, matcher.group(TokenType.LPAREN.name())));
			}
			else if(matcher.group(TokenType.RPAREN.name())!=null){
				tokens.add(new Token(TokenType.RPAREN, matcher.group(TokenType.RPAREN.name())));
			}
			else if(matcher.group(TokenType.LSQUARE.name())!=null){
				tokens.add(new Token(TokenType.LSQUARE, matcher.group(TokenType.LSQUARE.name())));
			}
			else if(matcher.group(TokenType.RSQUARE.name())!=null){
				tokens.add(new Token(TokenType.RSQUARE, matcher.group(TokenType.RSQUARE.name())));
			}
			else if(matcher.group(TokenType.COMMA.name())!=null){
				tokens.add(new Token(TokenType.COMMA, matcher.group(TokenType.COMMA.name())));
			}
			else if(matcher.group(TokenType.LOGICOP.name())!=null){
				tokens.add(new Token(TokenType.LOGICOP, matcher.group(TokenType.LOGICOP.name())));
			}
			else if(matcher.group(TokenType.NOT.name())!=null){
				tokens.add(new Token(TokenType.NOT, matcher.group(TokenType.NOT.name())));
			}
			else if(matcher.group(TokenType.RELOP.name())!=null){
				tokens.add(new Token(TokenType.RELOP, matcher.group(TokenType.RELOP.name())));
			}
//			else if(matcher.group(TokenType.STRINGOP.name())!=null){
//				tokens.add(new Token(TokenType.STRINGOP, matcher.group(TokenType.STRINGOP.name())));
//			}
			else if(matcher.group(TokenType.ARITHOP.name())!=null){
				tokens.add(new Token(TokenType.ARITHOP, matcher.group(TokenType.ARITHOP.name())));
			}
			
//			else if(matcher.group(TokenType.SET.name())!=null){
//				tokens.add(new Token(TokenType.SET, matcher.group(TokenType.SET.name())));
//			}
		}
		return tokens;
	}
	
	
	
	
	public static class Token {
		private TokenType tokenType;
		private String value;
		public TokenType getTokenType() {
			return tokenType;
		}
		public void setTokenType(TokenType tokenType) {
			this.tokenType = tokenType;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public Token(TokenType tokenType, String value) {
			super();
			this.tokenType = tokenType;
			this.value = value;
		}
	}
	
	public static enum TokenType{
		FACT("\\{[\\w\\d\\.]+\\}"), 
		STRING("\\'[\\w\\d\\s\\/]+\\'") , 
		INTEGER("-?[0-9]+(\\.\\d+)?"),
		DOUBLE("-?[0-9]+(\\.\\d+)?"),
		DATE("\\d{2}\\/\\/\\d{2}\\/\\/\\d{4}"),
		MONEY("AUD\\d+"),
		BOOLEAN("TRUE|FALSE"),
		WHITESPACE("[ \t\f\r\n]+"), 
		ARITHOP("[*|/|+|-]"), 
		LOGICOP("AND|OR|and|or"),
		NOT("!|NOT"),
		RELOP("<=|>=|=|!=|<|>|CONTAINS|IN|contains|in|between"),
		LPAREN("\\("), 
		RPAREN("\\)"), 
		LSQUARE("\\["),
		COMMA(","),
		RSQUARE("\\]"),;
		
		private String pattern;
		
		public String getPattern(){
			return pattern;
		}
		
		private TokenType(String pattern){
			this.pattern = pattern;
		}
	}
	
	

}
