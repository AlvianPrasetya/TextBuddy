import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import java.util.Scanner;

/**
 * This program stores and retrieves user specified lines into a storage 
 * file. It takes in user commands and prints the resulting output.
 * Valid commands include "add", "delete", "display", "clear", and "exit".
 * Add function adds the specified line to the end of the storage file.
 * Delete function delete the specified line number from the storage file.
 * Display function shows the user all of the lines stored in the storage file.
 * Clear function deletes all entries in the storage file.
 * Exit function terminates the program.
 * The program assumes that the user does not require to manually save as it 
 * will be done on every operation and exceptions are simply handled by showing 
 * the exception messages and terminating the program.
 * The command format is given by the example interaction below:
 
 Welcome to TextBuddy. mytextfile.txt is ready for use
 command: add little brown fox
 added to mytextfile.txt: “little brown fox”
 command: display
 1. little brown fox
 command: add jumped over the moon
 added to mytextfile.txt: “jumped over the moon”
 command: display
 1. little brown fox
 2. jumped over the moon
 command: delete 2
 deleted from mytextfile.txt: “jumped over the moon”
 command: display
 1. little brown fox
 command: clear
 all content deleted from mytextfile.txt
 command: display
 mytextfile.txt is empty
 command: exit
 
 * @author Alvian Prasetya
 */
public class TextBuddy {
	
	/*
	 * User interaction messages are displayed in one place for 
	 * convenient editing and proof-reading.
	 */
	private static final String MESSAGE_WELCOME = "Welcome to TextBuddy. %1$s is ready for use%n";
	private static final String MESSAGE_ENTER_COMMAND = "command: ";
	private static final String MESSAGE_FILE_IS_EMPTY = "%1$s is empty%n";
	private static final String MESSAGE_FILE_NAME_EMPTY = "fatal error: file name cannot be empty%n";
	private static final String MESSAGE_ADD_LINE_SUCCESS = "added to %1$s: \"%2$s\"%n";
	private static final String MESSAGE_DELETE_LINE_SUCCESS = "deleted from %1$s: \"%2$s\"%n";
	private static final String MESSAGE_DELETE_LINE_FAILED = "failed to delete from %1$s, "
														   + "line %2$s cannot be found%n";
	private static final String MESSAGE_CLEAR_FILE_SUCCESS = "all content deleted from %1$s%n";
	private static final String MESSAGE_COMMAND_UNRECOGNIZED = "command is not recognized, "
															 + "please enter a valid command "
															 + "(add/display/delete/clear)%n";
	private static final String MESSAGE_EXCEPTION = "exception encountered: %1$s%n";
	
	// This defines the temporary file name used to copy from initial storage file.
	private static final String TEMPORARY_FILE_NAME = "tempFile.txt";
	// This defines the format for printing line of string with its numbering.
	private static final String LINE_WITH_NUMBERING = "%1$s. %2$s%n";
	// Position of filename in the Command-Line Argument array.
	private static final int POSITION_OF_FILENAME = 0;
	// Space character as a valid delimiter
	private static final String SPACE = " ";
	
	private File _file;
	
	/**
	 * This is the default constructor for TextBuddy object. The constructor 
	 * will instantiate the File with specified file name and show welcome 
	 * message to user.
	 * @param fileName	Name of the storage file.
	 */
	public TextBuddy(String fileName) {
		try {
			_file = new File(fileName);			
			// Create new file if the file does not exist.
			if (!_file.exists()) {
				_file.createNewFile();
			}
			showToUser(String.format(MESSAGE_WELCOME, fileName));
		} catch (Exception exceptionMessage) {
			showToUser(String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage()));
		}
	}
	
	/**
	 * This is the main body of the program.
	 * @param args	The Command-Line Argument specified by the user when running the program.
	 */
	public static void main(String[] args) {
		Scanner scannerObject = new Scanner(System.in);
		String fileName;
		
		if (isEmptyArray(args)) {
			showToUser(MESSAGE_FILE_NAME_EMPTY);
			fileName = null;
			System.exit(0);
		} else {
			fileName = args[POSITION_OF_FILENAME];
		}
		TextBuddy newTextBuddy = new TextBuddy(fileName);
		runCommandsUntilExit(newTextBuddy, scannerObject);
	}
	
	/**
	 * This method fetches and runs user commands until the command
	 * specified is "exit".
	 * @param newTextBuddy		The TextBuddy object containing the storage file.
	 * @param scannerObject		Scanner for receiving typed inputs from user.
	 */
	public static void runCommandsUntilExit(TextBuddy newTextBuddy, Scanner scannerObject) {
		String commandLine, feedback;
		
		do {
			commandLine = readCommandLine(scannerObject);
			feedback = executeCommand(newTextBuddy, commandLine, scannerObject);
			if (feedback != null) showToUser(feedback);
		} while (feedback != null);
	}
	
	public static String executeCommand(TextBuddy newTextBuddy, String commandLine, Scanner scannerObject) {
		String commandType = getCommandType(commandLine).toLowerCase();
		
		if (commandType.equals("display")) {
			return newTextBuddy.display();
		} else if (commandType.equals("add")) {
			String stringToAdd = getCommandParameter(commandLine);
			return newTextBuddy.add(stringToAdd);
		} else if (commandType.equals("delete")) {
			int lineNumberToDelete = Integer.parseInt(getCommandParameter(commandLine));
			return newTextBuddy.delete(lineNumberToDelete);
		} else if (commandType.equals("clear")) {
			return clear(newTextBuddy);
		} else if (commandType.equals("exit")) {
			return null;
		} else {
			return MESSAGE_COMMAND_UNRECOGNIZED;
		}
	}
	
	/**
	 * This method displays all the entries in the storage file along
	 * with each of their corresponding numbering.
	 * @param newTextBuddy	The TextBuddy object containing the storage file.
	 */
	public String display() {
		if (isEmptyFile(_file)) {
			return String.format(MESSAGE_FILE_IS_EMPTY, _file.getName());
		} else {
			// Initialize the required reader objects to read the storage file.
			try (FileReader fileInputStream = new FileReader(_file);
				 BufferedReader reader = new BufferedReader(fileInputStream)) {
				
				return getFileContent(reader);
				
			} catch (IOException exceptionMessage) {
				return String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage());
			}
		}
	}
	
	/**
	 * This method appends the specified String to the last line of the 
	 * storage file.
	 * @param newTextBuddy	The TextBuddy object containing the storage file.
	 * @param lineToAdd		The String to be added to the storage file.
	 */
	public String add(String lineToAdd) {
		// Initialize the required writer objects to write into storage file.
		try (FileWriter fileOutputStream = new FileWriter(_file, true);
			 BufferedWriter writer = new BufferedWriter(fileOutputStream)) {
			
			writer.append(lineToAdd);
			writer.append("\n");
			return String.format(MESSAGE_ADD_LINE_SUCCESS, _file.getName(), 
								 lineToAdd);
			
		} catch (IOException exceptionMessage) {
			return String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage());
		}
	}
	
	/**
	 * This method deletes an entry with the specified line number from 
	 * the storage file.
	 * @param newTextBuddy			The TextBuddy object containing the storage file.
	 * @param lineNumberToDelete	The line number of the string to be deleted.
	 */
	public String delete(int lineNumberToDelete) {
		File temporaryFile = new File(TEMPORARY_FILE_NAME);
		// Initialize the required reader objects to read the storage file.
		// Initialize the required writer objects to write into temporary file.
		try (FileReader fileInputStream = new FileReader(_file);
			 BufferedReader reader = new BufferedReader(fileInputStream);
			 FileWriter temporaryOutputStream = new FileWriter(temporaryFile);
			 BufferedWriter temporaryWriter = new BufferedWriter(temporaryOutputStream)) {
			
			temporaryFile.createNewFile();
			return copyUndeletedLinesToNewFile(_file.getName(), reader, 
										temporaryWriter, lineNumberToDelete);
			
		} catch (IOException exceptionMessage) {
			return String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage());
		} finally {
			deleteAndReplace(_file, temporaryFile);
		}
	}
	
	/**
	 * This method clears the entire storage file content then 
	 * prints a success message if it succeeds or prints exception
	 * message if exception occurs.
	 * @param newTextBuddy	The TextBuddy object containing the storage file.
	 */
	public static String clear(TextBuddy newTextBuddy) {
		File temporaryFile = new File(TEMPORARY_FILE_NAME);
		try {
			temporaryFile.createNewFile();
			deleteAndReplace(newTextBuddy._file, temporaryFile);
			return String.format(MESSAGE_CLEAR_FILE_SUCCESS, newTextBuddy._file.getName());
		} catch (IOException exceptionMessage) {
			return String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage());
		}
	}
	
	/**
	 * This method copies the lines with line number other than the one 
	 * to be deleted to a temporary file.
	 * @param fileName				The file name of the source file.
	 * @param reader				The reader object to read through the source file.
	 * @param temporaryWriter		The writer object to write through the temporary file.
	 * @param lineNumberToDelete	The line number of the string to be deleted.
	 */
	public static String copyUndeletedLinesToNewFile(String fileName, BufferedReader reader, 
												   BufferedWriter temporaryWriter, int lineNumberToDelete) {
		try {
			String currentLine, deletedLine = null;
			int currentLineNumber = 0;
			// Read the next line from storage file until null.
			currentLine = reader.readLine();
			while (currentLine != null) {
				currentLineNumber++;
				// Copy the lines that are not going to be deleted to temporary file.
				if (currentLineNumber != lineNumberToDelete) {
					temporaryWriter.append(currentLine);
					temporaryWriter.append("\n");
				} else {
					deletedLine = currentLine;
				}	
				currentLine = reader.readLine();
			}
			if (lineNumberToDelete > currentLineNumber){
				return String.format(MESSAGE_DELETE_LINE_FAILED, fileName, lineNumberToDelete);
			} else {
				return String.format(MESSAGE_DELETE_LINE_SUCCESS, fileName, deletedLine);
			}
		} catch (IOException exceptionMessage) {
			return String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage());
		}
	}
	
	/**
	 * This method checks if a file is empty.
	 * If the file is empty, true is returned, returns false otherwise.
	 * @param file	The File object to be checked.
	 * @return		Boolean indicating if the file is empty.
	 */
	public static boolean isEmptyFile(File fileToCheck) {
		// Initialize the required reader objects to read the storage file.
		try (FileReader fileInputStream = new FileReader(fileToCheck);
			 BufferedReader reader = new BufferedReader(fileInputStream)) {

			String firstLine = reader.readLine();
			if (firstLine == null) {
				return true;
			} else {
				return false;
			}
			
		} catch (IOException exceptionMessage) {
			showToUser(String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage()));
			return false;
		}
	}
	
	/**
	 * This method deletes a specified file, 
	 * then replaces it by renaming another file into the one deleted.
	 * @param fileToDelete		The File object to be deleted.
	 * @param fileToReplace		The File object to replace the deleted file.
	 */
	public static void deleteAndReplace(File fileToDelete, File fileToReplace) {
		fileToDelete.delete();
		fileToReplace.renameTo(fileToDelete);
	}
	
	public static String readCommandLine(Scanner scannerObject) {
		String command;
		
		showToUser(MESSAGE_ENTER_COMMAND);
		command = scannerObject.nextLine();
		return command;
	}
	
	public static String getCommandType(String commandLine){
		if (commandLine.contains(SPACE)) {
			return commandLine.substring(0, commandLine.indexOf(SPACE));
		} else {
			return commandLine;
		}
	}
	
	public static String getCommandParameter(String commandLine) {
		if (commandLine.contains(SPACE)) {
			return commandLine.substring(commandLine.indexOf(SPACE)+1, commandLine.length());
		} else {
			return null;
		}
	}
	
	/**
	 * This method prints all the lines in the specified file 
	 * preceded by their line numbers.
	 * @param reader	The reader object to read through the storage file.
	 */
	public static String getFileContent(BufferedReader reader) {
		try {
			String fileContent = new String("");
			String currentLine;
			int numOfLinesRead = 0;
			
			currentLine = reader.readLine();
			while (currentLine != null) {
				numOfLinesRead++;
				fileContent += String.format(LINE_WITH_NUMBERING, numOfLinesRead, currentLine);
				currentLine = reader.readLine();
			}
			
			return fileContent;
		} catch (IOException exceptionMessage) {
			return String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage());
		}
	}
	
	public static boolean isEmptyArray(String[] arrayToCheck){
		if (arrayToCheck.length == 0){
			return true;
		} else {
			return false;
		}
	}
	
	public static void showToUser(String stringToShow){
		System.out.print(stringToShow);
	}
}
