package cn.heknd.scanner;

import java.util.*;
import java.io.*;

public class SNLScanner {

	public List<Token> getToken(String input)
	{
		WordScanner ws = new WordScanner(input);
		Vector<Token> tokenList = new Vector<Token>();
		Token token = ws.getNextWord();
		while(token!=null)
		{
			tokenList.add(token);
			token=ws.getNextWord();
		}
		tokenList.add(new Token(Words.EOF,"End of File",ws.line));
		return tokenList;
	}
}