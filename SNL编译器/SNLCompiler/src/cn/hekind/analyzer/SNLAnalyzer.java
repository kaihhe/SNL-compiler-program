package cn.hekind.analyzer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Vector;
import java.util.jar.Attributes.Name;

import cn.heknd.scanner.SNLScanner;
import cn.heknd.scanner.Token;
import cn.heknd.scanner.Words;

public class SNLAnalyzer {
	String message = "";				//报错信息
	Vector<Token> tokenVector = null;			//tokenList
	int next = 0;						//逻辑指针
	TreeNode tmpnode = null;
	public SNLAnalyzer(Vector<Token> tokenVector)
	{
		this.tokenVector = tokenVector;
	}
	public String getMessage()
	{
		return message;
	}
	private void writeWrongWords(Words word)
	{
		message += "line[" + tokenVector.get(next-1).line + "]:After \"" + tokenVector.get(next-1).context+"\" Should be \"" + word.toString() + "\"\n";
	}
	private void writeWrongString(String string)
	{
		message += "line[" + tokenVector.get(next-1).line + "]" +string+"\n";
	}
/****************************递归向下扫描*****************************/
	public TreeNode parse()
	{
		TreeNode root = program();
		if(this.tokenVector.lastElement().type==Words.EOF)
		{
			return root;
		}
		else
		{
			this.writeWrongWords(Words.EOF);
//			message += ""+this.tokenVector.get(next-1).line+":expect EOF at the end of file\n";
			return root;
		}
	}
	TreeNode program()
	{
		TreeNode head,part,body;
		TreeNode root = new TreeNode();
		root.nodeKind = TreeNode.NodeKind.PROC_K;
		
		//程序头
		head = programHead();
		if(head!=null)
			root.child[0]=head;
		else
			this.writeWrongString("Program Head Wrong");
//			message+=""+this.tokenVector.get(next-1).line+":program head wrong \n";

		//声明部分。
		while(tokenVector.get(next).type != Words.TYPE &&tokenVector.get(next).type != Words.VAR && tokenVector.get(next).type != Words.PROCEDURE&&tokenVector.get(next).type != Words.BEGIN)
		{
			if(next>=tokenVector.size()-1)
				return null;
			next++;
		}

		part = declarePart();
		if(part!=null)
			root.child[1]=part;
		else
			this.writeWrongString("Program Declare Wrong");
//			message+=""+this.tokenVector.get(next-1).line+":declare part wrong \n";

		//程序体
		while(tokenVector.get(next).type!=Words.BEGIN)
		{
			if(next>=tokenVector.size()-1)
				return null;
			next++;
		}
		body = programBody();
		if(body!=null)
			root.child[2]=body;
		else
			message+=""+this.tokenVector.get(next-1).line+":program body wrong \n";


		//匹配dot
		if(tokenVector.get(tokenVector.size()-2).type==Words.DOT)
			return root;
		else
		{
			message+=""+this.tokenVector.get(next-1).line+":expect a dot as the end of file \n";
			return root;
		}
	}
	TreeNode programHead()
	{
		Token cToken = tokenVector.get(next++);
		if(cToken.type==Words.PROGRAM)
		{
			TreeNode croot = new TreeNode();
			croot.nodeKind = TreeNode.NodeKind.PHEAD_K;
			cToken = tokenVector.get(next);
			if(cToken.type == Words.IDENTIFIERS)
			{
				croot.idString.add(cToken.context.toString());
				next++;
			}
			else
			{
				message+=""+cToken.line+"expect ID\n";
			}
			return croot;
		}
		message += ""+this.tokenVector.get(next-1).line+":expect \"program\"\n";
		next--;
		return null;
	}
	TreeNode declarePart()
	{
		TreeNode typeP = new TreeNode();
		typeP.nodeKind = TreeNode.NodeKind.TYPE_K;
		//确保type定义之前独到type
		while(tokenVector.get(next).type != Words.TYPE &&tokenVector.get(next).type != Words.VAR && tokenVector.get(next).type != Words.PROCEDURE&&tokenVector.get(next).type != Words.BEGIN)
		{
			if(next<tokenVector.size()-1)
				next++;
		}
		
		TreeNode tp1=typeDec();
		typeP.child[0]=tp1;
		
		
		TreeNode varP = new TreeNode();
		varP.nodeKind = TreeNode.NodeKind.VAR_K;
		//varDec只前保证独到var
		while(tokenVector.get(next).type != Words.VAR && tokenVector.get(next).type != Words.PROCEDURE&&tokenVector.get(next).type != Words.BEGIN)
		{
			if(next<tokenVector.size()-1)
				next++;
		}
		TreeNode tp2 = varDec();
		varP.child[0]=tp2;
		typeP.brother = varP;
		
		
		while(tokenVector.get(next).type != Words.PROCEDURE&&tokenVector.get(next).type != Words.BEGIN)
		{
			if(next<tokenVector.size()-1)
				next++;
		}
		TreeNode s = procDec();
		varP.brother = s;

		return typeP;
	}
	TreeNode typeDec()
	{
		TreeNode croot = new TreeNode();
		croot.nodeKind = TreeNode.NodeKind.DEC_K;

		Token ctoken = tokenVector.get(next);
		if(ctoken.type==Words.TYPE)
		{
			return typeDeclaration();
		}
		else if(ctoken.type==Words.VAR)
			return null;
		else if(ctoken.type==Words.PROCEDURE)
				return null;
		else if(ctoken.type==Words.BEGIN)
		{
			return null;
		}
		else
		{
			next++;
			return null;
		}

	}
	//6
	TreeNode typeDeclaration()
	{
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type==Words.TYPE)
		{
			TreeNode t = typeDecList();
			if(t==null)
			{
				message+=""+this.tokenVector.get(next-1).line+":declaration wrong \n";
				return t;
			}
			else
				return t;
		}
		next--;
		return null;
	}
	TreeNode typeDecList()
	{
		TreeNode t = new TreeNode();
		t.nodeKind = TreeNode.NodeKind.DEC_K;
		if(tokenVector.get(next).type != Words.IDENTIFIERS)
		{
			message += ""+tokenVector.get(next).line+":expect ID\n";
		}
		else
			typeId(t);
		
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type!=Words.EQUAL)
		{
			next--;
			message += ""+this.tokenVector.get(next-1).line+":expect \"=\"\n";
		}

		typeDef(t);
		
		ctoken = tokenVector.get(next++);
		if(ctoken.type!=Words.SEMICOLON)
		{
			message += ""+this.tokenVector.get(next-1).line+":expect \";\"\n";
			next--;
		}
		TreeNode p = typeDecMore();
		if(p != null)
			t.brother = p;
		return t;
	}
	//8*****************************
	TreeNode typeDecMore()
	{
		TreeNode tn = null;
		Token ctoken = tokenVector.get(next);
		if(ctoken.type==Words.VAR||ctoken.type==Words.PROCEDURE||ctoken.type==Words.BEGIN)
			return tn;
		else
		{
			if(ctoken.type==Words.IDENTIFIERS)
				return typeDecList();
			else
			{
				message +=""+this.tokenVector.get(next-1).line+":expect \"ID\"\n";
				next++;
				return typeDecMore();
			}
		}
		
	}
	void typeId(TreeNode tn)
	{
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type==Words.IDENTIFIERS)
		{
			tn.idString.add(ctoken.context);
		}
	}
	void typeDef(TreeNode tn)
	{
		if(tn == null)
			return;
		Token ctoken = tokenVector.get(next);
		if(ctoken.type==Words.INTEGER||ctoken.type==Words.CHAR)
		{
			baseType(tn);
		}
		else if(ctoken.type==Words.RECORD||ctoken.type==Words.ARRAY)
		{
			structureType(tn);
		}
		else if(ctoken.type==Words.IDENTIFIERS)
		{
			next++;
			tn.idString.add(ctoken.context);
		}
	}
	//11
	void baseType(TreeNode tn)
	{
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type==Words.INTEGER)
		{
			tn.kind = TreeNode.Kind.INTEGER_DEC;
			tn.idString.add("Integer");
		}
		else if(ctoken.type==Words.CHAR)
		{
			tn.kind = TreeNode.Kind.CHAR_DEC;
			tn.idString.add("Char");
		}
	}
	void structureType(TreeNode tn)
	{
		Token ctoken = tokenVector.get(next);
		if(ctoken.type == Words.ARRAY)
		{
			arrayType(tn);
		}else if(ctoken.type == Words.RECORD)
		{
			tn.kind = TreeNode.Kind.RECORD_DEC;
			recType(tn);
		}
	}
	void arrayType(TreeNode tn)
	{
		//array[
		if(tokenVector.get(next++).type==Words.ARRAY)
		{
			tn.idString.add("Array");
		}
		else
		{
			next--;
			message += ""+this.tokenVector.get(next-1).line+":expect array\n";
		}
		//array[
		if(tokenVector.get(next++).type!=Words.LEFT_BRACKET)
		{
			next--;
			message += ""+this.tokenVector.get(next-1).line+":expect [\n";
		}
		//number
		if(tokenVector.get(next++).type==Words.UNSIGNEDNUMBER)
		{
			tn.idString.add(tokenVector.get(next-1).context);
		}
		else
		{
			next--;
			message += ""+this.tokenVector.get(next-1).line+":expect number\n";
		}
		//..
		
		if(tokenVector.get(next++).type==Words.TWO_DOT)
		{
			tn.idString.add(tokenVector.get(next-1).context);
		}
		else
		{
			message += ""+this.tokenVector.get(next-1).line+":expect ..\n";
		}
		if(tokenVector.get(next++).type==Words.UNSIGNEDNUMBER)
		{
			tn.idString.add(tokenVector.get(next-1).context);
		}
		else
		{
			next--;
			message += ""+this.tokenVector.get(next-1).line+":expect number\n";
		}
		//]
		if(tokenVector.get(next++).type!=Words.RIGHT_BRACKET)
		{
			message += ""+this.tokenVector.get(next-1).line+":expect ]\n";
		}
		if(tokenVector.get(next++).type!=Words.OF)
		{
			message += ""+this.tokenVector.get(next-1).line+":expect of\n";
		}
		baseType(tn);
		tn.kind = TreeNode.Kind.ARRAY_DEC;
	}
	//14
	void recType(TreeNode tn)
	{
		if(tokenVector.get(next).type==Words.RECORD)
			next++;
		TreeNode fdl = fieldDecList();
		if(fdl!=null)
			tn.child[0]=fdl;
		else
			message += ""+this.tokenVector.get(next-1).line+":record type wrong\n";
		if(tokenVector.get(next++).type!=Words.END)
			message += ""+this.tokenVector.get(next-1).line+":expect end\n";
	}
	TreeNode fieldDecList()
	{
		TreeNode tn = new TreeNode();
		TreeNode p = null;
		tn.nodeKind = TreeNode.NodeKind.DEC_K;
		Token ctoken = tokenVector.get(next);
		if(ctoken.type==Words.INTEGER||ctoken.type==Words.CHAR)
		{
			baseType(tn);
			idList(tn);
			if(tokenVector.get(next++).type!=Words.SEMICOLON)
			{
				message += ""+this.tokenVector.get(next-1).line+":expect ;\n";
			}
			p = fieldDecMore();
		}
		else if(ctoken.type==Words.ARRAY)
		{
			arrayType(tn);
			idList(tn);
			if(tokenVector.get(next++).type!=Words.SEMICOLON)
			{
				message += ""+this.tokenVector.get(next-1).line+":expect ;\n";
			}
			p = fieldDecMore();
		}
		if(p!=null)
			tn.brother = p;
		return tn;
		
	}
	//16
	TreeNode fieldDecMore()
	{
		Token ctoken = tokenVector.get(next);
		if(ctoken.type==Words.END)
		{
			return null;
		}
		else if(ctoken.type == Words.INTEGER || ctoken.type == Words.CHAR || ctoken.type == Words.ARRAY)
		{
			return fieldDecList();
		}
		return null;
	}
	void idList(TreeNode tn)
	{
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type==Words.IDENTIFIERS)
		{
			tn.idString.add(ctoken.context);
		}
		idMore(tn);
	}
	void idMore(TreeNode tn)
	{
		Token ctoken = tokenVector.get(next);
		if(ctoken.type == Words.SEMICOLON)
		{
			return;
		}
		if(ctoken.type == Words.COMMA)
		{
			next++;
			idList(tn);
		}
	}
	/************************************88***********************************/
	TreeNode varDec()
	{
		TreeNode tn = null;
		Token ctoken = tokenVector.get(next);
		if(ctoken.type == Words.PROCEDURE || ctoken.type == Words.BEGIN)
			return null;
		else if(ctoken.type == Words.VAR)
		{
			tn = varDeclaration();
			return tn;
		}
		else
		{
			next++;
			return varDeclaration();
		}
	}
	//20
	TreeNode varDeclaration()
	{
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type == Words.VAR)
			return varDecList();
		else
		{
			message += ""+this.tokenVector.get(next-1).line+":expect var\n";
			return null;
		}
	}
	TreeNode varDecList()
	{
		TreeNode tn = new TreeNode();
		tn.nodeKind = TreeNode.NodeKind.VAR_K;
		TreeNode p = null;
		typeDef(tn);
		varIdList(tn);
		if(tokenVector.get(next++).type!=Words.SEMICOLON)
		{
			next--;
			message +=""+this.tokenVector.get(next-1).line+":expect ;\n";
		}
		p = varDecMore();
		if(p!=null)
			tn.brother = p;
		return tn;
		
	}
	TreeNode varDecMore()
	{
		Token ctoken = tokenVector.get(next);
		TreeNode tn = null;
		if(ctoken.type == Words.PROCEDURE || ctoken.type== Words.BEGIN)
		{
			return null;
		}
		else if(ctoken.type == Words.INTEGER || ctoken.type == Words.CHAR || ctoken.type == Words.ARRAY || ctoken.type == Words.RECORD || ctoken.type == Words.IDENTIFIERS)
		{
			tn = varDecList();
		}
		return tn;
		
	}
	void varIdList(TreeNode tn)
	{
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type == Words.IDENTIFIERS)
		{
			tn.idString.add(ctoken.context);
		}
		else
		{
			next--;
			message += ""+this.tokenVector.get(next-1).line+":expect ID\n";
		}
		varIdMore(tn);
	}
	void varIdMore(TreeNode tn)
	{
		Token ctoken = tokenVector.get(next);
		if(ctoken.type == Words.SEMICOLON)
		{
			return;
		}
		if(ctoken.type == Words.COMMA)
		{
			next++;
			varIdList(tn);
		}
	}
	//25***********************92
	TreeNode procDec()
	{
		TreeNode tn = null;
		Token ctoken = tokenVector.get(next);
		if(ctoken.type == Words.BEGIN)
		{
			return tn;
		}
		else if(ctoken.type == Words.PROCEDURE)
		{
			tn = procDeclaration();
			if(tn != null)
				tn.brother = procDec();
			return  tn;
		}
		next++;
		return null;
		
	}
	TreeNode procDeclaration()
	{
		TreeNode tn = new TreeNode();
		tn.nodeKind = TreeNode.NodeKind.PROC_DEC_K;
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type == Words.PROCEDURE)
		{
			if(tokenVector.get(next++).type == Words.IDENTIFIERS)
				tn.idString.add(tokenVector.get(next-1).context);
			else
			{
				message +=""+this.tokenVector.get(next-1).line+":expect id\n";
				next--;
			}
			
			if(tokenVector.get(next).type != Words.LEFT_PARENT)
			{
				message += ""+this.tokenVector.get(next-1).line+":expect (\n";
			}
			paramList(tn);
			if(tokenVector.get(next++).type != Words.RIGHT_PARENT)
			{
				next--;
				message += ""+this.tokenVector.get(next-1).line+":expect )\n";
			}
			if(tokenVector.get(next++).type != Words.SEMICOLON)
			{
				next--;
				message += ""+this.tokenVector.get(next-1).line+":expect ;\n";
			}
			tn.child[1] = procDecPart();
			tn.child[2] = procBody();
			tn.child[2].nodeKind = TreeNode.NodeKind.STM_L_K;
		}
		return tn;
		
	}
	void paramList(TreeNode tn)
	{
		if(tokenVector.get(next++).type != Words.LEFT_PARENT)
		{
			next--;
			message += ""+this.tokenVector.get(next-1).line+":expect (\n";
		}
		Token ctoken = tokenVector.get(next);
		if(ctoken.type == Words.INTEGER || ctoken.type == Words.CHAR || ctoken.type == Words.ARRAY || 
				ctoken.type == Words.RECORD || ctoken.type == Words.VAR || ctoken.type == Words.IDENTIFIERS )
		{
			tn.child[0] = paramDecList();
		}
	}
	TreeNode paramDecList()
	{
		TreeNode tn = param();
		if(tn == null)
			return null;
		tn.brother = paramMore();
		return tn;
		
	}
	TreeNode paramMore()
	{
		TreeNode tn = null;
		if(tokenVector.get(next).type == Words.RIGHT_PARENT)
		{
			return null;
		}
		if(tokenVector.get(next++).type == Words.SEMICOLON)
		{
			tn = paramDecList();
			return tn;
		}
		else
		{
			message += ""+this.tokenVector.get(next-1).line+":ecpect )\n";
			return tn;
		}
	}
	//30
	TreeNode param()
	{
		TreeNode tn = new TreeNode();
		tn.nodeKind = TreeNode.NodeKind.VAR_K;
		Token ctoken = tokenVector.get(next);
		/*************************************/
		if(ctoken.type == Words.INTEGER || ctoken.type == Words.CHAR || ctoken.type == Words.ARRAY || 
				ctoken.type == Words.RECORD || ctoken.type == Words.IDENTIFIERS )
		{
			tn.idString.add("Value");
			typeDef(tn);
			formList(tn);
			tn.nodeKind = TreeNode.NodeKind.DEC_K;
		}
		else if(ctoken.type == Words.VAR)
		{
			tn.idString.add("Var");
			next++;
			typeDef(tn);
			formList(tn);
			tn.nodeKind = TreeNode.NodeKind.DEC_K;
		}
		return tn;
		
	}
	void formList(TreeNode tn)
	{
		if(tokenVector.get(next++).type == Words.IDENTIFIERS)
		{
			tn.idString.add(tokenVector.get(next-1).context);
		}
		else
		{
			message += ""+this.tokenVector.get(next-1).line+":expect id\n";
		}
		fidMore(tn);
	}
	void fidMore(TreeNode tn)
	{
		Token ctoken = tokenVector.get(next);
		if(ctoken.type == Words.SEMICOLON || ctoken.type == Words.RIGHT_PARENT)
		{
			return;
		}
		else if(ctoken.type == Words.COMMA)
		{
			next++;
			formList(tn);
		}
	}
	TreeNode procDecPart()
	{
		return declarePart();
	}
	TreeNode procBody()
	{
		TreeNode tn = programBody();
		if(tn == null)
			message += ""+this.tokenVector.get(next-1).line+":program body wrong\n";
		return tn;
	}
	//35
	TreeNode programBody()
	{
		TreeNode tn = new TreeNode();
		tn.nodeKind = TreeNode.NodeKind.STM_L_K;
		if(tokenVector.get(next++).type != Words.BEGIN)
		{
			message += ""+tokenVector.get(next-1).line+":expect begin\n";
			next--;
		}
		tn.child[0] = stmList();
		if(tokenVector.get(next++).type != Words.END)
		{
			message += ""+this.tokenVector.get(next-1).line+":expect end\n";
			next--;
		}
		return tn;
	}
	TreeNode stmList()
	{
		TreeNode tn = stm();
		if(tn!=null)
			tn.brother = stmMore();
		return tn;
		
	}
	TreeNode stmMore()
	{
		TreeNode tn = null;

		Token ctoken = tokenVector.get(next);
		if(ctoken.type != Words.SEMICOLON)
		{
			message += ""+this.tokenVector.get(next-1).line+":expect ;\n";
		}
		else
		{
			next++;
			ctoken = tokenVector.get(next);
		}
		
		if(ctoken.type == Words.END)
		{
			return null;
		}
		else if(ctoken.type == Words.ENDWH)
		{
			next++;
			return null;
		}
		else{
			tn = stmList();
			return tn;
		}
	}
	TreeNode stm()
	{
		Token ctoken = tokenVector.get(next);
		switch(ctoken.type)
		{
		case IF:
			return conditionalStm();
		case WHILE:
			return loopStm();
		case RETURN:
			return returnStm();
		case READ:
			return inputStm();
		case WRITE:
			return outputStm();
		case IDENTIFIERS:
			return assCall();
		}
		return null;
	}
	TreeNode assCall()
	{
		TreeNode first = exp(),tmp=null;
		if(next>=tokenVector.size()-1)
		{
			return first;
		}
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type == Words.COLON_EQUAL)
		{
			tmp =  assignmentRest();
			if(tmp!=null)
				tmp.child[0] = first;
			return tmp;
		}
		else if(ctoken.type == Words.LEFT_PARENT)
		{
			tmp = callStmRest();
			if(tmp!=null)
				tmp.idString = first.idString;
			return tmp;
		}

		next--;
		return null;
	}
	//40
	TreeNode assignmentRest()
	{
		TreeNode tn = new TreeNode();
		tn.nodeKind = TreeNode.NodeKind.STMT_K;
		tn.idString.add("Assign");
		tn.child[0] = null;
		tn.child[1] = exp();
		return tn;
		
	}
	TreeNode conditionalStm()
	{
		TreeNode tn = new TreeNode();
		tn.nodeKind = TreeNode.NodeKind.STMT_K;
		tn.idString.add("Conditional");
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type == Words.IF)
		{
			tn.child[0] = exp();
		}else
		{
			message += ""+this.tokenVector.get(next-1).line+":expect if\n";
		}
		ctoken = tokenVector.get(next++);
		if(ctoken.type == Words.THEN)
		{
			tn.child[1] = stm();
		}else
		{

			message += ""+this.tokenVector.get(next-1).line+":expect then\n";
			next--;
			tn.child[1] = stm();
		}
		ctoken = tokenVector.get(next++);
		if(ctoken.type == Words.ELSE)
		{
			tn.child[2] = stm();
		}else
		{
			message += ""+this.tokenVector.get(next-1).line+":expect else\n";
			next--;
			tn.child[2] = stm();
		}
		ctoken = tokenVector.get(next++);
		if(ctoken.type != Words.FI)
		{
			message += ""+this.tokenVector.get(next-1).line+":expect fi\n";
			next--;
		}
		return tn;
		
	}
	TreeNode loopStm()
	{
		TreeNode tn = new TreeNode();
		tn.nodeKind = TreeNode.NodeKind.STMT_K;
		tn.idString.add("While");
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type == Words.WHILE)
		{
			tn.child[0] = exp();
		}else
		{
			message += ""+this.tokenVector.get(next-1).line+":expect while\n";
		}
		
		ctoken = tokenVector.get(next++);
		if(ctoken.type == Words.DO)
		{
			tn.child[1] = stmList();
		}else
		{
			message += ""+this.tokenVector.get(next-1).line+":expect do\n";
			next--;
			tn.child[1] = stmList();
		}
		
//		ctoken = tokenVector.get(next++);
		/*
		if(ctoken.type != Words.ENDWH)
		{
			next--;
			message += ""+this.tokenVector.get(next-1).line+":expect endwh\n";
		}
		*/
		return tn;
		
	}
	TreeNode inputStm()
	{
		TreeNode tn = new TreeNode();
		tn.nodeKind = TreeNode.NodeKind.STMT_K;
		tn.idString.add("Read");
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type != Words.READ)
		{
			message += ""+this.tokenVector.get(next-1).line+":expect read\n";
		}
		ctoken = tokenVector.get(next++);
		if(ctoken.type != Words.LEFT_PARENT)
		{
			message += ""+this.tokenVector.get(next-1).line+":expect (\n";
			next--;
		}
		
		ctoken = tokenVector.get(next++);
		if(ctoken.type != Words.IDENTIFIERS)
		{
			message += ""+this.tokenVector.get(next-1).line+":expect id\n";
		}
		else
		{
			tn.idString.add(ctoken.context);
		}
		
		ctoken = tokenVector.get(next++);
		if(ctoken.type != Words.RIGHT_PARENT)
		{
			message += ""+this.tokenVector.get(next-1).line+":expect )\n";
			next--;
		}
		return tn;
		
	}
	TreeNode outputStm()
	{
		TreeNode tn = new TreeNode();
		tn.nodeKind = TreeNode.NodeKind.STMT_K;
		tn.idString.add("Write");
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type != Words.WRITE)
		{
			message += ""+this.tokenVector.get(next-1).line+":expect write\n";
		}
		ctoken = tokenVector.get(next++);
		if(ctoken.type != Words.LEFT_PARENT)
		{
			message += ""+this.tokenVector.get(next-1).line+":expect (\n";
			next--;
		}
		
		tn.child[0] = exp();
		ctoken = tokenVector.get(next++);
		if(ctoken.type != Words.RIGHT_PARENT)
		{
			message +=""+this.tokenVector.get(next-1).line+":expect )\n";
			next--;
		}
		return tn;
		
	}
	//45
	TreeNode returnStm()
	{
		TreeNode tn = new TreeNode();
		tn.nodeKind = TreeNode.NodeKind.STMT_K;
		tn.idString.add("Return");
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type != Words.RETURN)
		{
			message += ""+this.tokenVector.get(next-1).line+":expect return\n";
		}
		return tn;
		
	}
	TreeNode callStmRest()
	{
		TreeNode tn = new TreeNode();
		tn.nodeKind = TreeNode.NodeKind.STMT_K;
		Token ctoken = tokenVector.get(next-1);

		if(ctoken.type != Words.LEFT_PARENT)
		{
			message +=""+this.tokenVector.get(next-1).line+":expect (\n";
		}
		
		tn.child[0] = actParamList();
		if(tn.child[0]!=null)
			tn.child[0].nodeKind = TreeNode.NodeKind.DEC_K;
		
		ctoken = tokenVector.get(next++);
		if(ctoken.type != Words.RIGHT_PARENT)
		{
			message += ""+this.tokenVector.get(next-1).line+":expect )\n";
			next--;
		}

		return tn;
	}
	TreeNode actParamList()
	{
		TreeNode tn = null;
		Token ctoken = tokenVector.get(next);
		if(ctoken.type == Words.RIGHT_PARENT)
		{
			return null;
		}
		else if(ctoken.type == Words.IDENTIFIERS || ctoken.type == Words.UNSIGNEDNUMBER)
		{
			//next--;
			tn = exp();
			if(tn != null)
			{
				tn.brother = actParamMore();
				return tn;
			}
		}
		return tn;
	}
	TreeNode actParamMore()
	{
		TreeNode tn = null;
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type == Words.RIGHT_PARENT)
		{
			next--;
			return null;
		}
		else if(ctoken.type == Words.COMMA)
		{
			return actParamList();
		}
		return null;
		
	}
	TreeNode exp()
	{
		TreeNode tn = simpleExp();
		if(next>=tokenVector.size()-1)
			return tn;
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type == Words.EQUAL || ctoken.type == Words.LESS)
		{
			TreeNode op = new TreeNode();
			op.nodeKind = TreeNode.NodeKind.STMT_K;
			if(ctoken.type == Words.COLON_EQUAL)
				op.idString.add("Assign");
			else
				op.idString.add("Less");
			op.child[0] = tn;
			tn = op;
		}
		else if(ctoken.type==Words.RIGHT_BRACKET)
		{
			return tn;
		}
		else
		{
			next--;
			return tn;
		}
		if(tn!=null)
		{
			tn.child[1] = simpleExp();
		}
		return tn;
		
	}
	//50
	TreeNode simpleExp()
	{
		TreeNode tn = term();
		if(next>=tokenVector.size()-1)
			return tn;
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type == Words.ADD || ctoken.type == Words.SUB)
		{
			TreeNode op = new TreeNode();
			op.nodeKind = TreeNode.NodeKind.EXP_K;
			if(ctoken.type == Words.ADD)
				op.idString.add("ADD");
			else
				op.idString.add("SUB");
			op.child[0] = tn;
			op.child[1] = simpleExp();
			return op;
		}
		next--;
		return tn;
		
	}
	TreeNode term()
	{
		TreeNode tn = factor();
		if(next>=tokenVector.size()-1)
			return tn;
		Token ctoken = tokenVector.get(next++);
		if(ctoken.type == Words.MUL || ctoken.type == Words.DIV)
		{
			TreeNode p = new TreeNode();
			p.nodeKind = TreeNode.NodeKind.EXP_K;
			if(ctoken.type == Words.MUL)
				p.idString.add("MUL");
			else
				p.idString.add("DIV");
			p.child[0]=tn;
			p.child[1] = exp();
			tn = p;
			return tn;
		}
		next--;
		return tn;
		
	}
	TreeNode factor()
	{
		TreeNode tn = null;
		switch(tokenVector.get(next).type)
		{
		case UNSIGNEDNUMBER:
		{
			TreeNode t = new TreeNode();
			t.nodeKind = TreeNode.NodeKind.EXP_K;
			t.idString.add(tokenVector.get(next++).context);
			return t;
		}
		case IDENTIFIERS:
			return variable();
		case LEFT_PARENT:
		{
/*********/			next++;
			tn = exp();
			break;
		}
		case RIGHT_BRACKET:
		{
			next++;
			return tn;
		}
		default:
		{
				
			message += ""+this.tokenVector.get(next-1).line+tokenVector.get(next).type.name()+"   wrong \n";
		}
		}
		return tn;
		
	}
	/**************111*****************/
	TreeNode variable()
	{
		TreeNode tn = new TreeNode();
		tn.nodeKind = TreeNode.NodeKind.EXP_K;
		if(tokenVector.get(next++).type == Words.IDENTIFIERS)
		{
			tn.idString.add(tokenVector.get(next-1).context);
			variMore(tn);
			return tn;
		}
		else
		{
			next--;
			return null;
		}

	}
	void variMore(TreeNode tn)
	{
		Token ctoken = tokenVector.get(next);
		if(ctoken.type == Words.LEFT_BRACKET)
		{
			next++;
			tn.child[0]=variable();
			if(tokenVector.get(next).type==Words.RIGHT_BRACKET)
				next++;
			else
				message += ""+tokenVector.get(next).line + "expect ]\n";
		}
		else if(ctoken.type == Words.DOT)
		{
			if(next>=tokenVector.size()-1)
				return;
			next++;
			tn.child[0] = fieldVar();
		}
		else if(ctoken.type == Words.COLON_EQUAL)
		{
			return;
		}
	}
	//55
	TreeNode fieldVar()
	{
		TreeNode tn = new TreeNode();
		tn.nodeKind = TreeNode.NodeKind.EXP_K;
		if(tokenVector.get(next++).type == Words.IDENTIFIERS)
		{
			tn.idString.add(tokenVector.get(next-1).context);
			fieldvarMore(tn);
		}
		else
		{
			message += ""+this.tokenVector.get(next-1).line+":expect id\n";
		}
		return tn;
	}
	void fieldvarMore(TreeNode tn)
	{
		Token ctoken = tokenVector.get(next);
		if(ctoken.type == Words.COLON_EQUAL)
		{
		}
		else if(ctoken.type == Words.LEFT_BRACES)
		{
			tn.brother = exp();
		}
	}
	void match(Words expected)
	{
		
	}
}
