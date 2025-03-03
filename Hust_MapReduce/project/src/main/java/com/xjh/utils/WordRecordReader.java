package com.xjh.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;


// 自定义RecordReader：读取每个文件夹中的文件，并将其转换为 <文件名, 类名@term1,term2,...> 格式的键值对
public class WordRecordReader extends RecordReader<Text, Text> {
    private String className;           // 当前文件的类名
    private Path[] files;               // 当前目录下的所有文件路径
    private int currentFileIndex = 0;   // 当前处理到的文件索引
    private FileSystem fs;              // 文件系统
    private float progress = 0;         // 处理进度
    private Text key;                   // 输出的键（文件名）
    private Text value;                 // 输出的值（类名和单词列表）

    public void initialize(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        Configuration conf = taskAttemptContext.getConfiguration();
        FileSplit split = (FileSplit) inputSplit;
        Path path = split.getPath();
        className = path.getName();         // 获取类名，即目录名
        fs = path.getFileSystem(conf);
        FileStatus[] statuses = fs.listStatus(path);    // 获取目录下的所有文件
        files = FileUtil.stat2Paths(statuses);          // 将文件状态转换为路径数组
    }

    public boolean nextKeyValue() throws IOException, InterruptedException {
        // 如果所有文件已经处理完，返回false
        if (currentFileIndex >= files.length) {
            return false;
        }
        if (null == key) {
            key = new Text();
        }
        if (null == value) {
            value = new Text();
        }

        // 读取当前文件
        Path currentFile = files[currentFileIndex];
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(className).append("@");

        try (InputStream in = fs.open(currentFile); Scanner scanner = new Scanner(in)) {
            // 将文件中的内容按行读取，并构建类名和单词列表
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                contentBuilder.append(line).append(",");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 设置当前键为文件名，值为类名和单词列表
        key.set(currentFile.getName());
        value.set(contentBuilder.toString());

        currentFileIndex++;
        progress++; // 更新进度
        return true;
    }

    public Text getCurrentKey() throws IOException, InterruptedException {
        return key;
    }

    public Text getCurrentValue() throws IOException, InterruptedException {
        return value;
    }

    public float getProgress() throws IOException, InterruptedException {
        return progress / files.length;
    }

    public void close() throws IOException {

    }
}
