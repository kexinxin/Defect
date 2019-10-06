import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String pathname2="igraph.dot";
        String pathname = "igraph2BugFile";
        String resultFile="igraph2Result.dot";

        List<String> bugfileList=new ArrayList<String>();
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                //System.out.println(line);
                bugfileList.add(line.split("/")[line.split("/").length-1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        List<String> lines=new ArrayList<String>();


        List<String> fileList=new ArrayList<String>();
        try (FileReader reader = new FileReader(pathname2);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                System.out.println(line);
                if(line.contains("node")&&line.contains("label")){
                    String[] tokens=line.split(" ");
                    String fileName=tokens[7];
                    fileName=fileName.replaceAll("\"","");
                    fileName=fileName.split("\\\\")[fileName.split("\\\\").length-1];
                    if(!fileList.contains(fileName)) fileList.add(fileName);
                }
                //fileList.add(line.split("/")[line.split("/").length-1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String,String> hashMap=new HashMap<String,String>();
        for(String str:fileList){
            if(str.endsWith(".h")){
                for(String source:fileList){
                    if(str.split("\\.")[0].equals(source.split("\\.")[0])&&!source.endsWith(".h"))
                        hashMap.put(str,source);
                }
            }
        }


        try (FileReader reader = new FileReader(pathname2);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                //System.out.println(line);
                if(line.contains("node")&&line.contains("label")){
                    String[] tokens=line.split(" ");
                    String fileName=tokens[7];
                    fileName=fileName.replaceAll("\"","");
                    fileName=fileName.split("\\\\")[fileName.split("\\\\").length-1];
                    if(fileName.endsWith(".h")){
                        String str=hashMap.get(fileName);
                        if(str==null) continue;
                        line=line.replaceAll(fileName,hashMap.get(fileName));
                    }
                    for(String name:bugfileList){
                        if(line.contains(name)){
                            line=line.replace("ff9999","267b2c");
                        }
                    }
                }else if(line.contains("->")){
                    String[] tokens=line.split(" ");
                    tokens[4]=tokens[4].replaceAll("\"","");
                    String start=tokens[4].split("\\\\")[tokens[4].split("\\\\").length-1];
                    tokens[6]=tokens[6].replaceAll("\"","");
                    String end=tokens[6].split("\\\\")[tokens[6].split("\\\\").length-1];
                    if(start.endsWith(".h")){
                        if(hashMap.get(start)==null) continue;
                        line=line.replace(start,hashMap.get(start));
                    }
                    if(end.endsWith(".h")) {
                        if(hashMap.get(end)==null) continue;
                        line=line.replace(end,hashMap.get(end));
                    }
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }




        try {
            File writeName = new File(resultFile); // 相对路径，如果没有则要建立一个新的output.txt文件
            writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                for(String line:lines){
                    out.write(line+"\r\n");
                }
                out.flush(); // 把缓存区内容压入文件
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //for(String str:lines) System.out.println(str);
    }
}
