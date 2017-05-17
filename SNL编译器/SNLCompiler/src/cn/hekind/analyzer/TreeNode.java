package cn.hekind.analyzer;

import java.util.*;

public class TreeNode {
	public enum NodeKind
	{
		PROC_K, PHEAD_K, TYPE_K, VAR_K, PROC_DEC_K, STM_L_K,
		DEC_K, STMT_K, EXP_K
	}
	public enum Kind
	{
		ARRAY_DEC, CHAR_DEC, INTEGER_DEC, RECORD_DEC, ID_DEC,
		IF_STMT, WHILE_STMT,ASSIGN_STMT,READ_STMT, WRITE_STMT, CALL_STMT, RETURN_STMT,
		OP_EXP,CONST_EXP,ID_EXP
	}
	public TreeNode[] child;
	public TreeNode brother;
	int line;
	
	public NodeKind nodeKind;
	Kind kind;
	public ArrayList<String> idString;
	ArrayList<String> typeArray;
	ArrayList<String> addr;
	public TreeNode()
	{
		child = new TreeNode[3];
		addr = new ArrayList<String>();
		idString = new ArrayList<String>();
		typeArray = new ArrayList<String>();
		brother = null;
	}
	public void printTree(TreeNode root,int space)
	{
		if(root==null)
			return;
		for(int i=0;i<space;i++)
			System.out.print("    ");
		System.out.println(root.nodeKind.toString()+root.idString);
		for(int i=0;i<3;i++)
		{
			if(root.child[i]!=null)
				printTree(root.child[i],space+1);
		}
		if(root.brother!=null)
		{
			printTree(root.brother,space);
		}
	}
	public String getTree(TreeNode root, int space)
	{
		String output = "";
		if(root == null)
			return "";
		for(int i=0;i<space;i++)
			output+="    ";
		output+=root.nodeKind.toString()+root.idString+"\n";
		for(int i=0;i<3;i++)
			if(root.child[i]!=null)
				output+=root.child[i].getTree(root.child[i], space+1);
		if(root.brother!=null)
			output+=root.brother.getTree(root.brother, space);
		return output;
	}
}
