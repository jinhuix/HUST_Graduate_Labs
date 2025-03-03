package com.xjh.jobs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.xjh.utils.ClassInputFormat;
import com.xjh.utils.Config;

import java.io.IOException;


// 第一个MapReduce任务，统计每种类别的文档数量，用于计算先验概率
// 输出格式：<类别名称，文档数量>
public class ClassCount {
    // Mapper: 不做实际处理，直接将输入输出为<类别名称, 1>形式
    public static class ClassCountMapper extends Mapper<Text, IntWritable, Text, IntWritable> {
        @Override
        protected void map(Text key, IntWritable value, Context context) throws IOException, InterruptedException {
            context.write(key, value);
        }
    }

    // Reducer: 汇总每个类别的文档数量
    public static class ClassCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable totalCount = new IntWritable();

        @Override
        protected void reduce(Text category, Iterable<IntWritable> counts, Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable count : counts) {
                sum += count.get();
            }
            totalCount.set(sum);
            context.write(category, totalCount);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "ClassCount");
        // 设置主类
        job.setJarByClass(ClassCount.class);
        // 设置Mapper和Reducer类
        job.setMapperClass(ClassCountMapper.class);
        job.setReducerClass(ClassCountReducer.class);
        // 设置输出键值对的类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        // 设置自定义InputFormat
        job.setInputFormatClass(ClassInputFormat.class);
        // 输入数据路径
        FileInputFormat.addInputPath(job, new Path(Config.CLASS_SUM_MAP_INPUT));
        // 输出数据路径
        FileOutputFormat.setOutputPath(job, new Path(Config.CLASS_SUM_REDUCE_OUTPUT));

        // 提交作业并等待完成
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
