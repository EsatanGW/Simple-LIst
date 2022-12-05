package com.cyc.shelltool;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ShellTool {

    public static void main(String[] args) throws Exception {
        String assembleFlag = "ALL";
        if(args != null && args.length > 0){
            assembleFlag = args[0];
        }
        System.out.println("assembleFlag : "+ assembleFlag);
        System.out.println("current run path : "+System.getProperty("user.dir"));
        File currentPathFile = new File(System.getProperty("user.dir"));
        String parentPath = "";
        if(currentPathFile.getName().equals("shelltool")){
            parentPath = currentPathFile.getParent();
        }else {
            parentPath = currentPathFile.getPath();
        }
        System.out.println("parentPath : "+parentPath);
        File tempFileApk = new File(parentPath+ File.separator +"shelltool/source/apk/");
        if (tempFileApk.exists()) {
            File[]files = tempFileApk.listFiles();
            for(File file: files){
                if (file.isFile()) {
                    file.delete();
                }
            }
        }

        File result = new File(parentPath+ File.separator +"shelltool/result/");
        if (result.exists()) {
            File[]files = result.listFiles();
            for(File file: files){
                if (file.isFile()) {
                    file.delete();
                }
            }
        }

        /**
         * 第一步 处理原始apk 加密dex
         *
         */
        AES.init(AES.DEFAULT_PWD);
        File apkDirs = null;
        switch (assembleFlag){
            case "QA":
                apkDirs = new File(parentPath+ File.separator +"app/build/outputs/apk/qa/");
                break;
            case "UAT":
                apkDirs = new File(parentPath+ File.separator +"app/build/outputs/apk/uat/");
                break;
            case "PROD":
                apkDirs = new File(parentPath+ File.separator +"app/build/outputs/apk/prod");
                break;
            case "ALL":
            default:
                apkDirs = new File(parentPath+ File.separator +"app/build/outputs/apk/");
                break;
        }
        List<File> apkList = getAllFile(apkDirs);
        //查找已经编译好的apk,取时间戳最近的apk
        List<File> apkFileList = new ArrayList<>();

        for(File file : apkList){
            System.out.println("file : "+file.getAbsoluteFile());
            if(file.getName().startsWith("app")&&file.getName().endsWith("apk")){
                apkFileList.add(file);
            }
        }
        if(apkFileList.size() == 0){
            throw new RuntimeException("not find shell apk");
        }
        for (File apkFile : apkFileList){
            String apkName = apkFile.getName();
            System.out.println("apkName : "+apkName);
            //解压apk
            File mainDexFile = AES.encryptAPKFile(apkFile,tempFileApk);
            if (tempFileApk.isDirectory()) {
                File[] listFiles = tempFileApk.listFiles();
                for (File file : listFiles) {
                    if (file.isFile()) {
                        if (file.getName().endsWith(".dex")) {
                            String name = file.getName();
                            System.out.println("rename step1:"+name);
                            int cursor = name.indexOf(".dex");
                            String newName = file.getParent()+ File.separator + name.substring(0, cursor) + "_" + ".dex";
                            System.out.println("rename step2:"+newName);
                            file.renameTo(new File(newName));
                        }
                    }
                }
                //添加名称文件，覆盖安装时监测是否重新加载dex
                File versionFile = new File(tempFileApk.getAbsolutePath() + File.separator + "apkname.txt");
                FileOutputStream outputStream = new FileOutputStream(versionFile);
                outputStream.write(apkName.getBytes());
                outputStream.flush();
                outputStream.close();
            }


            /**
             * 第二步 将壳dex复制到apk/下
             */
            File aarDex = new File(parentPath+ File.separator +"shelltool/source/shelldex/classes.dex");
            File tempMainDex = new File(tempFileApk.getPath() + File.separator + "classes.dex");
            if (!tempMainDex.exists()) {
                tempMainDex.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(tempMainDex);
            byte[] fbytes = Utils.getBytes(aarDex);
            fos.write(fbytes);
            fos.flush();
            fos.close();


            /**
             * 第3步 打包签名
             */
            File unsignedApk = new File(parentPath+ File.separator +"shelltool/result/apk-unsigned.apk");
            unsignedApk.getParentFile().mkdirs();
//        File disFile = new File(apkFile.getAbsolutePath() + File.separator+ "temp");
            Zip.zip(tempFileApk, unsignedApk);
            //不用插件就不能自动使用原apk的签名...
            File signedApk = new File(parentPath+ File.separator +"shelltool/result/"+ apkName);
            Signature.signature(unsignedApk, signedApk,parentPath);

            unsignedApk.delete();
            deleteFile(tempFileApk);
        }

        Desktop.getDesktop().open(new File(parentPath+ File.separator +"shelltool/result/"));
    }

    private static List<File> getAllFile(File file){
        List<File> fileList = new ArrayList<>();
        if(file.isDirectory()){
            for (File fileItem : file.listFiles()){
                fileList.addAll(getAllFile(fileItem));
            }
        }else {
            fileList.add(file);
        }
        return fileList;
    }

    private static void deleteFile(File file) {
        /**
         *  File[] listFiles()
         *    返回一个抽象路径名数组，这些路径名表示此抽象路径名表示的目录中的文件。
         */
        File[] files = file.listFiles();
        if (files!=null){//如果包含文件进行删除操作
            for (int i = 0; i <files.length ; i++) {
                if (files[i].isFile()){
                    //删除子文件
                    files[i].delete();
                }else if (files[i].isDirectory()){
                    //通过递归的方法找到子目录的文件
                    deleteFile(files[i]);
                }
                files[i].delete();//删除子目录
            }
        }
    }
}