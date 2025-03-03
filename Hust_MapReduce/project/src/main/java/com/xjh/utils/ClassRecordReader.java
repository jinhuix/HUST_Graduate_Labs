package com.xjh.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import java.io.IOException;

// 自定义 RecordReader 负责将输入的目录内容读取为键值对
public class ClassRecordReader extends RecordReader<Text, IntWritable> {
    private String className;           // 当前处理的类别名称
    private Path[] files;               // 当前类别下的所有文档路径
    private int currentFileIndex = 0;   // 当前处理到的文件索引
    private float progressCounter = 0;  // 进度计数
    private Text key;                   // 输出键
    private IntWritable value;          // 输出值

    public void initialize(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        Configuration conf = taskAttemptContext.getConfiguration();
        FileSplit split = (FileSplit) inputSplit;
        Path path = split.getPath();
        System.out.println("正在处理目录: " + path.toString());
        className = path.getName();     // 提取类别名称（即目录名称）

        //获得子目录，即是目录下的所有文件
        FileSystem fs = path.getFileSystem(conf);
        FileStatus[] statuses = fs.listStatus(path);
        files = FileUtil.stat2Paths(statuses);
    }

    public boolean nextKeyValue() throws IOException, InterruptedException {
        if (currentFileIndex >= files.length) {  // 已经处理完所有文件
            return false;
        }

        // 初始化键和值
        if (null == key) {
            key = new Text();
        }
        if (null == value) {
            value = new IntWritable();
        }
        
        // 设置当前键值对
        key.set(className);
        value.set(1);   // 每个文件贡献1次计数

        // 更新处理的文件索引
        currentFileIndex++;     
        progressCounter++;
        return true;
    }

    public Text getCurrentKey() throws IOException, InterruptedException {
        return key;
    }

    public IntWritable getCurrentValue() throws IOException, InterruptedException {
        return value;
    }

    // 返回处理进度的百分比，计算为当前处理的文件数与总文件数的比例
    public float getProgress() throws IOException, InterruptedException {
        return progressCounter / files.length;
    }

    public void close() throws IOException {
        // 可以在此释放资源，如果需要的话
    }
}
