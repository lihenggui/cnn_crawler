package com.merxury.persistent;

import com.merxury.controller.Controller;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.util.*;

public class FileHelper {
    private List<String> tags;
    private Map<String, Integer> numberOfFiles;
    private String workingFolder;
    private static final String prefix = "file_";
    private static final String suffix = ".txt";
    private static FileHelper instance;
    private static final int CONTENT_MIN_LENGTH = 15;

    public FileHelper(String workingFolder) throws IOException {
        numberOfFiles = new Hashtable<>();
        tags = new ArrayList<>();
        this.workingFolder = workingFolder;
        Files.list(Paths.get(workingFolder))
                .filter(Files::isDirectory)
                .filter(path -> !path.getFileName().toString().startsWith("."))
                .forEach(path -> tags.add(path.getFileName().toString()));
        tags.forEach(tag -> numberOfFiles.put(tag, 0));
    }

    /**
     * detects whether a category exists or not
     *
     * @param tag category name
     * @return true, exists; false, doesn't exist
     */
    public boolean isExistCategory(String tag) {
        return tags.contains(tag);
    }

    /**
     * create a new category by tag
     *
     * @param tag category name
     * @return true, create successful ; false , create fail
     */
    public boolean makeNewCategory(String tag) {
        try {
            Files.createDirectory(Paths.get(workingFolder, tag));
        } catch (FileAlreadyExistsException e) {
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean makeNewCategory(String tag, String subContent) {
        try {
            Files.createDirectory(Paths.get(workingFolder, tag, subContent));
        } catch (FileAlreadyExistsException e) {
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * returns the size of the categories
     *
     * @return size
     */
    public int sizeOfCategories() {
        return tags.size();
    }

    /**
     * get the counts of specified categories;
     *
     * @return count of files
     */
    public int getCategoryFileCount(String tag) {
        if (tag == null && !isExistCategory(tag)) {
            return 0;
        }
        if (numberOfFiles.get(tag) != null && numberOfFiles.get(tag) > 0) {
            return numberOfFiles.get(tag);
        } else {
            List<String> files = new ArrayList<>();
            try {
                Files.list(Paths.get(workingFolder, tag))
                        .filter(Files::isRegularFile)
                        .filter(file -> !file.getFileName().toString().startsWith(".") && file.getFileName().toString().endsWith(suffix))
                        .forEach(file -> files.add(file.getFileName().toString()));
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
            int size = files.size();
            numberOfFiles.put(tag, size);
            return size;
        }
    }

    public void save(String content, String tag) {
        if (!isExistCategory(tag)) {
            makeNewCategory(tag);
        }
        int count = getCategoryFileCount(tag);
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        iterator.setText(content);
        int start = iterator.first();
        try {
            for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
                //short sentence filter
                if (end - start < CONTENT_MIN_LENGTH) {
                    continue;
                }
                Files.write(Paths.get(workingFolder, tag, prefix + count + suffix), content.substring(start, end).getBytes());
                count++;
                numberOfFiles.put(tag, count);
                System.out.println(content.substring(start, end));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(String content,  String tag, String subCategory) {
        if (!isExistCategory(tag)) {
            makeNewCategory(tag, subCategory);
        }
        int count = getCategoryFileCount(tag);
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        iterator.setText(content);
        int start = iterator.first();
        try {
            for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
                //short sentence filter
                if (end - start < CONTENT_MIN_LENGTH) {
                    continue;
                }
                Files.write(Paths.get(workingFolder, tag, prefix + count + suffix), content.substring(start, end).getBytes());
                count++;
                numberOfFiles.put(tag, count);
                System.out.println(content.substring(start, end));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get an instance of FileHelper
     *
     * @param workingFolder folder
     * @return A FileHelper instance
     */
    public static FileHelper getInstance(String workingFolder) {
        if (instance == null) {
            synchronized (FileHelper.class) {
                if (instance == null) {
                    try {
                        instance = new FileHelper(workingFolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return instance;
    }

    public static void main(String args[]) throws IOException {
        new FileHelper(Controller.crawlStorageFolder);
    }
}
