package com.xjh.jobs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.xjh.utils.Config;
import com.xjh.utils.WordInputFormat;

import java.io.IOException;


// 第二个MapReduce任务：统计每个类别下单词的出现次数
// 输入：每个文件包含 <类名, term1,term2,term3,...>
// 输出：<类名, 单词, 出现次数>
public class WordCount {
    // Mapper类：输入格式为 <文件名, 类名@term1,term2,term3,...>
    // 输出格式为 <类名, 单词> -> 1
    public static class WordCountMapper extends Mapper<Text, Text, Text, IntWritable> {
        private IntWritable count = new IntWritable(1); // 输出的计数值为1

        @Override
        protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {
            String[] split = value.toString().split("@");   // 按 @ 分割类名和单词列表
            String className = split[0];
            String[] term = split[1].split(",");            // 分割出单词列表
            // 输出每个单词对应的计数（类名 + 单词）
            for (String s : term) {
                context.write(new Text(className + "," + term), count);
            }
        }
    }

    // Reducer类：输入格式为 <类名, {1, 1, 1, ...}>，输出格式为 <类名, 单词, 出现次数>
    public static class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {    // 累加每个单词的出现次数
                sum += val.get();
            }
            result.set(sum);                    // 设置单词的总出现次数
            context.write(key, result);         // 输出 <类名, 单词, 出现次数>
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "WordCount");
        job.setJarByClass(WordCount.class);
        job.setMapperClass(WordCount.WordCountMapper.class);
        job.setReducerClass(WordCount.WordCountReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        
        // 使用自定义InputFormat
        job.setInputFormatClass(WordInputFormat.class);

        // 设置输入输出路径
        FileInputFormat.addInputPath(job, new Path(Config.CLASS_SUM_MAP_INPUT));
        FileOutputFormat.setOutputPath(job, new Path(Config.TERM_REDUCE_OUTPUT));

        // 提交任务并等待完成
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
