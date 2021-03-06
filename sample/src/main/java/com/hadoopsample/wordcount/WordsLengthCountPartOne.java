package com.hadoopsample.wordcount;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class WordsLengthCountPartOne {

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf);
        job.setJarByClass( WordsLengthCountPartOne.class );

        job.setMapperClass( WordLengthCountMapper.class );
        job.setReducerClass( WordLengthCountReducer.class );

//        job.setMapOutputKeyClass(IntWritable.class);
//        job.setMapOutputValueClass( Text.class );

        job.setOutputKeyClass( IntWritable.class );
        job.setOutputValueClass( Text.class );

        FileInputFormat.setInputPaths( job, new Path( args[0] ) );
        FileOutputFormat.setOutputPath( job, new Path( args[1] ) );

        boolean done =  job.waitForCompletion(true);
        System.exit( done?0:1 );

    }
    // Read the file,
    // map the words by the length of the words.
    private static class WordLengthCountMapper extends Mapper<LongWritable,Text,IntWritable, Text>{
        StringTokenizer st = null;
        @Override
        protected void map(LongWritable key, Text text, Context context)
            throws IOException, InterruptedException {
            //  want to use regular expression as the symbol to split different words.
            // "/[^0-9^a-z]/g" if it is not 0-9 nor a-z nor -,
//            st= new StringTokenizer( text.toString().toLowerCase() );
            text.set(text.toString().toLowerCase());
            String[] word = text.toString().split( "[^\\w-]" );
            for(String oneWord : word){
                int length = oneWord.length();
                context.write( new IntWritable(length), new Text(oneWord));
            }
        }
    }

    // get the data from map.
    private static class WordLengthCountReducer extends Reducer<IntWritable, Text, IntWritable, Text>{
        @Override
        protected void reduce(IntWritable key, Iterable<Text> textCollection, Context context)
            throws IOException, InterruptedException {
            TreeMap<String, Integer> wordCountMap = new TreeMap<>();
            int wordNumWithSameLength = 0;
            while(textCollection.iterator().hasNext()){
                wordNumWithSameLength++;
                String keyWord = textCollection.iterator().next().toString();
                if(wordCountMap.containsKey( keyWord )){
                    int tmp = wordCountMap.get(keyWord).intValue() + 1;
                    wordCountMap.put( keyWord, tmp );
                }
                else
                    wordCountMap.put(keyWord,1);
            }
            String statistic = wordNumWithSameLength + "\n" ;

            // Print the detail that how many times a word appeared.
//            for(Map.Entry<String,Integer> entry : wordCountMap.entrySet()){
//                statistic +=  entry.getKey() + " " + entry.getValue() + "\n";
//            }
            context.write( key,new Text(statistic) );
        }
    }
}

