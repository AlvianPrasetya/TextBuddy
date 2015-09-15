import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.*;

public class TextBuddyTest {

	public static final String NL = System.getProperty("line.separator");
	
	public Scanner sc = new Scanner(System.in);
	
	@Test
	public void testExecuteCommand() {
		
		testOneCommand("display before any add", "mytestfile.txt is empty" + NL, "display");
		
		testOneCommand("add first item", "added to mytestfile.txt: \"Hello World!\"" + NL, "add Hello World!");
		
		testOneCommand("add second item", "added to mytestfile.txt: \"Hello once again!\"" + NL, "add Hello once again!");
		
		testOneCommand("display two items", "1. Hello World!" + NL + "2. Hello once again!" + NL, "display");
		
		testOneCommand("add third item", "added to mytestfile.txt: \"I hope this works.\"" + NL, "add I hope this works.");
		
		testOneCommand("display three items", "1. Hello World!" + NL + "2. Hello once again!" + NL + "3. I hope this works." + NL, "display");
		
		testOneCommand("delete second item", "deleted from mytestfile.txt: \"Hello once again!\"" + NL, "delete 2");
		
		testOneCommand("display two items", "1. Hello World!" + NL + "2. I hope this works." + NL, "display");
		
		testOneCommand("add third item", "added to mytestfile.txt: \"Yeah, it's working!\"" + NL, "add Yeah, it's working!");
		
		testOneCommand("display three items", "1. Hello World!" + NL + "2. I hope this works." + NL + "3. Yeah, it's working!" + NL, "display");
	
		testOneCommand("delete first item", "deleted from mytestfile.txt: \"Hello World!\"" + NL, "delete 1");
		
		testOneCommand("display two items", "1. I hope this works." + NL + "2. Yeah, it's working!" + NL, "display");
		
		testOneCommand("add third item", "added to mytestfile.txt: \"Now what should I do?\"" + NL, "add Now what should I do?");
		
		testOneCommand("add fourth item", "added to mytestfile.txt: \"Should I try to clear this file?\"" + NL, "add Should I try to clear this file?");
		
		testOneCommand("display four items", "1. I hope this works." + NL + "2. Yeah, it's working!" + NL + "3. Now what should I do?" + NL + "4. Should I try to clear this file?" + NL, "display");
		
		testOneCommand("clear", "all content deleted from mytestfile.txt" + NL, "clear");
		
		testOneCommand("add first item", "added to mytestfile.txt: \"I should be the only text now.\"" + NL, "add I should be the only text now.");
		
		testOneCommand("display one item", "1. I should be the only text now." + NL, "display");
		
		testOneCommand("add second item", "added to mytestfile.txt: \"Bye! Nice meeting you.\"" + NL, "add Bye! Nice meeting you.");
		
		testOneCommand("clear", "all content deleted from mytestfile.txt" + NL, "clear");
		
		testOneCommand("display nothing", "mytestfile.txt is empty" + NL, "display");
		
		testOneCommand("exit", null, "exit");
	}
	

	private void testOneCommand(String description, String expected, String command) {
		assertEquals(description, expected, TextBuddy.executeCommand(new TextBuddy("mytestfile.txt"), command, sc)); 
	}

}