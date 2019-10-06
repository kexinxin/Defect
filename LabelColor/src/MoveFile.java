import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MoveFile {

    public static List<String> fileNameList=new ArrayList<String>();
    public static List<String> fileNameTest=new ArrayList<String>();

    static void copy(String srcPathStr, String desPathStr)
    {
        //获取源文件的名称
        String newFileName = srcPathStr.substring(srcPathStr.lastIndexOf("\\")+1); //目标文件地址
        System.out.println("源文件:"+newFileName);
        desPathStr = desPathStr + File.separator + newFileName; //源文件地址
        System.out.println("目标文件地址:"+desPathStr);

        File file=new File(desPathStr);
        if(!file.exists())
        {
            try {
                while (file.getParentFile() != null && !file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try
        {
            FileInputStream fis = new FileInputStream(srcPathStr);//创建输入流对象
            FileOutputStream fos = new FileOutputStream(desPathStr); //创建输出流对象
            byte datas[] = new byte[1024*8];//创建搬运工具
            int len = 0;//创建长度
            while((len = fis.read(datas))!=-1)//循环读取数据
            {
                fos.write(datas,0,len);
            }
            fis.close();//释放资源
            fis.close();//释放资源
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private static void getFiles(String fileDir) {
        List<File> fileList = new ArrayList<File>();
        File file = new File(fileDir);
        File[] files = file.listFiles();// 获取目录下的所有文件或文件夹
        if (files == null) {// 如果目录为空，直接退出
            return;
        }
        // 遍历，目录下的所有文件
        for (File f : files) {
            if (f.isFile()) {
                fileList.add(f);
            } else if (f.isDirectory()) {
                getFiles(f.getAbsolutePath());
            }
        }
        for (File f1 : fileList) {
            if(f1.getAbsolutePath().endsWith(".c")||f1.getAbsolutePath().endsWith(".cpp")||f1.getAbsolutePath().endsWith(".h"))
                fileNameList.add(f1.getAbsolutePath());
//            if((f1.getAbsolutePath().endsWith(".c")||f1.getAbsolutePath().endsWith(".cpp")||f1.getAbsolutePath().endsWith(".h"))&&!fileNameTest.contains(f1.getName()))
//                fileNameTest.add(f1.getName());
            //System.out.println(f1.getName());
        }
    }


    public static void main(String[] args)
    {
        String fatherPath="I:\\Users\\usi\\Git\\";
        String projectName="Atmosphere-NXATMOSPHERE\\";
        String targetProjectName="zhenglihoudewenjian\\"+projectName;

        getFiles(fatherPath+projectName);

        for(String name:fileNameList){
            String srcPathStr = name;
            String desPathStr = fatherPath+targetProjectName; //目标文件地址
            copy(srcPathStr, desPathStr);
        }
    }
}