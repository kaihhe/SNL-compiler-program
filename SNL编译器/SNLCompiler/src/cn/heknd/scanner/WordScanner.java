package cn.heknd.scanner;

public class WordScanner {
	//单词扫描器的状态信息
	enum State
	{
		START,
		INASSIGN,
		INCOMMENT,
		INNUM,
		INID,
		INCHAR,
		INRANGE,
		DONE
	}
	//保留字
	static String[] reservedWords = 
		{
		"program","type","var","procedure","begin","end","array","of","record","if","then",
		"else","fi","while","do","endwh","read","write","return","integer","char"
	};
	State cs;
	String source;
	int next;
	int line;
	WordScanner(String source)
	{
		this.source = source;
		cs = State.START;
		next=0;
		line=1;
	}
	//getWord()
	public Token getNextWord()
	{
		this.cs = State.START;
		Token result = null;
		if(this.source==null)
			return result;
		while(true)
			switch(this.cs)
			{
			case START:
				if(source.length()<=next+1)
				{
					if(source.length()==next+1&&source.charAt(next)=='.')
					{
						next++;
						return new Token(Words.DOT,".",line);
					}
					else
						return null;
				}
				//过滤空白字符
				while(source.charAt(next)==' '||source.charAt(next)=='\n'||source.charAt(next)=='\r')
				{
					if(source.charAt(next)=='\n')
						line++;
					next++;
					if(next>source.length()-1)
						return null;
				}
				if(isLetter(source.charAt(next)))
					this.cs = State.INID;
				else if(isNumber(source.charAt(next)))
					this.cs = State.INNUM;
				else
				{
					switch(source.charAt(next++))
					{
					case '+':
						return result = new Token(Words.ADD,"+",line);
					case '-':
						return result = new Token(Words.SUB,"-",line);
					case '*':
						return result = new Token(Words.MUL,"*",line);
					case '/':
						return result = new Token(Words.DIV,"/",line);
					case '<':
						return result = new Token(Words.LESS,"<",line);
					case '=':
						return result = new Token(Words.EQUAL,"=",line);
					case '(':
						return result = new Token(Words.LEFT_PARENT,"(",line);
					case ')':
						return result = new Token(Words.RIGHT_PARENT,")",line);
					case '[':
						return result = new Token(Words.LEFT_BRACKET,"[",line);
					case ']':
						return result = new Token(Words.RIGHT_BRACKET,"]",line);
					case ';':
						return result = new Token(Words.SEMICOLON,";",line);
					case ',':
						return result = new Token(Words.COMMA,",",line);
					case '\'':
						this.cs = State.INCHAR;
						break;
					case '.':
						if(source.charAt(next)!='.')
						{
							return new Token(Words.DOT,".",line);
						}
						else
						{
							next++;
							return new Token(Words.TWO_DOT,"..",line);
						}
					case ':':
						this.cs = State.INASSIGN;
						break;
					case '{':
						this.cs = State.INCOMMENT;
						break;
					}
				}
				break;
			case INASSIGN:
				if(source.charAt(next++)=='=')
					return result = new Token(Words.COLON_EQUAL,":=",line);
				else
					return result = new Token(Words.SEMICOLON,":",line);
			case INCOMMENT:
				while(source.charAt(next)!='}'&&next<source.length())
					{
						if(source.charAt(next)=='\n')
							line++;
						next++;
					};
				this.cs=State.START;
				break;
			case INNUM:
				String num = "";
				num += source.charAt(next++);
				while(isNumber(source.charAt(next)))
				{
					num += source.charAt(next++);
				}
				return result = new Token(Words.UNSIGNEDNUMBER,num,line);
			case INID:
				String id = "";
				id += source.charAt(next++);
				while(next<source.length()&&(isLetter(source.charAt(next))||isNumber(source.charAt(next))))
				{
					id += source.charAt(next++);
				}
				Token tmp = getReservedToken(id);
				if(tmp!=null)
				{
					tmp.line=line;
					return tmp;
				}
				return result = new Token(Words.IDENTIFIERS,id,line);
			case INCHAR:
				if(isLetter(source.charAt(next))||isNumber(source.charAt(next)))
					return result = new Token(Words.CHAR,""+source.charAt(next++),line);
			case INRANGE:
				next++;
				this.cs=State.START;
				break;
			case DONE:
				return null;
			}
	}
	//工具函数
	//isLetter()
	//isNumber()
	static boolean isLetter(int ch)
	{
		if((ch>='A'&&ch<='Z')||(ch>='a'&&ch<='z'))
			return true;
		return false;
	}
	static boolean isNumber(int ch)
	{
		if(ch>='0'&&ch<='9')
			return true;
		return false;
	}
	static Token getReservedToken(String input)
	{
		for(int i=0;i<reservedWords.length;i++)
		{
			if(input.equals(reservedWords[i])&&reservedWords[i].equals(input))
			{
				return new Token(Words.valueOf(input.toUpperCase()),input);
			}
		}
		return null;
	}
}
