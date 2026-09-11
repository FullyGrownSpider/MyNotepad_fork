package qtnotes.init;/*
 * Copyright (c) 2021 Mohit Saini, Under MIT License. Use is subject to license terms.
 * 
 */

import qtnotes.gui.GUIHandler;

import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


/**
 * qtnotes.init.InitialValues class help to get the initial values of the MSNotepad, and,
 * also save these parameters in the "Settings.txt" file at the window close and
 * load these parameters again in the variables at the start.
 */
public class InitialValues {
	public static final String NEW_FILE = "Untitled";
    private static final AtomicBoolean showStatusBar = new AtomicBoolean(true);
	private static final AtomicBoolean wrapTheLine = new AtomicBoolean(false);
	private static final AtomicBoolean replaceQuote = new AtomicBoolean(true);
	private static final AtomicBoolean exception = new AtomicBoolean(true);
	private static final AtomicInteger frameWidth = new AtomicInteger(600);
	private static final AtomicInteger frameHeight = new AtomicInteger(450);
	private static final AtomicInteger frameX = new AtomicInteger(450);
	private static final AtomicInteger frameY = new AtomicInteger(450);
	private static final AtomicInteger caretPosition = new AtomicInteger(0);
	private static final AtomicInteger zoomLevel = new AtomicInteger(100);
	private static final AtomicInteger editorFontSize = new AtomicInteger(24);
	private static final AtomicInteger editorFontStyle = new AtomicInteger(0);

	private static final AtomicReference<String> editorFontFamily = new AtomicReference<>("Consolas");
	private static final AtomicReference<String> fileName = new AtomicReference<>(NEW_FILE);
	private static final AtomicReference<String> filePath = new AtomicReference<>("");

	private static Font editorFont = new Font("Consolas", Font.PLAIN, 24);

	private static final Map <String, Serializable> settingReader = new HashMap<>();

	/**
	 * getShowStatusBar method is help to get the showStatusBar variable.
	 * @return	the showStatusBar value.
	 */
	public static Boolean getShowStatusBar() {
		return showStatusBar.get();
	}

	/**
	 * setShowStatusBar method is help to set the showStatusBar variable.
	 * @param value the value of showStatusBar.
	 */
	public static void setShowStatusBar(Boolean value) {
		showStatusBar.set(value);
	}

	public static Boolean getWrapTheLine() {
		return wrapTheLine.get();
	}

	public static Boolean getReplaceQuote() {
		return replaceQuote.get();
	}

	public static void setReplaceQuote(Boolean value) {
		replaceQuote.set(value);
	}

	public static int getZoom() {
		return zoomLevel.get();
	}

	public static void setZoom(int value) {
		zoomLevel.set(value);
	}

	/**
	 * setWrapTheLine method is help to set the wrapTheLine variable.
	 * @param value the value of wrapTheLine.
	 */
	public static void setWrapTheLine(Boolean value) {
		wrapTheLine.set(value);
	}
    
	/**
	 * getFrameWidth method is help to get the frameWidth variable.
	 * @return	the frameWidth value.
	 */
	public static int getFrameWidth() {
		return frameWidth.get();
	}

	/**
	 * setFrameWidth method is help to set the frameWidth variable.
	 * @param value the value of frameWidth.
	 */
	public static void setFrameWidth(int value) {
		frameWidth.set(value);
	}

	/**
	 * getFrameHeight method is help to get the frameHeight variable.
	 * @return	the frameHeight value.
	 */
	public static int getFrameHeight() {
		return frameHeight.get();
	}

	/**
	 * setFrameHeight method is help to set the frameHeight variable.
	 * @param value the value of frameHeight.
	 */
	public static void setFrameHeight(int value) {
		frameHeight.set(value);
	}

	public static int getFrameX(){
		return frameX.get();
	}

	public static int getFrameY(){
		return frameY.get();
	}

	public static void setFrameX(int value) {
		frameX.set(value);
	}
	public static void setFrameY(int value) {
		frameY.set(value);
	}
	/**
	 * getEditorFont method is help to get the editorFont variable.
	 * @return	the editorFont boolean value.
	 */
	public static Font getEditorFont() {
		return editorFont;
	}

	/**
	 * setEditorFont method is help to set the editorFont variable.
	 * @param font the value of editorFont.
	 */
	public static void setEditorFont(Font font) {
		editorFont = font;
		GUIHandler.getEditorTextArea().setFont(font);
	}

	/**
	 * getFileName method is help to get the fileName variable.
	 * @return	the fileName value.
	 */
	public static String getFileName() {
		return fileName.get();
	}

	/**
	 * setFileName method is help to set the fileName variable.
	 * @param name the value of fileName.
	 */
	public static void setFileName(String name) {
		fileName.set(name);
	}

	/**
	 * getFilePath method is help to get the filePath variable.
	 * @return	the filePath value.
	 */
	public static String getFilePath() {
		return filePath.get();
	}

	/**
	 * setFilePath method is help to set the filePath variable.
	 * @param path the value of filePath.
	 */
	public static void setFilePath(String path) {
		if (path == null)
			path = "";
		filePath.set(path);
	}

	/**
	 * getCaretPosition method is help to get the caretPosition variable.
	 * @return	the caretPosition value.
	 */
	public static int getCaretPosition() {
		return caretPosition.get();
	}

	/**
	 * setCaretPosition method is help to set the caretPosition variable.
	 * @param value the value of caretPosition.
	 */
	public static void setCaretPosition(int value) {
		caretPosition.set(value);
	}

	/**
	 * writeToFile method is help to save the value in the file
	 * decide what to store here
	 */
	public static void writeToFile() {
		try {
			caretPosition.set(GUIHandler.getCursorLocationOrSelectStart());
			File file = new File("Settings.txt");
			FileWriter writer = new FileWriter(file);
			for (var item : settingReader.entrySet()) {
				writer.write(item.getKey() + " : " +item.getValue().toString() + "\n");
			}
			writer.close();
			System.out.println(file.getAbsolutePath());
		} catch (Exception ex) {
			System.out.println("Unable to write Setting.text file !");
		}
	}


	/**
	 * readFromFile method is help to load the variables from the file.
	 */
	public static void readFromFile() {
		//init settingReader, because you always have to read before writing
		settingReader.put("Show-qtnotes.gui.helper.StatusBar", showStatusBar);
		settingReader.put("Wrap-The-Line", wrapTheLine);
		settingReader.put("Zoom-Level", zoomLevel);
		settingReader.put("replaceQuote", replaceQuote);
		settingReader.put("Font-Family", editorFontFamily);
		settingReader.put("Font-Style", editorFontStyle);
		settingReader.put("Font-Size", editorFontSize);
		settingReader.put("Opened-File-Name", fileName);
		settingReader.put("Opened-File-Path", filePath);
		settingReader.put("Caret-Position", caretPosition);
		settingReader.put("Frame-Width", frameWidth);
		settingReader.put("Frame-Height", frameHeight);
		settingReader.put("Frame-X", frameX);
		settingReader.put("Frame-Y", frameY);
		settingReader.put("Exception", exception);

		try {
			File file = new File("Settings.txt");
			Scanner read = new Scanner(file);
			while (read.hasNext()){
				String value = read.nextLine();
				int valueLocation = value.indexOf(":");
				if (valueLocation == -1) continue;
				var toEdit = settingReader.get(value.substring(0, valueLocation).trim());
				if (toEdit == null) continue;
				valueLocation++;
				if (toEdit instanceof AtomicBoolean){
					((AtomicBoolean) toEdit).set(Boolean.parseBoolean(value.substring(valueLocation).trim()));
				} else if (toEdit instanceof AtomicInteger){
					((AtomicInteger) toEdit).set(Integer.parseInt(value.substring(valueLocation).trim()));
				} else {
                    //noinspection unchecked
                    ((AtomicReference<String>) toEdit).set(value.substring(valueLocation).trim());
				}
			}

			read.close();
            //noinspection MagicConstant
            editorFont = new Font(editorFontFamily.get(), editorFontStyle.get(), editorFontSize.get());
		} catch (Exception ex) {
			System.out.println("default setting work!");
		}
	}

	public static boolean getException() {
		return exception.get();
	}

	public static void setException(boolean b) {
		exception.set(b);
	}
}
