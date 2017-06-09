package com.ofss.fc.junit.commonservice.test.questionnaire;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.ofss.fc.app.commonservice.core.dto.questionnaire.answer.BooleanAnswerValueDTO;
import com.ofss.fc.app.commonservice.core.dto.questionnaire.answer.DateAnswerValueDTO;
import com.ofss.fc.app.commonservice.core.dto.questionnaire.answer.DoubleAnswerValueDTO;
import com.ofss.fc.app.commonservice.core.dto.questionnaire.answer.IntegerAnswerValueDTO;
import com.ofss.fc.app.commonservice.core.dto.questionnaire.answer.MoneyAnswerValueDTO;
import com.ofss.fc.app.commonservice.core.dto.questionnaire.answer.TextAnswerValueDTO;
import com.ofss.fc.datatype.Date;
import com.ofss.fc.datatype.dto.MoneyDTO;
import com.ofss.fc.enumeration.commonservice.QuestionnaireOperatorType;
import com.ofss.fc.enumeration.commonservice.QuestionnaireOperatorType;
import com.ofss.fc.junit.commonservice.test.questionnaire.Lexer.Token;
import com.ofss.fc.junit.commonservice.test.questionnaire.Lexer.TokenType;
import com.ofss.fc.junit.commonservice.test.questionnaire.condition.ASTNode;
import com.ofss.fc.junit.commonservice.test.questionnaire.condition.ArithmeticExpression;
import com.ofss.fc.junit.commonservice.test.questionnaire.condition.FactOperand;
import com.ofss.fc.junit.commonservice.test.questionnaire.condition.LiteralOperand;
import com.ofss.fc.junit.commonservice.test.questionnaire.condition.LogicalExpression;
import com.ofss.fc.junit.commonservice.test.questionnaire.condition.RelationalExpression;

public class OperatorPrecedenceParser {
	
	public static final String SEPARATOR = " ";
	public static final String COMMA = ",";
	public static final String LPAREN = "(";
	public static final String RPAREN = ")";

	public OperatorPrecedenceParser() {
		
	}
	
	public static ASTNode parseAsAST(List<Token> tokens){
		
		ASTNode root = null;
		StringBuilder rpnString = new StringBuilder();
		String tokenValue = "";
		int arity;
		Stack<String> operatorStack = new Stack<String>();
		Stack<ASTNode> nodeStack = new Stack<ASTNode>();
		Stack<Integer> variableArityStack = new Stack<Integer>(); 
		
		Set<TokenType> operators = Stream.of(TokenType.LOGICOP, TokenType.NOT, TokenType.RELOP, TokenType.ARITHOP)
										 .collect(Collectors.toCollection(HashSet::new));
		
		for(Token token: tokens){
			tokenValue = token.getValue();
			//OPERATOR(Pop till stack contains op of <= priority. Push)
			if(operators.contains(token.getTokenType())){
				QuestionnaireOperatorType operator  = QuestionnaireOperatorType.fromSymbol(tokenValue.toLowerCase());
				while (!operatorStack.isEmpty()){
					QuestionnaireOperatorType peekOperator  = QuestionnaireOperatorType.fromSymbol(operatorStack.peek());
					if(peekOperator!=null && operator.preceeds(peekOperator) < 0 || (operator.isLeftAssociative() && operator.preceeds(peekOperator) == 0)){
						rpnString.append(SEPARATOR).append(operatorStack.pop());
						nodeStack.push(generateExpression(peekOperator, nodeStack, variableArityStack));
						
					}
					else break;
					
				}
				operatorStack.push(tokenValue.toLowerCase());
			}
			//COMMA(Reduce till LPAREN|COMMA)
			else if(TokenType.COMMA.equals(token.getTokenType())){
				while(!(LPAREN.equals(operatorStack.peek()) || COMMA.equals(operatorStack.peek()))){
					String operator = operatorStack.pop();
					rpnString.append(SEPARATOR).append(operator);
					nodeStack.push(generateExpression(QuestionnaireOperatorType.fromSymbol(operator), nodeStack, variableArityStack));
				}
				if(LPAREN.equals(operatorStack.peek())){
					variableArityStack.push(1);
				}
				else if(COMMA.equals(operatorStack.peek())){
					variableArityStack.push(variableArityStack.pop() + 1);
				}
				operatorStack.push(tokenValue);
			}
			//LPAREN(Push)
			else if(TokenType.LPAREN.equals(token.getTokenType()) || TokenType.LSQUARE.equals(token.getTokenType())){
				operatorStack.push(tokenValue);
			}
			
			//RPAREN(Reduce till LPAREN)
			else if(TokenType.RPAREN.equals(token.getTokenType())){
				while(!LPAREN.equals(operatorStack.peek())){
					String operator = operatorStack.pop();
					rpnString.append(SEPARATOR).append(operator);
					if(!COMMA.equals(operator)){
						nodeStack.push(generateExpression(QuestionnaireOperatorType.fromSymbol(operator), nodeStack, variableArityStack));
					}
				}
				operatorStack.pop();//pop opening parentheses
			}
			//SYMBOL(Push)
			else{
				rpnString.append(SEPARATOR).append(tokenValue);
				nodeStack.push(generateOperand(token));
			}
		}
		//EOS(Reduce till stack empty)
		while(!operatorStack.isEmpty()){
			String operator = operatorStack.pop();
			rpnString.append(SEPARATOR).append(operator);
			nodeStack.push(generateExpression(QuestionnaireOperatorType.fromSymbol(operator), nodeStack, variableArityStack));
		}
//		System.out.println("Post-fix notation: " + rpnString.substring(1).toString() + "\n");
		return root = nodeStack.pop();
	}
	

	/**
	 * Pop the arity of the operator from node stack
	 * generate expresssion
	 * Push expression onto the node stack. 
	 */
	private static ASTNode generateExpression(QuestionnaireOperatorType operatorGrammar, Stack<ASTNode> nodeStack, Stack<Integer> variableArityStack) {
		ASTNode expression = null;
		LinkedList<ASTNode> nodes = new LinkedList<ASTNode>();
		
		int arity = operatorGrammar.getArity();
		arity += operatorGrammar.isVariableArity()? variableArityStack.pop():0;
		
		IntStream.range(0, arity).forEach(i-> nodes.addFirst(nodeStack.pop()));
		QuestionnaireOperatorType operator = (QuestionnaireOperatorType)operatorGrammar;
		switch((QuestionnaireOperatorType)operator){
		case ADD:
		case SUBTRACT:
		case MULTIPLY:
		case DIVIDE:
			expression = new ArithmeticExpression(operator,nodes.toArray(new ASTNode[nodes.size()]));
			break;
		case AND:
		case OR:
		case NOT:
			expression = new LogicalExpression(operator,nodes.toArray(new ASTNode[nodes.size()]));
			break;
		case EQUAL:
		case NOT_EQUAL:
		case LESS:
		case LESS_EQUAL:
		case GREATER:
		case GREATER_EQUAL:
		case BETWEEN:
		case CONTAINS:
		case CONTAINS_ONLY:
		case NOT_CONTAINS:
		case IN:
		case NOT_IN:
			expression = new RelationalExpression(operator,nodes.toArray(new ASTNode[nodes.size()]));
			break;
		}
		return expression;
	}

	private static ASTNode generateOperand(Token token) {
		ASTNode node = null;
		switch(token.getTokenType()){
		case FACT:
			node = new FactOperand(token.getValue());
			break;
		case STRING:
			node = new LiteralOperand(new TextAnswerValueDTO(token.getValue().substring(1, token.getValue().length()-1)));
			break;
		case INTEGER:
			node = new LiteralOperand(new IntegerAnswerValueDTO(Integer.parseInt(token.getValue())));
			break;
		case DOUBLE:
			node = new LiteralOperand(new DoubleAnswerValueDTO(Double.parseDouble(token.getValue())));
			break;
		case MONEY:
			MoneyDTO money = new MoneyDTO();
			money.setAmount(new BigDecimal(token.getValue()));
			node = new LiteralOperand(new MoneyAnswerValueDTO(money));
			break;
		case DATE:
			node = new LiteralOperand(new DateAnswerValueDTO(new Date(token.getValue())));
			break;
		case BOOLEAN:
			node = new LiteralOperand(new BooleanAnswerValueDTO(Boolean.parseBoolean(token.getValue())));
			break;
		}
		return node;
	}

}
