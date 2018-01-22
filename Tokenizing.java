import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Scanner;

public class Tokenizing
{
	byte[] b = null;
	FileOutputStream ir;
	FileInputStream in;
	Opcodes op = new Opcodes();							
	Scanner sc = new Scanner(System.in);
	String fname,strFile,pass1 = "Pass1.txt",add_in_file = "",pass2="Pass2.txt";
	Hashtable<String,Integer> litTab = new Hashtable<String,Integer>();
	Hashtable<String,Integer> symTab = new Hashtable<String,Integer>();

	public static void main(String[] args){	
		new Tokenizing().tokenize();
	}
	public void tokenize()
	{
		try{
			//System.out.println("Enter File Name for processing : ");
			fname = "File.txt";	//sc.next();				//take file name from user 
			pass1();
			pass2();
			ir.close();
			in.close();
			sc.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	public void pass2() throws Exception
	{
		strFile = "";
		System.out.println("\n\nPass2 generated in File "+pass2);
		in = new FileInputStream(pass1);//read the intermediate code file
		int k=0;
		while((k=in.read())!=-1)
		{
			strFile = strFile + (char)k;
		}
		add_in_file = "";
		
		ir = new FileOutputStream(pass2);
		String[] pass1Str = strFile.split("\\n");		
		for(int i=0 ; i<pass1Str.length ; i++)
		{
			String temp[] = pass1Str[i].split("\\s");
			for(int j=1 ; j<temp.length ; j++)//j=1 coz we don't need address of that particular inst in pass2	
			{
				String opcode="", regOperand="";
				String str="";

				add_in_file = temp[0];
				if(temp[j].contains(","))
				{			
					str = temp[j].substring(temp[j].indexOf("(")+1, temp[j].indexOf(","));
					opcode = temp[j].substring(temp[j].indexOf(',')+1, temp[j].indexOf(')'));
					if(str.equalsIgnoreCase("IS"))//to get the opcode if it is IS only
					{
						add_in_file = add_in_file +" + "+ opcode;//add the opcode taken from intermediate code of pass 1
						ir.write(10);
					}
					
					if(str.length()==1)//to get the literal
					{
						add_in_file = " "+litTab.get(temp[j].substring(temp[j].indexOf(',')+1, temp[j].indexOf(')')));
					}
					
					if(str.equalsIgnoreCase("AD") && Integer.parseInt(opcode)==05)
					{
						add_in_file = add_in_file +" + "+ opcode;//add the opcode taken from intermediate code of pass 1
						ir.write(10);
					}
					if(str.equalsIgnoreCase("AD") && Integer.parseInt(opcode)==04)
					{
						continue;
					}
					if(str.equalsIgnoreCase("DL") || (str.equalsIgnoreCase("AD")) && !(Integer.parseInt(opcode)==05))
					{
						ir.write(10);
					}
					b = add_in_file.getBytes();
					ir.write(b);
				}
				if(op.isRegister(temp[j]))
				{
					regOperand = op.getRegisterOpcode(temp[j]);
					add_in_file = " "+ regOperand;//add the reg operand taken from intermediate code of pass 1
					b = add_in_file.getBytes();
					ir.write(b);
				}
				if(symTab.containsKey(temp[j]) && !(temp[j-1].contains("AD")) && !(temp[j-1].contains("DL")) )
				{
					add_in_file = " "+symTab.get(temp[j]);
					b = add_in_file.getBytes();
					ir.write(b);
				}
			}
		}
	}
	public String[] splitter()throws Exception
	{
				
		in = new FileInputStream(fname);		
		strFile = new String("");
		int k=0;
		while((k=in.read())!=-1)	//read file and store in string
		{
			strFile  = strFile + (char)k;
		}
		
		return strFile.split(",|\\s");	//split the file according to space or ,
	}
	public void pass1() throws Exception
	{
		System.out.println("\n\nPass1 generated in File "+pass1);
		int start_address = 0, index=0;

		ir = new FileOutputStream(pass1);//output file

		String[] resStr = splitter();
		
		for(int i=0 ; i<resStr.length ; i++)
			System.out.println(" :- "+resStr[i]);
		
		if(resStr[0].equalsIgnoreCase("Start"))	//identify start address if specified
		{
			char x = resStr[1].charAt(0);
			if(x>=48 && x<=57)
			{
				start_address = Integer.parseInt(resStr[1]);
				index = 3;
			}
		}
		//System.out.println("Start Address : "+start_address);		
		for(int i=index ; i<resStr.length ; i++)		
		{			
			String qwerty = resStr[i].substring(resStr[i].indexOf(":")+1, resStr[i].length());

/*================================================================================================================*/			
			if(!(qwerty.equalsIgnoreCase("EQU")) && resStr[i].contains(":") && !(op.isDeclarativeStatement(resStr[i].substring(resStr[i].indexOf(":")+1,resStr[i].length()))))
			{
				String temp = resStr[i];
				temp = temp.substring(0, temp.indexOf(":"));				
				symTab.put(temp, start_address);
				resStr[i] = resStr[i].substring(resStr[i].indexOf(":")+1,resStr[i].length());
			}
/*===============================================================================================================*/

			if(!(qwerty.equalsIgnoreCase("EQU")) && 
					resStr[i].contains(":") && 
					(op.isDeclarativeStatement(resStr[i].substring(resStr[i].indexOf(":")+1,resStr[i].length()))))
			{
				String temp = resStr[i];
				temp = temp.substring(0, temp.indexOf(":"));
				symTab.put(temp, start_address);							
				temp = resStr[i].substring(resStr[i].indexOf(":")+1, resStr[i].length());
				add_in_file =  start_address + " ("+op.getOpcodeDL(temp)+") "+resStr[i+1];
				//System.out.println(add_in_file);
				store(add_in_file);
				start_address = start_address + Integer.parseInt(resStr[i+1]);
				i+=3;
			}
/*================================================================================================*/
			if(resStr[i].equalsIgnoreCase("END"))//END
			{
				add_in_file =  start_address + " ("+op.getOpcodeAD(resStr[i])+") ";				
				if(!(resStr[i+2].equalsIgnoreCase(null)))
				{
					String temp = resStr[i+2];
					temp = temp.replaceAll("=", "");
					temp = temp.replaceAll("'", "");
					add_in_file =  add_in_file + temp;
				}
				//System.out.println(add_in_file);
				store(add_in_file);
				break;
			}
/*================================================================================================*/			
			if(resStr[i].equalsIgnoreCase("ORIGIN"))
			{
				String temp = resStr[i+1];
				temp = temp.substring(0, temp.indexOf("+"));
				String temp2 = resStr[i+1];
				temp2 = temp2.substring(temp2.indexOf("+")+1,temp2.length());
				add_in_file = start_address+" ("+op.getOpcodeAD(resStr[i])+") " + temp +"+"+temp2;
				store(add_in_file);
				i+=2;	
				start_address = symTab.get(temp) + Integer.parseInt(temp2);
			}
/*================================================================================================*/			

			if(resStr[i].contains("EQU") )
			{
				String symbol = resStr[i].substring(0, resStr[i].indexOf(":"));
				String inst = resStr[i].substring(resStr[i].indexOf(":")+1,resStr[i].length());
				add_in_file = start_address+" ("+op.getOpcodeAD(inst)+") "+ resStr[i+1];
				//System.out.println(add_in_file);
				symTab.put(symbol, symTab.get(resStr[i+1]));
				store(add_in_file);
			}

/*================================================================================================*/			
			if(op.isImperativeStatement(resStr[i])) 					
			{
				if(resStr[i].contains(":"))
					resStr[i] = resStr[i].substring(resStr[i].indexOf(":")+1, resStr[i].length());
				add_in_file = start_address+" ("+op.getOpcodeIS(resStr[i])+")";
				if(!(resStr[i].equalsIgnoreCase("Stop")))
					start_address+=3;
				else
					start_address+=1;
				if(resStr[i+1].equalsIgnoreCase("AREG") || resStr[i+1].equalsIgnoreCase("BREG") || resStr[i+1].equalsIgnoreCase("CREG") || resStr[i+1].equalsIgnoreCase("DREG"))
				{
					add_in_file = add_in_file +" "+ resStr[i+1];
					i++;
				}
				if(op.checkLiteral(resStr[i+1]))
				{
					String temp = resStr[i+1];
					temp = temp.replaceAll("=", "");
					temp = temp.replaceAll("'", "");
					add_in_file = add_in_file + " (L,"+temp+")";					
					i++;
				}
				if(op.checkSymbol(resStr[i+1]))
				{
					String temp = resStr[i+1];
					add_in_file = add_in_file + " "+temp;
					i++;					
				}
				//System.out.println(add_in_file);							
				store(add_in_file);
			}
/*================================================================================================*/
			if(resStr[i].equalsIgnoreCase("LTORG"))
			{
				int j=i+2;
				while(resStr[j].contains("="))
				{
					String temp = resStr[j];
					temp = temp.replaceAll("=", "");
					temp = temp.replaceAll("'", "");
					litTab.put(temp, start_address);
					add_in_file = start_address +" ("+ op.getOpcodeAD("LTORG")+") " + temp;
					//System.out.println(add_in_file);
					store(add_in_file);
					start_address+=1;					
					j+=2;
				}
			}
		}
		System.out.println("\n\nData Structures in pass 1 -\n");
		System.out.println("Literal Table -->"+litTab);
		//store("\nLiteral Table : "+litTab);
		System.out.println("Symbol Table  -->"+symTab);
		//store("\nSymbol Table : "+symTab);
	}
	boolean store(String str) throws Exception
	{
		b = str.getBytes();
		ir.write(b);
		ir.write(10);		
		return true;
	}
}