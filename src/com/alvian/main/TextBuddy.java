package com.alvian.main;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;

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
 added to mytextfile.txt: �little brown fox�
 command: display
 1. little brown fox
 command: add jumped over the moon
 added to mytextfile.txt: �jumped over the moon�
 command: display
 1. little brown fox
 2. jumped over the moon
 command: delete 2
 deleted from mytextfile.txt: �jumped over the moon�
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
	private static final String MESSAGE_SORT_FILE_SUCCESS = "all lines in file %1$s have been sorted%n";
	private static final String MESSAGE_SORT_FILE_FAILED = "failed to sort lines in file %1$s, "
														 + "file is empty%n";
	private static final String MESSAGE_SEARCH_SUCCESS = "%1$s line(s) were found "
													   + "with token \"%2$s\" in file %3$s%n";
	private static final String MESSAGE_SEARCH_FAILED = "no line was found "
			  										  + "with token \"%1$s\" in file %2$s%n";
	private static final String MESSAGE_COMMAND_UNRECOGNIZED = "command \"%1$s\" is not recognized, "
															 + "please enter a valid command%n";
	private static final String MESSAGE_EXCEPTION = "exception encountered: %1$s%n";
	
	// This defines the temporary file name used to copy from initial storage file.
	private static final String TEMPORARY_FILE_NAME = "tempFile.txt";
	// This defines the format for line of string with line break.
	private static final String FORMAT_LINE = "%1$s%n";
	// This defines the format for line of string with its numbering.
	private static final String FORMAT_LINE_WITH_NUMBERING = "%1$s. %2$s";
	// Position of filename in the Command-Line Argument array.
	private static final int POSITION_OF_FILENAME = 0;
	
	private File _file;
	
	/**
	 * This is the default constructor for TextBuddy object. The constructor 
	 * will instantiate the File with specified file name and show welcome 
	 * message to user.
	 * @param fileName	Name of the storage file.
	 */
	public TextBuddy(String storageFileName) {
		try {
			_file = new File(storageFileName);			
			// Create new file if the file does not exist.
			if (!_file.exists()) {
				_file.createNewFile();
			}
			showToUser(String.format(MESSAGE_WELCOME, storageFileName));
		} catch (Exception exceptionMessage) {
			showToUser(String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage()));
		}
	}
	
	public static void main(String[] args) {
		Scanner scannerObject = new Scanner(System.in);
		String storageFileName = getStorageFileName(args);
		if (storageFileName == null) {
			showToUser(MESSAGE_FILE_NAME_EMPTY);
			System.exit(0);
		}
		TextBuddy newTextBuddy = new TextBuddy(storageFileName);
		newTextBuddy.runCommandsUntilExit(scannerObject);
	}
	
	/**
	 * This method fetches, runs user commands, and print the feedbacks until 
	 * the command specified is "exit".
	 * @param scannerObject		Scanner for receiving typed inputs from user.
	 */
	public void runCommandsUntilExit(Scanner scannerObject) {
		String commandLine = new String();
		String feedback = new String();
		
		do {
			commandLine = readCommandLine(scannerObject);
			feedback = executeCommand(commandLine);
			if (feedback != null) showToUser(feedback);
		} while (feedback != null);
	}
	
	/**
	 * This method runs the specified command and return the feedback given 
	 * by the program.
	 * @param commandLine		The user-specified command, including the parameters.
	 * @return					The feedback string given after the execution of command.
	 */
	public String executeCommand(String commandLine) {
		String commandType = getCommandType(commandLine).toLowerCase();
		
		if (commandType.equals("display")) {
			return display();
		} else if (commandType.equals("add")) {
			String stringToAdd = getCommandParameter(commandLine);
			return add(stringToAdd);
		} else if (commandType.equals("delete")) {
			int lineNumberToDelete = Integer.parseInt(getCommandParameter(commandLine));
			return delete(lineNumberToDelete);
		} else if (commandType.equals("clear")) {
			return clear();
		} else if (commandType.equals("sort")) {
			return sort();
		} else if (commandType.equals("search")) {
			String searchToken = getCommandParameter(commandLine);
			return search(searchToken);
		} else if (commandType.equals("exit")) {
			return null;
		} else {
			return String.format(MESSAGE_COMMAND_UNRECOGNIZED, commandType);
		}
	}
	
	/**
	 * This method returns a string of all the entries in the storage file 
	 * along with each of their corresponding numbering.
	 * @return	Returns the lines to be displayed, or exception message if 
	 * 			exception occurs.
	 */
	public String display() {
		if (isEmptyFile(_file)) {
			return String.format(MESSAGE_FILE_IS_EMPTY, _file.getName());
		} else {
			// Initialize the required reader objects to read the storage file.
			try (BufferedReader reader = new BufferedReader(new FileReader(_file))) {
				return getCompressedString(addNumberings(getFileContent(reader)));
			} catch (IOException exceptionMessage) {
				return String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage());
			}
		}
	}

	/**
	 * This method appends the specified String to the last line of the 
	 * storage file and return a success/unsuccessful message.
	 * @param lineToAdd		The String to be added to the storage file.
	 * @return	Returns the successfully add/unsuccessful message.
	 */
	public String add(String lineToAdd) {
		// Initialize the required writer objects to write into storage file.
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(_file, true))) {
			writer.append(String.format(FORMAT_LINE, lineToAdd));
			return String.format(MESSAGE_ADD_LINE_SUCCESS, _file.getName(), lineToAdd);
		} catch (IOException exceptionMessage) {
			return String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage());
		}
	}
	
	/**
	 * This method deletes an entry with the specified line number from 
	 * the storage file and return a success/unsuccessful message.
	 * @param lineNumberToDelete	The line number of the string to be deleted.
	 * @return	Returns the successfully delete/unsuccessful message.
	 */
	public String delete(int lineNumberToDelete) {
		// Initialize the required reader objects to read the storage file.
		try (BufferedReader reader = new BufferedReader(new FileReader(_file))) {
			ArrayList<String> fileContent = getFileContent(reader);
			
			if (!isLineNumberValid(lineNumberToDelete, fileContent)) {
				return String.format(MESSAGE_DELETE_LINE_FAILED, _file.getName(), 
									 lineNumberToDelete);
			} else {
				String deletedLine = fileContent.get(lineNumberToDelete - 1);
				fileContent.remove(lineNumberToDelete - 1);
				clear();
				// Initialize the required writer objects to write into storage file.
				BufferedWriter writer = new BufferedWriter(new FileWriter(_file, false));
				try {
					writer.write(getCompressedString(fileContent));
				} finally {
					writer.close();
				}
				return String.format(MESSAGE_DELETE_LINE_SUCCESS, _file.getName(), deletedLine);
			}
		} catch (IOException exceptionMessage) {
			return String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage());
		}
	}
	
	/**
	 * This method clears the entire storage file content then 
	 * returns a success message if it succeeds or exception
	 * message if exception occurs.
	 * @return	Returns the successful message if file has been cleared 
	 * 			or exception message if exception occurs.
	 */
	public String clear() {
		File temporaryFile = new File(TEMPORARY_FILE_NAME);
		
		try {
			temporaryFile.createNewFile();
			deleteAndReplace(_file, temporaryFile);
			return String.format(MESSAGE_CLEAR_FILE_SUCCESS, _file.getName());
		} catch (IOException exceptionMessage) {
			return String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage());
		}
	}
	
	/**
	 * This method sorts the entries in the storage file alphabetically 
	 * and returns a success message or error message if file is empty.
	 * @return	Returns the successful message if file has been sorted 
	 * 			or error message if file is empty.
	 */
	public String sort() {
		// Initialize the required reader objects to read the storage file.
		try (BufferedReader reader = new BufferedReader(new FileReader(_file))) {
			ArrayList<String> fileContent = getFileContent(reader);
			
			if (fileContent.isEmpty()) {
				return String.format(MESSAGE_SORT_FILE_FAILED, _file.getName());
			} else {
				// Entries are sorted alphabetically ignoring different cases.
				Collections.sort(fileContent, String.CASE_INSENSITIVE_ORDER);
				// Clear and rewrite the file with the sorted entries.
				clear();
				BufferedWriter writer = new BufferedWriter(new FileWriter(_file, false));
				try {
					writer.write(getCompressedString(fileContent));
				} finally {
					writer.close();
				}
				return String.format(MESSAGE_SORT_FILE_SUCCESS, _file.getName());
			}
		} catch (IOException exceptionMessage) {
			return String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage());
		}
	}
	
	/**
	 * This method search the storage file for entries containing the specified 
	 * substring. It returns the String of entries containing the substring or 
	 * returns not found message if no entry matches the substring.
	 * @param searchToken	The specified substring to search for in the storage.
	 * @return				Returns the String of entries containing the substring 
	 * 						or returns not found message if no entry matches.
	 */
	public String search(String searchToken) {
		// Initialize the required reader objects to read the storage file.
		try (BufferedReader reader = new BufferedReader(new FileReader(_file))) {
			ArrayList<String> fileContent = getFileContent(reader);
			ArrayList<String> searchResult = new ArrayList<String>();
			// Iterate and search through every entry in the file content.
			for (int i = 0; i < fileContent.size(); i++) {
				String currentLineLowerCase = fileContent.get(i).toLowerCase();
				if (currentLineLowerCase.contains(searchToken.toLowerCase())) {
					searchResult.add(fileContent.get(i));
				}
			}
			if (searchResult.isEmpty()) {
				return String.format(MESSAGE_SEARCH_FAILED, searchToken, _file.getName());
			} else {
				return String.format(MESSAGE_SEARCH_SUCCESS, searchResult.size(), searchToken, _file.getName()) 
						+ getCompressedString(addNumberings(searchResult));
			}
		} catch (IOException exceptionMessage) {
			return String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage());
		}
	}
	
	public static boolean isEmptyFile(File fileToCheck) {
		// Initialize the required reader objects to read the storage file.
		try (BufferedReader reader = new BufferedReader(new FileReader(fileToCheck))) {
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
		showToUser(MESSAGE_ENTER_COMMAND);
		String command = scannerObject.nextLine();
		return command;
	}
	
	public static String getCommandType(String commandLine){
		if (commandLine.contains(" ")) {
			return commandLine.substring(0, commandLine.indexOf(" "));
		} else {
			return commandLine;
		}
	}
	
	public static String getCommandParameter(String commandLine) {
		if (commandLine.contains(" ")) {
			return commandLine.substring(commandLine.indexOf(" ") + 1, commandLine.length());
		} else {
			return null;
		}
	}
	
	/**
	 * This method returns all the lines in the specified file 
	 * encapsulated in an array list.
	 * @param reader	The reader object to read through the storage file.
	 * @return			An array list of string containing the entries in the file.
	 */
	public static ArrayList<String> getFileContent(BufferedReader reader) {
		try {
			ArrayList<String> fileContent = new ArrayList<String>();
			String currentLine = reader.readLine();
			
			while (currentLine != null) {
				fileContent.add(currentLine);
				currentLine = reader.readLine();
			}
			return fileContent;
		} catch (IOException exceptionMessage) {
			showToUser(String.format(MESSAGE_EXCEPTION, exceptionMessage.getMessage()));
			return null;
		}
	}
	
	/**
	 * This method adds numberings to all entries of the array list and 
	 * return the numbered array list of strings.
	 * @param fileContent	The array list with no numbering in all entries.
	 * @return				The array list with numberings in all entries.
	 */
	public static ArrayList<String> addNumberings(ArrayList<String> fileContent) {
		if (fileContent == null) {
			return null;
		} else {
			ArrayList<String> fileContentWithNumberings = new ArrayList<String>();
			
			for (int i = 0; i < fileContent.size(); i++) {
				fileContentWithNumberings.add(String.format(FORMAT_LINE_WITH_NUMBERING, i + 1, fileContent.get(i)));
			}
			return fileContentWithNumberings;
		}
	}
	
	/**
	 * This method compresses the specified array list to a string with each 
	 * element separated by a line break.
	 * @param fileContent	The array list of string to be compressed.
	 * @return				The string containing the compressed array list.
	 */
	public static String getCompressedString(ArrayList<String> fileContent) {
		String compressedString = new String("");
		
		if (fileContent == null) {
			return compressedString;
		} else {
			for (int i = 0; i < fileContent.size(); i++) {
				compressedString += String.format(FORMAT_LINE, fileContent.get(i));
			}
			return compressedString;
		}
	}
	
	public static boolean isLineNumberValid(int lineNumber, ArrayList<String> fileContent) {
		if (lineNumber > fileContent.size()) {
			return false;
		} else {
			return true;
		}
	}
	
	public static String getStorageFileName(String[] args) {
		if (isEmptyArray(args)) {
			return null;
		} else {
			return args[POSITION_OF_FILENAME];
		}
	}
	
	public static boolean isEmptyArray(String[] arrayToCheck) {
		if (arrayToCheck.length == 0){
			return true;
		} else {
			return false;
		}
	}
	
	public static void showToUser(String stringToShow) {
		System.out.print(stringToShow);
	}
}
