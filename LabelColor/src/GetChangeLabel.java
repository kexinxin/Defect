import java.io.*;
import java.util.*;

public class GetChangeLabel {
    public static void main(String[] args){
        HashMap<String,String> hashMap=new HashMap<String,String>();
        String pathname = "igraph2Result.dot";
        String relationName="igraph2Relation.txt";
        String bugFileName="igraph2BugFile";
        String labelName="igraph2Label.txt";

        List<String> fileList=new ArrayList<String>();
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            //网友推荐更加简洁的写法
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
        System.out.println(fileList.size());
        for(int i=1;i<=fileList.size();i++){
            hashMap.put(fileList.get(i-1),i+"");
        }

        try {
            File writeName = new File("hashMapResult.txt"); // 相对路径，如果没有则要建立一个新的output.txt文件
            writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
//                for(String line:lines){
//                    out.write(line+"\r\n");
//                }
                Iterator iter=hashMap.entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry entry=(Map.Entry) iter.next();
                    String key=(String)entry.getKey();
                    String val=(String)entry.getValue();
                    out.write(key+" "+val+"\r\n");
                }
                out.flush(); // 把缓存区内容压入文件


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> relation=new ArrayList<String>();
        ArrayList<String> relationFile=new ArrayList<String>();
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                System.out.println(line);
                if(line.contains("->")){
                    String[] tokens=line.split(" ");
                    tokens[4]=tokens[4].replaceAll("\"","");
                    String start=tokens[4].split("\\\\")[tokens[4].split("\\\\").length-1];
                    tokens[6]=tokens[6].replaceAll("\"","");
                    String end=tokens[6].split("\\\\")[tokens[6].split("\\\\").length-1];
                    String startId=hashMap.get(start);
                    String endId=hashMap.get(end);
                    if(!startId.equals(endId)){
                        relation.add(startId+" "+endId);
                        if(!relationFile.contains(startId)) relationFile.add(startId);
                        if(!relationFile.contains(endId)) relationFile.add(endId);
                    }
                }
                //fileList.add(line.split("/")[line.split("/").length-1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            File writeName = new File(relationName); // 相对路径，如果没有则要建立一个新的output.txt文件
            writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                for(int i=0;i<relation.size();i++) out.write(relation.get(i)+"\r\n");
                out.flush(); // 把缓存区内容压入文件
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        List<String> bugFileList=new ArrayList<String>();
        try (FileReader reader = new FileReader(bugFileName);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                //System.out.println(line);
                bugFileList.add(line.split("/")[line.split("/").length-1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            File writeName = new File(labelName); // 相对路径，如果没有则要建立一个新的output.txt文件
            writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                for(int i=0;i<fileList.size();i++){
                    if(bugFileList.contains(fileList.get(i))&&relationFile.contains(hashMap.get(fileList.get(i))))
                        out.write(hashMap.get(fileList.get(i))+" "+"1"+"\r\n");
                    else if(relationFile.contains(hashMap.get(fileList.get(i))))
                        out.write(hashMap.get(fileList.get(i))+" "+"0"+"\r\n");
                }
                out.flush(); // 把缓存区内容压入文件
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
