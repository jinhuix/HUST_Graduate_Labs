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
import com.xjh.utils.Predition;
import com.xjh.utils.WordInputFormat;

import java.io.IOException;
import java.util.Map;
import java.util.Set;


// 第三个Job
// 计算每个文件的预测类，并输出 <文件名, 类名>
public class Predict {
    
    private static Map<String, Double> priors = Predition.getPriors();  // 先验概率
    private static Map<String, Double> termProbability = Predition.getTermProbability();    // 条件概率 <<类别,单词>,概率>
    private static Set<String> termSet = Predition.getTermSet();    // 训练集中单词集合
    private static Map<String, Double> classTermSum = Predition.getClassTermSum();  // 训练集中三类数据的单词个数


    // Map阶段：
    // 输入 <文件名, 类名@单词1,单词2,...>
    // 输出 <文件名, <类名, 概率>>，计算每个类别的概率
    public static class PredictMapper extends Mapper<Text, Text, Text, Text> {

        @Override
        protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {
            String[] parts = value.toString().split("@");
            String[] split = parts[1].split(",");       // 提取单词列表

            for (String className : Config.CLASS_ARRAY) {
                String[] split2 = split[1].split(",");  //当前单词组
                double prior = priors.get(className);   //先验概率
                for (String s : split2) {
                    // 求文档属于该类的概率：公式 = 先验概率 + 每个单词的概率
                    double temp = 0.0;
                    if (!termSet.contains(s)) {         // 若单词不在训练的集合中
                        temp = Math.log(1 / (classTermSum.get(className) + termSet.size()));
                    } else {
                        temp = termProbability.get(className + "," + s);
                    }
                    prior += temp;
                }
                String v = className + "," + prior;
                context.write(key, new Text(v));
            }
        }
    }

    // Reduce阶段：
    // 输入 <文件名, {<类名1, 概率1>, <类名2, 概率2>, ...}>
    // 输出 <文件名, 类名>，选择概率最大的类
    public static class PredictReducer extends Reducer<Text, Text, Text, Text> {

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String maxClass = "";
            double maxPro = Double.NEGATIVE_INFINITY; // 初始值设为最小的负值

            // 遍历所有类别的概率，选择最大概率的类别
            for (Text value : values) {
                String className = value.toString().split(",")[0];
                double pro = Double.valueOf(value.toString().split(",")[1]);
                if (pro > maxPro) {
                    maxClass = className;
                    maxPro = pro;
                }
            }
            context.write(key, new Text(maxClass)); // 输出预测结果
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Predit");

        // 设置作业配置
        job.setJarByClass(Predict.class);
        job.setMapperClass(Predict.PredictMapper.class);
        job.setReducerClass(Predict.PredictReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setInputFormatClass(WordInputFormat.class);
        
        // 设置输入输出路径
        FileInputFormat.addInputPath(job, new Path(Config.PREDICT_MAP_INPUT));
        FileOutputFormat.setOutputPath(job, new Path(Config.PREDICT_REDUCE_OUTPUT));

        // 提交作业并等待完成
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
