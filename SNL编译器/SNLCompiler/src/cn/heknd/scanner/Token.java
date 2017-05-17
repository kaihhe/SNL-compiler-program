package cn.heknd.scanner;


public class Token
{
	//token列表
	//类型
	//内容
	//行号
	public Words type;
	public String context;
	Token(){}
	public int line;
	Token(Words type, String context)
	{
		this.type = type;
		this.context = context;
	}
	Token(Words type, String context,int line)
	{
		this.type = type;
		this.context = context;
		this.line = line;
	}
}